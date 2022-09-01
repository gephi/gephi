package org.gephi.project.impl;

import org.gephi.project.api.ProjectListener;
import org.gephi.utils.longtask.spi.LongTask;

public interface ProjectTask extends LongTask, Runnable {

    void onStart(ProjectListener projectListener);

    void onSuccess(ProjectListener projectListener);

    void onError(ProjectListener projectListener, Throwable throwable);
}
