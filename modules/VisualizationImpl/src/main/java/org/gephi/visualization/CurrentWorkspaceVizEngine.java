package org.gephi.visualization;

import java.util.Optional;

import com.jogamp.newt.event.NEWTEvent;
import org.gephi.project.api.Workspace;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;

public interface CurrentWorkspaceVizEngine {

    Optional<VizEngine<JOGLRenderingTarget, NEWTEvent>> getEngine();

    Optional<VizEngine<JOGLRenderingTarget, NEWTEvent>> getEngine(Workspace workspace);
}
