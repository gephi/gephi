/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.utils.longtask.api;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 * Portable long-task executor, that supports synchronous and asynchronous
 * execution, progress, cancellation and error management.
 * <p>
 * Note that only one task can be executed by the executor at one time.
 *
 * @author Mathieu Bastian
 * @see LongTask
 */
public final class LongTaskExecutor {

    private final boolean inBackground;
    private final long interruptDelay;
    private final String name;
    private boolean interruptCancel;
    private ThreadPoolExecutor executor;
    private volatile RunningLongTask currentTask;
    private Timer cancelTimer;
    private LongTaskListener listener;
    private LongTaskErrorHandler defaultErrorHandler;

    /**
     * Creates a new long task executor.
     *
     * @param doInBackground when <code>true</code>, the task will be executed
     *                       in a separate thread
     * @param name           the name of the executor, used to recognize threads by names
     * @param interruptDelay number of seconds to wait before * calling
     *                       <code>Thread.interrupt()</code> after a cancel request
     */
    public LongTaskExecutor(boolean doInBackground, String name, int interruptDelay) {
        this.inBackground = doInBackground;
        this.name = name;
        this.interruptCancel = true;
        this.interruptDelay = interruptDelay * 1000L;
    }

    /**
     * Creates a new long task executor.
     *
     * @param doInBackground doInBackground when <code>true</code>, the task
     *                       will be executed in a separate thread
     * @param name           the name of the executor, used to recognize threads by names
     */
    public LongTaskExecutor(boolean doInBackground, String name) {
        this(doInBackground, name, 0);
        this.interruptCancel = false;
    }

    /**
     * Creates a new long task executor.
     *
     * @param doInBackground doInBackground when <code>true</code>, the task
     *                       will be executed in a separate thread
     */
    public LongTaskExecutor(boolean doInBackground) {
        this(doInBackground, "LongTaskExecutor");
    }

    /**
     * Execute a long task with cancel and progress support. Task can be
     * <code>null</code>. In this case <code>runnable</code> will be executed
     * normally, but without cancel and progress support.
     *
     * @param task         the task to be executed, can be <code>null</code>.
     * @param runnable     the runnable to be executed
     * @param taskName     the name of the task, is displayed in the status bar if
     *                     available
     * @param errorHandler error handler for exception retrieval during
     *                     execution
     * @throws NullPointerException  if <code>runnable</code> * or
     *                               <code>taskName</code> is null
     * @throws IllegalStateException if a task is still executing at this time
     */
    public void execute(LongTask task, final Runnable runnable, String taskName,
                        LongTaskErrorHandler errorHandler) {
        if (runnable == null || taskName == null) {
            throw new NullPointerException();
        }
        execute(new RunningLongTask<>(task, runnable, taskName, errorHandler));
    }

    /**
     * Execute a long task with cancel and progress support. Task can be
     * <code>null</code>. In this case <code>callable</code> will be executed
     * normally, but without cancel and progress support.
     *
     * @param task         the task to be executed, can be <code>null</code>.
     * @param callable     the callable to be executed
     * @param taskName     the name of the task, is displayed in the status bar if
     *                     available
     * @param errorHandler error handler for exception retrieval during
     *                     execution
     * @return a future that can be used to retrieve the result of the task
     * @throws NullPointerException  if <code>callable</code> * or
     *                               <code>taskName</code> is null
     * @throws IllegalStateException if a task is still executing at this time
     */
    public <V> Future<V> execute(LongTask task, final Callable<V> callable, String taskName,
                                 LongTaskErrorHandler errorHandler) {
        if (callable == null || taskName == null) {
            throw new NullPointerException();
        }
        return execute(new RunningLongTask<>(task, callable, taskName, errorHandler));
    }

    private <V> Future<V> execute(RunningLongTask<V> runningLongtask) {
        if (inBackground) {
            return submit(runningLongtask);
        }
        // The synchronous path runs the whole task body on the calling thread and must
        // do so without holding this executor's monitor. cancel() is synchronized and
        // is typically called from the EDT (the progress bar's Cancel button), so a
        // monitor held here would block the EDT until the task completes and the cancel
        // request would always arrive too late to be of any use.
        currentTask = runningLongtask;
        runningLongtask.call();
        return runningLongtask.future;
    }

    /**
     * Execute a long task with cancel and progress support. Task can be
     * <code>null</code>. In this case <code>runnable</code> will be executed
     * normally, but without cancel and progress support.
     *
     * @param task     the task to be executed, can be <code>null</code>.
     * @param runnable the runnable to be executed
     * @throws NullPointerException  if <code>runnable</code> is null
     * @throws IllegalStateException if a task is still executing at this time
     */
    public void execute(LongTask task, Runnable runnable) {
        execute(task, runnable, "", null);
    }

    /**
     * Execute a long task with cancel and progress support. Task can be
     * <code>null</code>. In this case <code>callable</code> will be executed
     * normally, but without cancel and progress support.
     *
     * @param task     the task to be executed, can be <code>null</code>.
     * @param callable the callable to be executed
     * @throws NullPointerException  if <code>callable</code> is null
     * @throws IllegalStateException if a task is still executing at this time
     */
    public <V> Future<V> execute(LongTask task, Callable<V> callable) {
        return execute(task, callable, "", null);
    }

    /**
     * Submits the task to the background executor, creating it on first use.
     * Synchronized so the lazy creation and the submission stay atomic.
     */
    private synchronized <V> Future<V> submit(RunningLongTask<V> runningLongtask) {
        if (executor == null) {
            this.executor = new ThreadPoolExecutor(0, 1, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory());
        }
        Future<V> result = executor.submit(runningLongtask);
        runningLongtask.future = result;
        return result;
    }

    /**
     * Cancel the current task. If the task fails to cancel itself and if an
     * <code>interruptDelay</code> has been specified, the task will be
     * <b>interrupted</b> after <code>interruptDelay</code>. Using
     * <code>Thread.interrupt()</code> may cause hazardous behaviors and should
     * be avoided. Therefore any task should be cancelable.
     */
    public synchronized void cancel() {
        if (inBackground) {
            if (executor != null) {
                RunningLongTask rlt = currentTask;
                if (rlt != null) {
                    boolean res = rlt.cancel();
                    if (interruptCancel && !res) {
                        cancelTimer = new Timer(name + "_cancelTimer");
                        cancelTimer.schedule(new InterruptTimerTask(rlt), interruptDelay);
                    }
                }
            }
        } else {
            RunningLongTask rlt = currentTask;
            if (rlt != null) {
                boolean res = rlt.cancel();
                if (interruptCancel && !res) {
                    cancelTimer = new Timer(name + "_cancelTimer");
                    cancelTimer.schedule(new InterruptTimerTask(rlt), interruptDelay);
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if the executor is executing a task.
     *
     * @return <code>true</code> if a task is running, <code>false</code>
     * otherwise
     */
    public boolean isRunning() {
        return currentTask != null;
    }

    /**
     * Set the listener to this executor. Only a unique listener can be set to
     * this executor. The listener's
     * {@link LongTaskListener#taskFinished(LongTask)} is called when the task
     * terminates normally and its
     * {@link LongTaskListener#fatalError(Throwable)} when it terminates with an
     * uncaught exception.
     *
     * @param listener a listener for this executor
     */
    public void setLongTaskListener(LongTaskListener listener) {
        this.listener = listener;
    }

    /**
     * Set the default error handler. Use error handlers to get errors and
     * exceptions thrown during tasks execution.
     *
     * @param errorHandler the default error handler
     * @deprecated implement {@link LongTaskListener#fatalError(Throwable)} and
     * register the listener with {@link #setLongTaskListener(LongTaskListener)}
     * instead, so a single listener receives both the successful and the failed
     * outcome of a task. Default error handlers remain fully supported and are
     * still invoked.
     */
    @Deprecated
    public void setDefaultErrorHandler(LongTaskErrorHandler errorHandler) {
        if (errorHandler != null) {
            this.defaultErrorHandler = errorHandler;
        }
    }

    /**
     * Completes the given task. Only the first caller which successfully
     * claimed the completion (see <code>RunningLongTask.claimFinish()</code>)
     * should call this method, so a task is completed exactly once and the
     * listener is notified exactly once for it.
     *
     * @param runningLongTask the task which completed
     * @param notifyListener  whether <code>taskFinished()</code> should be called on
     *                        the listener, failures are reported to the error handler
     *                        and to the listener's <code>fatalError()</code> instead
     */
    private void finished(RunningLongTask runningLongTask, boolean notifyListener) {
        LongTaskListener listenerToNotify = null;
        synchronized (this) {
            if (cancelTimer != null) {
                cancelTimer.cancel();
                cancelTimer = null;
            }
            if (currentTask == runningLongTask) {
                currentTask = null;
            }
            if (notifyListener && listener != null) {
                listenerToNotify = listener;
            }
        }
        // Notified outside the monitor: the listener can run arbitrary code (e.g. Swing
        // updates triggered synchronously from a background thread), and holding the lock
        // here would block other threads calling synchronized methods on this executor,
        // such as cancel() from the EDT's Stop button, for as long as that code takes.
        if (listenerToNotify != null) {
            listenerToNotify.taskFinished(runningLongTask.task);
        }
    }

    /**
     * Inner class for associating a task to its Future instance
     */
    protected class RunningLongTask<V> implements Callable<V> {

        private final LongTask task;
        private final Runnable runnable;
        private final Callable<V> callable;
        private final LongTaskErrorHandler errorHandler;
        private final AtomicBoolean finishClaimed = new AtomicBoolean();
        private Future<V> future;
        private ProgressTicket progress;

        public RunningLongTask(LongTask task, Runnable runnable, String taskName, LongTaskErrorHandler errorHandler) {
            this.task = task;
            this.runnable = runnable;
            this.callable = null;
            this.errorHandler = errorHandler;
            init(taskName);
        }

        public RunningLongTask(LongTask task, Callable<V> callable, String taskName, LongTaskErrorHandler errorHandler) {
            this.task = task;
            this.runnable = null;
            this.callable = callable;
            this.errorHandler = errorHandler;
            init(taskName);
        }

        private void init(String taskName) {
            ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
            if (progressProvider != null) {
                this.progress = progressProvider.createTicket(taskName, () -> {
                    LongTaskExecutor.this.cancel();
                    return true;
                });
                if (task != null) {
                    task.setProgressTicket(progress);
                }
            }
        }

        @Override
        public V call() {
            if (task != null && progress != null) {
                progress.start();
            }
            currentTask = this;
            V result = null;
            try {
                if (runnable != null) {
                    runnable.run();
                } else if (callable != null) {
                    result = callable.call();
                    if (!inBackground) {
                        future = CompletableFuture.completedFuture(result);
                    }
                }

                if (claimFinish()) {
                    finished(this, true);
                }
                if (progress != null) {
                    progress.finish();
                }
            } catch (Throwable e) {
                LongTaskErrorHandler err = errorHandler;
                if (progress != null) {
                    progress.finish();
                }
                try {
                    if (err != null) {
                        err.fatalError(e);
                    } else if (defaultErrorHandler != null) {
                        defaultErrorHandler.fatalError(e);
                    } else {
                        Logger.getLogger("").log(Level.SEVERE, "", e);
                    }
                } finally {
                    // Failures are reported through the error handler and the listener's
                    // fatalError(), not through the listener's taskFinished(), but the task
                    // still has to be completed so the executor doesn't stay 'running'. When
                    // the task has been interrupted by the cancel timer the completion is
                    // already claimed by it and this whole block is a no-op: the timer has
                    // then already notified the listener itself.
                    if (claimFinish()) {
                        finished(this, false);
                        LongTaskListener lst = listener;
                        if (lst != null) {
                            // In addition to, and not instead of, the error handler above.
                            // Called after finished() so a failing handler or listener can't
                            // leave the executor 'running' forever
                            lst.fatalError(e);
                        }
                    }
                }
                if (!inBackground) {
                    future = CompletableFuture.failedFuture(e);
                } else if (callable != null) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException(e);
                }
            }

            return result;
        }

        public boolean cancel() {
            if (task != null) {
                return task.cancel();
            }
            return false;
        }

        /**
         * Claims the completion of this task. Returns <code>true</code> for the
         * first caller only, so that the task thread and the interrupt timer
         * can't both complete the same task.
         *
         * @return <code>true</code> if the caller is responsible for completing
         * this task, <code>false</code> if it has already been completed
         */
        private boolean claimFinish() {
            return finishClaimed.compareAndSet(false, true);
        }
    }

    /**
     * Inner class for naming the executor service thread
     */
    private class NamedThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }

    private class InterruptTimerTask extends TimerTask {

        private final RunningLongTask task;

        public InterruptTimerTask(RunningLongTask runningLongTask) {
            this.task = runningLongTask;
        }

        @Override
        public void run() {
            if (task != null) {
                // Claim the completion before interrupting the task thread, otherwise
                // that thread may complete the task (and make isRunning() false) while
                // the listener hasn't been notified yet.
                boolean claimed = task.claimFinish();
                try {
                    if (task.future != null) {
                        task.future.cancel(interruptCancel);
                    }
                    if (task.progress != null) {
                        task.progress.finish();
                    }
                } finally {
                    // The completion is claimed above, so it has to be performed here even
                    // if the cancellation failed, otherwise no other thread can complete
                    // the task anymore.
                    if (claimed) {
                        finished(task, true);
                    }
                }

                if (!inBackground) {
                    Logger.getLogger("").warning("Task from " + name + " did not respond to cancellation request. Interrupting thread.");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
