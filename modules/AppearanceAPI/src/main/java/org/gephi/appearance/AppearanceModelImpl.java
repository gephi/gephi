/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance;

import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Interpolator;
import org.gephi.project.api.Workspace;

/**
 *
 * @author mbastian
 */
public class AppearanceModelImpl implements AppearanceModel {

    private final Workspace workspace;
    private Interpolator interpolator;
    private boolean localScale = false;

    public AppearanceModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.interpolator = Interpolator.LINEAR;
    }

    @Override
    public Interpolator getInterpolator() {
        return interpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            throw new NullPointerException();
        }
        this.interpolator = interpolator;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean useLocalScale() {
        return localScale;
    }

    public void setLocalScale(boolean localScale) {
        this.localScale = localScale;
    }
}
