/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.api;

import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.project.api.Workspace;

/**
 *
 * @author mbastian
 */
public interface AppearanceController {

    /**
     * Sets the interpolator to be used when transforming values. This is set to
     * the current model only. If the model is changed (i.e. switch workspace),
     * call this again.
     * <p>
     * Default interpolator implementations can be found in the
     * {@link Interpolator} class.
     *
     * @param interpolator the interpolator to use for transformation.
     */
    public void setInterpolator(Interpolator interpolator);

    /**
     * Sets whether rankings use a local or a global scale. When calculating the
     * minimum and maximum value (i.e. the scale) rankings can use the complete
     * graph or only the currently visible graph. When using the visible graph
     * it is called the <b>local</b> scale.
     *
     * @param useLocalScale <code>true</code> for local, <code>false</code> for
     * global
     */
    public void setUseLocalScale(boolean useLocalScale);

    public AppearanceModel getModel();

    public AppearanceModel getModel(Workspace workspace);

    public Transformer getTransformer(TransformerUI ui);
}
