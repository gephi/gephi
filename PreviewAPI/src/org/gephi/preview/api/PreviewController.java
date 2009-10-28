package org.gephi.preview.api;

import java.awt.Font;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizer;
import org.gephi.preview.api.supervisor.EdgeSupervisor;
import org.gephi.preview.api.supervisor.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisor.SelfLoopSupervisor;

/**
 *
 * @author jeremy
 */
public interface PreviewController {

    public Graph getGraph();

    /**
	 * Returns the global edge supervisor.
	 *
	 * @return the controller's global edge supervisor
	 */
	public GlobalEdgeSupervisor getGlobalEdgeSupervisor();

    /**
	 * Returns the self-loop supervisor.
	 *
	 * @return the controller's self-loop supervisor
	 */
	public SelfLoopSupervisor getSelfLoopSupervisor();

    /**
	 * Returns the unidirectional edge supervisor.
	 *
	 * @return the controller's unidirectional edge supervisor
	 */
	public EdgeSupervisor getUniEdgeSupervisor();

    /**
	 * Returns the bidirectional edge supervisor.
	 *
	 * @return the controller's bidirectional edge supervisor
	 */
	public EdgeSupervisor getBiEdgeSupervisor();

    public Boolean getShowNodes();
    public Float getNodeBorderWidth();
    public NodeColorizer getNodeColorizer();
    public GenericColorizer getNodeBorderColorizer();
    public Boolean getShowNodeLabels();
    public Font getNodeLabelFont();
    public Integer getNodeLabelMaxChar();
    public NodeChildColorizer getNodeLabelColorizer();
    public Boolean getShowNodeLabelBorders();
    public NodeChildColorizer getNodeLabelBorderColorizer();
//    public Boolean getShowEdges();
//    public Boolean getCurvedUniEdges();
//    public Boolean getCurvedBiEdges();
//    public EdgeColorizer getUniEdgeColorizer();
//    public EdgeColorizer getBiEdgeColorizer();
//    public Boolean getShowUniEdgeLabels();
//    public Integer getUniEdgeLabelMaxChar();
//    public Font getUniEdgeLabelFont();
//    public EdgeChildColorizer getUniEdgeLabelColorizer();
//    public Boolean getShowUniEdgeMiniLabels();
//    public Float getUniEdgeMiniLabelAddedRadius();
//    public Integer getUniEdgeMiniLabelMaxChar();
//    public Font getUniEdgeMiniLabelFont();
//    public EdgeChildColorizer getUniEdgeMiniLabelColorizer();
//    public Boolean getShowUniEdgeArrows();
//    public Float getUniEdgeArrowAddedRadius();
//    public Float getUniEdgeArrowSize();
//    public EdgeChildColorizer getUniEdgeArrowColorizer();
//    public Boolean getShowBiEdgeLabels();
//    public Integer getBiEdgeLabelMaxChar();
//    public Font getBiEdgeLabelFont();
//    public EdgeChildColorizer getBiEdgeLabelColorizer();
//    public Boolean getShowBiEdgeMiniLabels();
//    public Float getBiEdgeMiniLabelAddedRadius();
//    public Integer getBiEdgeMiniLabelMaxChar();
//    public Font getBiEdgeMiniLabelFont();
//    public EdgeChildColorizer getBiEdgeMiniLabelColorizer();
//    public Boolean getShowBiEdgeArrows();
//    public Float getBiEdgeArrowAddedRadius();
//    public Float getBiEdgeArrowSize();
//    public EdgeChildColorizer getBiEdgeArrowColorizer();

    public void setShowNodes(Boolean value);
    public void setNodeBorderWidth(Float value);
    public void setNodeColorizer(NodeColorizer value);
    public void setNodeBorderColorizer(GenericColorizer value);
    public void setShowNodeLabels(Boolean value);
    public void setNodeLabelFont(Font value);
    public void setNodeLabelMaxChar(Integer value);
    public void setNodeLabelColorizer(NodeChildColorizer value);
    public void setShowNodeLabelBorders(Boolean value);
    public void setNodeLabelBorderColorizer(NodeChildColorizer value);
//    public void setShowEdges(Boolean value);
//    public void setCurvedUniEdges(Boolean value);
//    public void setCurvedBiEdges(Boolean value);
//    public void setUniEdgeColorizer(EdgeColorizer value);
//    public void setBiEdgeColorizer(EdgeColorizer value);
//    public void setShowUniEdgeLabels(Boolean value);
//    public void setUniEdgeLabelMaxChar(Integer value);
//    public void setUniEdgeLabelFont(Font value);
//    public void setUniEdgeLabelColorizer(EdgeChildColorizer value);
//    public void setShowUniEdgeMiniLabels(Boolean value);
//    public void setUniEdgeMiniLabelAddedRadius(Float value);
//    public void setUniEdgeMiniLabelMaxChar(Integer value);
//    public void setUniEdgeMiniLabelFont(Font value);
//    public void setUniEdgeMiniLabelColorizer(EdgeChildColorizer value);
//    public void setShowUniEdgeArrows(Boolean value);
//    public void setUniEdgeArrowAddedRadius(Float value);
//    public void setUniEdgeArrowSize(Float value);
//    public void setUniEdgeArrowColorizer(EdgeChildColorizer value);
//    public void setShowBiEdgeLabels(Boolean value);
//    public void setBiEdgeLabelMaxChar(Integer value);
//    public void setBiEdgeLabelFont(Font value);
//    public void setBiEdgeLabelColorizer(EdgeChildColorizer value);
//    public void setShowBiEdgeMiniLabels(Boolean value);
//    public void setBiEdgeMiniLabelAddedRadius(Float value);
//    public void setBiEdgeMiniLabelMaxChar(Integer value);
//    public void setBiEdgeMiniLabelFont(Font value);
//    public void setBiEdgeMiniLabelColorizer(EdgeChildColorizer value);
//    public void setShowBiEdgeArrows(Boolean value);
//    public void setBiEdgeArrowAddedRadius(Float value);
//    public void setBiEdgeArrowSize(Float value);
//    public void setBiEdgeArrowColorizer(EdgeChildColorizer value);
}
