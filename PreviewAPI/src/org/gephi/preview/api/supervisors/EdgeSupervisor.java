package org.gephi.preview.api.supervisors;

import java.awt.Font;
import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeColorizer;

/**
 * Edge supervisor.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface EdgeSupervisor extends Supervisor {

    /**
     * Returns true if the edges must be displayed in the preview.
     *
     * @return true if the edges must be displayed in the preview
     */
    public Boolean getShowFlag();

    /**
     * Returns true if the edges are curved.
     * 
     * @return true if the edges are curved
     */
    public Boolean getCurvedFlag();

    /**
     * Defines if the edges are curved.
     *
     * @param value  true for curved edges
     */
    public void setCurvedFlag(Boolean value);

    /**
     * Returns the edge colorizer.
     *
     * @return the edge colorizer
     */
    public EdgeColorizer getColorizer();

    /**
     * Defines the edge colorizer.
     *
     * @param value  the edge colorizer to set
     */
    public void setColorizer(EdgeColorizer value);

    /**
     * Returns true if the edge labels must be displayed in the preview.
     *
     * @return true if the edge labels must be displayed in the preview
     */
    public Boolean getShowLabelsFlag();

    /**
     * Defines if the edge labels must be displayed in the preview.
     *
     * @param value  true to display the edge labels in the preview
     */
    public void setShowLabelsFlag(Boolean value);

    /**
     * Returns the edge label font.
     *
     * @return the edge label font
     */
    public Font getLabelFont();

    /**
     * Defines the edge label font.
     *
     * @param value  the edge label font to set
     */
    public void setLabelFont(Font value);

    /**
     * Returns the edge label character limit.
     *
     * @return the edge label character limit
     */
    public Integer getLabelMaxChar();

    /**
     * Defines the edge label character limit.
     *
     * @param value  the edge label character limit to set
     */
    public void setLabelMaxChar(Integer value);

    /**
     * Returns the edge label colorizer.
     *
     * @return the edge label colorizer
     */
    public EdgeChildColorizer getLabelColorizer();

    /**
     * Defines the edge label colorizer.
     *
     * @param value  the edge label colorizer to set
     */
    public void setLabelColorizer(EdgeChildColorizer value);

    /**
     * Returns true if the edge mini-labels must be displayed in the preview.
     *
     * @return true if the edge mini-labels must be displayed in the preview.
     */
    public Boolean getShowMiniLabelsFlag();

    /**
     * Defines if the edge mini-labels must be displayed in the preview.
     *
     * @param value  true to display the edge mini-labels in the preview
     */
    public void setShowMiniLabelsFlag(Boolean value);

    /**
     * Returns the edge mini-label font.
     *
     * @return the edge mini-label font
     */
    public Font getMiniLabelFont();

    /**
     * Defines the edge mini-label font.
     *
     * @param value  the edge mini-label font to set
     */
    public void setMiniLabelFont(Font value);

    /**
     * Returns the edge mini-label character limit.
     *
     * @return the edge mini-label character limit
     */
    public Integer getMiniLabelMaxChar();

    /**
     * Defines the edge mini-label character limit.
     *
     * @param value  the edge mini-label character limit
     */
    public void setMiniLabelMaxChar(Integer value);

    /**
     * Returns the edge mini-label added radius.
     *
     * @return the edge mini-label added radius
     */
    public Float getMiniLabelAddedRadius();

    /**
     * Defines the edge mini-label added radius.
     *
     * @param value  the edge mini-label added radius to set
     */
    public void setMiniLabelAddedRadius(Float value);

    /**
     * Returns the edge mini-label colorizer.
     *
     * @return the edge mini-label colorizer
     */
    public EdgeChildColorizer getMiniLabelColorizer();

    /**
     * Defines the edge mini-label colorizer.
     *
     * @param value  the edge mini-label colorizer to set
     */
    public void setMiniLabelColorizer(EdgeChildColorizer value);

    /**
     * Returns true if the edge arrows must be displayed in the preview.
     *
     * @return true if the edge arrows must be displayed in the preview
     */
    public Boolean getShowArrowsFlag();

    /**
     * Defines if the edge arrows must be displayed in the preview.
     *
     * @param value  true to display the edge arrows in the preview
     */
    public void setShowArrowsFlag(Boolean value);

    /**
     * Returns the edge arrow added radius.
     *
     * @return the edge arrow added radius
     */
    public Float getArrowAddedRadius();

    /**
     * Defines the edge arrow added radius.
     *
     * @param value  the edge arrow added radius to set
     */
    public void setArrowAddedRadius(Float value);

    /**
     * Returns the edge arrow size.
     *
     * @return the edge arrow size
     */
    public Float getArrowSize();

    /**
     * Defines the edge arrow size.
     *
     * @param value  the edge arrow size to set
     */
    public void setArrowSize(Float value);

    /**
     * Returns the edge arrow colorizer.
     *
     * @return the edge arrow colorizer
     */
    public EdgeChildColorizer getArrowColorizer();

    /**
     * Defines the edge arrow colorizer.
     *
     * @param value  the edge arrow colorizer to set
     */
    public void setArrowColorizer(EdgeChildColorizer value);
}
