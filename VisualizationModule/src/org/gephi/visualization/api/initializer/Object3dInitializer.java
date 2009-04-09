/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.api.initializer;

import javax.swing.JPanel;
import org.gephi.graph.api.Object3d;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.api.Object3dImpl;

/**
 *
 * @author Mathieu Bastian
 */
public interface Object3dInitializer {

    public Object3dImpl initObject(Renderable n);

    public String getName();

    public JPanel getPanel();

    @Override
    public String toString();
}
