/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.visualization.api;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 *
 * @author Mathieu
 */
public interface GraphIO extends MouseListener, MouseWheelListener, MouseMotionListener, KeyListener {

    public float[] getMousePosition();
    public float[] getMouseDrag();
    public void startMouseListening();
    public void stopMouseListening();
}
