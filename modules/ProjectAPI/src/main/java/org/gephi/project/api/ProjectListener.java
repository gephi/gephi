package org.gephi.project.api;

import java.util.EventListener;

/**
 * Project listener.
 */
public interface ProjectListener extends EventListener {

    void lock();

    /**
     * Called when a project was successfully saved.
     * @param project project that was saved
     */
    void saved(Project project);

    /**
     * Called when a project was successfully opened.
     * @param project project that was opened
     */
    void opened(Project project);

    /**
     * Called when an error occurred in project manipulation.
     * @param project project that was manipulated, could be <code>null</code>
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
