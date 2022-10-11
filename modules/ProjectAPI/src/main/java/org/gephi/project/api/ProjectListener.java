package org.gephi.project.api;

import java.util.EventListener;

public interface ProjectListener extends EventListener {

    void lock();

    void saved(Project project);

    void opened(Project project);

    void error(Project project, Throwable throwable);

    void closed(Project project);

    void changed(Project project);
}
