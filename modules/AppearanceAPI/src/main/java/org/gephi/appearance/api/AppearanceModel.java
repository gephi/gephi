/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.api;

import org.gephi.project.api.Workspace;

/**
 *
 * @author mbastian
 */
public interface AppearanceModel {

    /**
     * Returns the current interpolator. The default interpolator is a simple
     * linear interpolation.
     *
     * @return the current interpolator
     */
    public Interpolator getInterpolator();

    /**
     * Return the workspace this model is associated with
     *
     * @return the workspace of this model
     */
    public Workspace getWorkspace();

    /**
     * Returns
     * <code>true</code> if rankings are using the currently visible graph as a
     * scale. If
     * <code>false</code> the complete graph is used to determine minimum and
     * maximum values, the ranking scale.
     *
     * @return <code>true</code> if using a local scale, <code>false</code> if
     * global scale
     */
    public boolean useLocalScale();
}
