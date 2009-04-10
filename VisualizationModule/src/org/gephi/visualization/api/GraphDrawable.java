/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.visualization.api;

import java.awt.Component;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import org.gephi.visualization.gleem.linalg.Vec3f;

/**
 *
 * @author Mathieu Bastian
 */
public interface GraphDrawable {
    public Component getGraphComponent();
    public int getViewportHeight();
    public int getViewportWidth();
    public float[] getCameraTarget();
    public float[] getCameraLocation();
    public Vec3f getCameraVector();
    public double getDraggingMarkerX();
    public double getDraggingMarkerY();
    public DoubleBuffer getProjectionMatrix();
    public IntBuffer getViewport();

}
