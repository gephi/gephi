package org.gephi.preview.api.supervisor;

import java.awt.Font;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizer;

/**
 * Node supervisor.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface NodeSupervisor {

    public Boolean getShowNodes();

    public void setShowNodes(Boolean value);

    public Float getNodeBorderWidth();

    public void setNodeBorderWidth(Float value);

    public NodeColorizer getNodeColorizer();

    public void setNodeColorizer(NodeColorizer value);

    public GenericColorizer getNodeBorderColorizer();

    public void setNodeBorderColorizer(GenericColorizer value);

    public Boolean getShowNodeLabels();

    public void setShowNodeLabels(Boolean value);

    public Font getNodeLabelFont();

    public void setNodeLabelFont(Font value);

    public Integer getNodeLabelMaxChar();

    public void setNodeLabelMaxChar(Integer value);

    public NodeChildColorizer getNodeLabelColorizer();

    public void setNodeLabelColorizer(NodeChildColorizer value);

    public Boolean getShowNodeLabelBorders();

    public void setShowNodeLabelBorders(Boolean value);

    public NodeChildColorizer getNodeLabelBorderColorizer();

    public void setNodeLabelBorderColorizer(NodeChildColorizer value);
}
