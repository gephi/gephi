package org.gephi.visualization;

import java.util.Optional;

import com.jogamp.newt.event.NEWTEvent;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = CurrentWorkspaceVizEngine.class)
public class CurrentWorkspaceVizEngineImpl implements CurrentWorkspaceVizEngine {
    private Workspace currentWorkspace = null;

    public CurrentWorkspaceVizEngineImpl() {
        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {

            }

            @Override
            public void select(Workspace workspace) {
                currentWorkspace = workspace;
            }

            @Override
            public void unselect(Workspace workspace) {
                currentWorkspace = null;
            }

            @Override
            public void close(Workspace workspace) {
                currentWorkspace = null;
            }

            @Override
            public void disable() {
                currentWorkspace = null;
            }
        });
    }

    @Override
    public Optional<VizEngine<JOGLRenderingTarget, NEWTEvent>> getEngine() {
        return getEngine(currentWorkspace);
    }

    @Override
    public Optional<VizEngine<JOGLRenderingTarget, NEWTEvent>> getEngine(Workspace workspace) {
        if (workspace == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
            workspace.getLookup().lookup(VizEngine.class)
        );
    }
}
