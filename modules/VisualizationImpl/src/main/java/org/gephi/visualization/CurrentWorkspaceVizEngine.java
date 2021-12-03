package org.gephi.visualization;

import java.util.Optional;
import org.gephi.project.api.Workspace;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.lwjgl.LWJGLRenderingTarget;
import org.gephi.viz.engine.lwjgl.pipeline.events.LWJGLInputEvent;

public interface CurrentWorkspaceVizEngine {

    Optional<VizEngine<LWJGLRenderingTarget, LWJGLInputEvent>> getEngine();

    Optional<VizEngine<LWJGLRenderingTarget, LWJGLInputEvent>> getEngine(Workspace workspace);
}
