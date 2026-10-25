package org.gephi.desktop.visualization.actions;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jogamp.newt.event.NEWTEvent;
import java.awt.event.ActionEvent;
import java.util.Optional;
import javax.swing.Action;
import org.gephi.graph.api.Rect2D;
import org.gephi.visualization.VizController;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.joml.Vector2f;
import org.junit.Assert;
import org.junit.Test;

public class GraphViewActionTest {

    @Test
    public void testDisabledWithoutEngine() {
        for (Action action : new Action[] {new ZoomIn(), new ZoomOut(), new CenterOnGraph()}) {
            Assert.assertFalse(action.isEnabled());
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        }
    }

    @Test
    public void testZoomInAndOutAroundCenter() {
        final VizEngine<JOGLRenderingTarget, NEWTEvent> engine = mockEngine(2f);
        final VizController controller = mockController(engine);

        new ZoomIn().perform(controller);
        verify(engine).screenCoordinatesToWorldCoordinates(400, 300);
        verify(engine).setZoom(eq(2f * 1.331f, 1e-4f));

        new ZoomOut().perform(controller);
        verify(engine).setZoom(eq(2f / 1.331f, 1e-4f));
    }

    @Test
    public void testZoomDoesNothingBeforeTheEngineHasASize() {
        final VizEngine<JOGLRenderingTarget, NEWTEvent> engine = mockEngine(2f);
        when(engine.getWidth()).thenReturn(0);
        when(engine.getHeight()).thenReturn(0);
        final VizController controller = mockController(engine);

        new ZoomIn().perform(controller);
        new ZoomOut().perform(controller);

        verify(engine, never()).setZoom(anyFloat());
        verify(engine, never()).screenCoordinatesToWorldCoordinates(anyInt(), anyInt());
    }

    @Test
    public void testCenterOnGraph() {
        final VizController controller = mockController(mockEngine(1f));

        new CenterOnGraph().perform(controller);
        verify(controller).centerOnGraph();
    }

    @SuppressWarnings("unchecked")
    private static VizEngine<JOGLRenderingTarget, NEWTEvent> mockEngine(float zoom) {
        final VizEngine<JOGLRenderingTarget, NEWTEvent> engine = mock(VizEngine.class);
        when(engine.getZoom()).thenReturn(zoom);
        when(engine.getWidth()).thenReturn(800);
        when(engine.getHeight()).thenReturn(600);
        when(engine.getViewBoundaries()).thenReturn(new Rect2D(0, 0, 10, 10));
        when(engine.screenCoordinatesToWorldCoordinates(anyInt(), anyInt())).thenReturn(new Vector2f(5, 5));
        return engine;
    }

    private static VizController mockController(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        final VizController controller = mock(VizController.class);
        when(controller.getEngine()).thenReturn(Optional.of(engine));
        return controller;
    }
}
