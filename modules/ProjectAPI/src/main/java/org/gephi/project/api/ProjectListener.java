package org.gephi.project.api;

import java.util.EventListener;

/**
 * Project listener.
 * <p>
 * <b>Threading:</b> all the methods below are invoked synchronously, on the thread that is performing
 * the project operation. That thread is never the Event Dispatch Thread, and two callbacks may well
 * arrive on two different threads, so any state shared with other threads must be volatile, atomic or
 * otherwise guarded.
 * <p>
 * Implementations must post any user interface work with <code>SwingUtilities.invokeLater</code> and
 * return promptly: they run inside the operation and delay it. In particular they must never call
 * <code>SwingUtilities.invokeAndWait</code>, or wait on the Event Dispatch Thread in any other way,
 * as that deadlocks whenever the EDT is itself waiting for the operation.
 */
public interface ProjectListener extends EventListener {

    /**
     * Called when a long project operation starts, so clients can disable what must not be used while
     * it runs. Toggling Swing components has to be deferred to the Event Dispatch Thread.
     */
    void lock();

    /**
     * Called when an operation ends without any other outcome being reported, for instance because the
     * user cancelled it.
     */
    void unlock();

    /**
     * Called when a project was successfully saved.
     *
     * @param project project that was saved
     */
    void saved(Project project);

    /**
     * Called when a project was successfully opened.
     *
     * @param project project that was opened
     */
    void opened(Project project);

    /**
     * Called when an error occurred in project manipulation.
     *
     * @param project   project that was manipulated, could be <code>null</code>
     * @param throwable error that occurred
     */
    void error(Project project, Throwable throwable);

    /**
     * Called when a project was closed.
     *
     * @param project project that was closed
     */
    void closed(Project project);

    /**
     * Called when a project was changer, for instance renamed.
     *
     * @param project project that was changed
     */
    void changed(Project project);
}
