/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.objects;

import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.compatibility.CompatibilityObject3dClass;

/**
 *
 * @author Mathieu Bastian
 */
public interface Object3dClassLibrary {

    public CompatibilityObject3dClass[] createObjectClassesCompatibility(AbstractEngine engine);
}
