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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private boolean interruptCancel;
    private final long interruptDelay;
    private final String name;
    private RunningLongTask runningTask;
    private ExecutorService executor;
    private Timer cancelTimer;
    private LongTaskListener listener;
    private LongTaskErrorHandler errorHandler;
    private LongTaskErrorHandler defaultErrorHandler;

    /**
     * Creates a new long task executor.
     * @param doInBackground when <code>true</code>, the task will be executed in a separate thread
     * @param name the name of the executor, used to recognize threads by names
     * @param interruptDelay number of seconds to wait before calling <code>Thread.interrupt()</code> after a cancel request
     */
    public LongTaskExecutor(boolean doInBackground, String name, int interruptDelay) {
        this.inBackground = doInBackground;
        this.name = name;
        this.interruptCancel = true;
        this.interruptDelay = interruptDelay * 1000;
    }

    /**
     * Creates a new long task executor.
     * @param doInBackground doInBackground when <code>true</code>, the task will be executed in a separate thread
     * @param name the name of the executor, used to recognize threads by names
     */
    public LongTaskExecutor(boolean doInBackground, String name) {
        this(doInBackground, name, 0);
        this.interruptCancel = false;
    }

    /**
     * Creates a new long task executor.
     * @param doInBackground doInBackground when <code>true</code>, the task will be executed in a separate thread
     */
    public LongTaskExecutor(boolean doInBackground) {
        this(doInBackground, "LongTaskExecutor");
    }

    /**
     * Execute a long task with cancel and progress support. Task can be <code>null</code>.
     * In this case <code>runnable</code> will be executed normally, but without
     * cancel and progress support.
     * @param task the task to be executed, can be <code>null</code>.
     * @param runnable the runnable to be executed
     * @param taskName the name of the task, is displayed in the status bar if available
     * @param errorHandler error handler for exception retrieval during execution
     * @throws NullPointerException if <code>runnable</code> or <code>taskName</code> is null
     * @throws IllegalStateException if a task is still executing at this time
     */
    public void execute(LongTask task, final Runnable runnable, String taskName, LongTaskErrorHandler errorHandler) {
        if (runnable == null || taskName == null) {
            throw new NullPointerException();
        }
        if (runningTask != null) {
            throw new IllegalStateException("A task is still executing");
        }
        if (executor == null) {
            this.executor = new ThreadPoolExecutor(0, 1, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory());
        }
        if (errorHandler != null) {
            this.errorHandler = errorHandler;
        }
        runningTask = new RunningLongTask(task, runnable, taskName);
        if (inBackground) {
            runningTask.future = executor.submit(runningTask);
        } else {
            runningTask.run();
        }
    }

    /**
     * Execute a long task with cancel and progress support. Task can be <code>null</code>.
     * In this case <code>runnable</code> will be executed normally, but without
     * cancel and progress support.
     * @param task the task to be executed, can be <code>null</code>.
     * @param runnable the runnable to be executed
     * @throws NullPointerException if <code>runnable</code> is null
     * @throws IllegalStateException if a task is still executing at this time
     */
    public void execute(LongTask task, Runnable runnable) {
        execute(task, runnable, "", null);
    }

    /**
     * Cancel the current task. If the task fails to cancel itself and if an <code>interruptDelay</code> has been specified,
     * the task will be <b>interrupted</b> after <code>interruptDelay</code>. Using <code>Thread.interrupt()</code> may cause
     * hazardous behaviours and should be avoided. Therefore any task should be cancellable.
     */
    public synchronized void cancel() {
        if (runningTask != null) {
            if (runningTask.isCancellable()) {
                if (interruptCancel) {
                    if (!runningTask.cancel()) {
                        cancelTimer = new Timer(name + "_cancelTimer");
                        cancelTimer.schedule(new InterruptTimerTask(), interruptDelay);
                    }
                } else {
                    runningTask.cancel();
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if the executor is executing a task.
     * @return <code>true</code> if a task is running, <code>false</code> otherwise
     */
    public boolean isRunning() {
        return runningTask != null;
    }

    /**
     * Set the listener to this executor. Only a unique listener can be set to this executor. The listener
     * is called when the task terminates normally.
     * @param listener a listener for this executor
     */
    public void setLongTaskListener(LongTaskListener listener) {
        this.listener = listener;
    }

    /**
     * Set the default error handler. Use error handlers to get errors and exceptions thrown during
     * tasks execution.
     * @param errorHandler the default error handler
     */
    public void setDefaultErrorHandler(LongTaskErrorHandler errorHandler) {
        if (errorHandler != null) {
            this.defaultErrorHandler = errorHandler;
        }
    }

    private synchronized void finished() {
        if (cancelTimer != null) {
            cancelTimer.cancel();
        }
        LongTask task = runningTask.task;
        runningTask = null;
        errorHandler = null;
        if (listener != null) {
            listener.taskFinished(task);
        }
    }

    /**
     * Inner class for associating a task to its Future instance
     */
    private class RunningLongTask implements Runnable {

        private final LongTask task;
        private final Runnable runnable;
        private Future future;
        private ProgressTicket progress;

        public RunningLongTask(LongTask task, Runnable runnable, String taskName) {
            this.task = task;
            this.runnable = runnable;
            ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
            if (progressProvider != null) {
                this.progress = progressProvider.createTicket(taskName, new Cancellable() {

                    public boolean cancel() {
                        LongTaskExecutor.this.cancel();
                        return true;
                    }
                });
                if (task != null) {
                    task.setProgressTicket(progress);
                } else {
                    progress.start();
                }
            }
        }

        public void run() {
            try {
                runnable.run();
            } catch (Exception e) {
                LongTaskErrorHandler err = errorHandler;
                finished();
                if (progress != null) {
                    progress.finish();
                }
                if (err != null) {
                    err.fatalError(e);
                } else if (defaultErrorHandler != null) {
                    defaultErrorHandler.fatalError(e);
                } else {
                    Logger.getLogger("").log(Level.SEVERE, "", e);
                }
            }

            finished();
            if (progress != null) {
                progress.finish();
            }
        }

        public boolean cancel() {
            /*if (inBackground) {
            if (future != null && future.cancel(false)) {
            return true;
            }
            }*/
            if (task != null) {
                return task.cancel();
            }
            return false;
        }

        public boolean isCancellable() {
            if (inBackground) {
                if (!future.isCancelled()) {
                    return true;
                }
                return false;
            }
            return true;
        }
    }

    /**
     * Inner class for naming the executor service thread
     */
    private class NamedThreadFactory implements ThreadFactory {

        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }

    private class InterruptTimerTask extends TimerTask {

        @Override
        public void run() {
            if (runningTask != null) {
                System.out.println("Interrupt task");
                cancelTimer = null;
                if (runningTask.progress != null) {
                    runningTask.progress.finish();
                }
                finished();
                executor.shutdownNow();
                executor = null;
            }
        }
    }
}
