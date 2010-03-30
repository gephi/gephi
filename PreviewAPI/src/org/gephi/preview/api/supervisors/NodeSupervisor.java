package org.gephi.preview.api.supervisors;

import java.awt.Font;
import org.gephi.preview.api.GenericColorizer;
import org.gephi.preview.api.NodeChildColorizer;
import org.gephi.preview.api.NodeColorizer;

/**
 * Node supervisor.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface NodeSupervisor extends Supervisor {

    /**
     * Returns true if the nodes must be displayed in the preview.
     *
     * @return true if the nodes must be displayed in the preview
     */
    public Boolean getShowNodes();

    /**
     * Defines if the nodes must be displayed in the preview.
     *
     * @param value  true to display the nodes in the preview
     */
    public void setShowNodes(Boolean value);

    /**
     * Returns the node border width.
     *
     * @return the node border width
     */
    public Float getNodeBorderWidth();

    /**
     * Defines the node border width.
     *
     * @param value  the node border width to set
     */
    public void setNodeBorderWidth(Float value);

    /**
     * Returns the node colorizer.
     *
     * @return the node colorizer
     */
    public NodeColorizer getNodeColorizer();

    /**
     * Defines the node colorizer.
     *
     * @param value  the node colorizer to set
     */
    public void setNodeColorizer(NodeColorizer value);

    /**
     * Returns the node border colorizer.
     *
     * @return the node border colorizer
     */
    public GenericColorizer getNodeBorderColorizer();

    /**
     * Defines the node border colorizer.
     *
     * @param value  the node border colorizer to set
     */
    public void setNodeBorderColorizer(GenericColorizer value);

    /**
     * Returns true if the node labels must be displayed in the preview.
     *
     * @return true if the node labels must be displayed in the preview
     */
    public Boolean getShowNodeLabels();

    /**
     * Defines if the node labels must be displayed in the preview.
     *
     * @param value  true to display the node labels in the preview
     */
    public void setShowNodeLabels(Boolean value);

    /**
     * Returns the base node label font.
     *
     * @return the base node label font
     */
    public Font getBaseNodeLabelFont();

    /**
     * Defines the base node label font.
     *
     * @param value  the base node label font to set
     */
    public void setBaseNodeLabelFont(Font value);

    /**
     * Returns the node label character limit.
     *
     * @return the node label character limit
     */
    public Integer getNodeLabelMaxChar();

    /**
     * Defines the node label character limit.
     *
     * @param value  the node label character limit to set
     */
    public void setNodeLabelMaxChar(Integer value);

    /**
     * Returns the node label colorizer.
     *
     * @return the node label colorizer
     */
    public NodeChildColorizer getNodeLabelColorizer();

    /**
     * Defines the node label colorizer.
     *
     * @param value  the node label colorizer to set
     */
    public void setNodeLabelColorizer(NodeChildColorizer value);

    /**
     * Returns true if the node label borders must be displayed in the preview.
     *
     * @return true if the node label borders must be displayed in the preview
     */
    public Boolean getShowNodeLabelBorders();

    /**
     * Defines if the node label borders must be displayed in the preview.
     *
     * @param value  true to display the node label borders in the preview
     */
    public void setShowNodeLabelBorders(Boolean value);

    /**
     * Returns the node label border colorizer.
     *
     * @return the node label border colorizer
     */
    public NodeChildColorizer getNodeLabelBorderColorizer();

    /**
     * Defines the node label border colorizer.
     *
     * @param value  the node label border colorizer to set
     */
    public void setNodeLabelBorderColorizer(NodeChildColorizer value);

    /**
     * Returns true if label size takes node size in account
     *
     * @return true if label size takes node size in account
     */
    public Boolean getProportionalLabelSize();

    /**
     * Defines if label size takes node size in account
     * 
     * @param proportionnalLabelSize true to set proportionnal label size
     */
    public void setProportionalLabelSize(Boolean proportionnalLabelSize);
}
