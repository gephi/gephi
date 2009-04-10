/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.api.objects;

import org.gephi.visualization.api.objects.CompatibilityObject3dClass;
import org.gephi.visualization.opengl.AbstractEngine;

/**
 *
 * @author Mathieu Bastian
 */
public interface Object3dClassLibrary {

    public CompatibilityObject3dClass[] createObjectClassesCompatibility(AbstractEngine engine);
}
