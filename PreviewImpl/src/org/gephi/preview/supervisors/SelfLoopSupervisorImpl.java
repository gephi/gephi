package org.gephi.preview.supervisors;

import java.util.HashSet;
import java.util.Set;
import org.gephi.preview.SelfLoopImpl;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.SupervisorPropery;
import org.gephi.preview.api.supervisors.SelfLoopSupervisor;
import org.gephi.preview.propertyeditors.EdgeColorizerPropertyEditor;
import org.gephi.preview.updaters.CustomColorMode;

/**
 * Self-loop supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class SelfLoopSupervisorImpl implements SelfLoopSupervisor {

    //Properties
    private Boolean show;
    private EdgeColorizer colorizer;
    private Float edgeScale;
    //Architecture
    private final Set<SelfLoopImpl> supervisedSelfLoops = new HashSet<SelfLoopImpl>();

    public SelfLoopSupervisorImpl() {
        defaultValues();
    }

    public void defaultValues() {
        show = true;
        colorizer = new CustomColorMode(0, 0, 0);
        edgeScale = new Float(1f);
    }

    public Float getEdgeScale() {
        return edgeScale;
    }

    public void setEdgeScale(Float edgeScale) {
        this.edgeScale = edgeScale;
    }

    /**
     * Adds the given self-loop to the list of the supervised self-loops.
     *
     * It updates the self-loop with the supervisor's values.
     *
     * @param selfLoop  the self-loop to supervise
     */
    public void addSelfLoop(SelfLoopImpl selfLoop) {
        supervisedSelfLoops.add(selfLoop);

        colorSelfLoops();
    }

    /**
     * Clears the list of supervised self-loops.
     */
    public void clearSupervised() {
        supervisedSelfLoops.clear();
    }

    /**
     * Returns true if the self-loops must be displayed in the preview.
     *
     * @return true if the self-loops must be displayed in the preview
     */
    public Boolean getShowFlag() {
        return show;
    }

    /**
     * Defines if the self-loops must be displayed in the preview.
     *
     * @param value  true to display the self-loops in the preview
     */
    public void setShowFlag(Boolean value) {
        show = value;
    }

    /**
     * Returns the self-loop colorizer.
     *
     * @return the self-loop colorizer
     */
    public EdgeColorizer getColorizer() {
        return colorizer;
    }

    /**
     * Sets the self-loop colorizer.
     *
     * @param value  the self-loop colorizer to set
     */
    public void setColorizer(EdgeColorizer value) {
        colorizer = value;
        colorSelfLoops();
    }

    /**
     * Colors the given self-loop.
     *
     * @param selfLoop  the self-loop to color
     */
    private void colorSelfLoop(SelfLoopImpl selfLoop) {
        colorizer.color(selfLoop);
    }

    /**
     * Colors the supervised self-loops.
     */
    private void colorSelfLoops() {
        for (SelfLoopImpl sl : supervisedSelfLoops) {
            colorSelfLoop(sl);
        }
    }

    public SupervisorPropery[] getProperties() {
        final String CATEGORY = "Self-Loop";
        try {
            return new SupervisorPropery[]{
                        SupervisorPropery.createProperty(this, Boolean.class, "showFlag", CATEGORY, "Show"),
                        SupervisorPropery.createProperty(this, Float.class, "edgeScale", CATEGORY, "Thickness"),
                        SupervisorPropery.createProperty(this, EdgeColorizer.class, "colorizer", CATEGORY, "Color", EdgeColorizerPropertyEditor.class)};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SupervisorPropery[0];
    }
}
