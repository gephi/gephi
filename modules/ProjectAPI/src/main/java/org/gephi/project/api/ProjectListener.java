package org.gephi.project.api;

public interface ProjectListener {

    void startSaving(Project project);

    void endSaving(Project project);

    void savingError(Project project, Throwable throwable);

    void startLoading(Project project);

    void endLoading(Project project);

    void loadingError(Project project, Throwable throwable);

    void closed(Project project);

    //Renamed?
}
