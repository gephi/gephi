/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.bridge;

import org.gephi.graph.api.Object3d;

/**
 *
 * @author Mathieu Bastian
 */
public interface EventBridge {

    public void initEvents();
    public void initArchitecture();
    public void mouseClick(Object3d[] clickedObjects);
}
