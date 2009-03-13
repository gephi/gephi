/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gephi.visualization.objects;

import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.compatibility.CompatibilityObject3dClass;

/**
 *
 * @author Mathieu
 */
public interface Object3dClassLibrary {
    public CompatibilityObject3dClass[] createObjectClassesCompatibility(AbstractEngine engine);
}
