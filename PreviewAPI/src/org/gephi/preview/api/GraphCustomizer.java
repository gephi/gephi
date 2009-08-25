package org.gephi.preview.api;

import java.awt.Font;
import java.beans.PropertyChangeListener;

/**
 *
 * @author jeremy
 */
public interface GraphCustomizer {

    public void addPropertyChangeListener(PropertyChangeListener pcl);
    public void removePropertyChangeListener(PropertyChangeListener pcl);

    public Boolean getShowNodes();
    public Float getNodeBorderWidth();
    public String getNodeColor();
    public String getNodeBorderColor();
    public Boolean getShowNodeLabels();
    public Font getNodeLabelFont();
    public Integer getNodeLabelMaxChar();
    public String getNodeLabelColor();
    public Boolean getShowNodeLabelBorders();
    public String getNodeLabelBorderColor();
    public Boolean getShowEdges();
    public Boolean getCurvedUniEdges();
    public Boolean getCurvedBiEdges();
    public String getUniEdgeColor();
    public String getBiEdgeColor();
    public Boolean getShowSelfLoops();
    public String getSelfLoopColor();
    public Boolean getShowUniEdgeLabels();
    public Integer getUniEdgeLabelMaxChar();
    public Font getUniEdgeLabelFont();
    public String getUniEdgeLabelColor();
    public Boolean getShowUniEdgeMiniLabels();
    public Float getUniEdgeMiniLabelAddedRadius();
    public Integer getUniEdgeMiniLabelMaxChar();
    public Font getUniEdgeMiniLabelFont();
    public String getUniEdgeMiniLabelColor();
    public Boolean getShowUniEdgeArrows();
    public Float getUniEdgeArrowAddedRadius();
    public Float getUniEdgeArrowSize();
    public String getUniEdgeArrowColor();
    public Boolean getShowBiEdgeLabels();
    public Integer getBiEdgeLabelMaxChar();
    public Font getBiEdgeLabelFont();
    public String getBiEdgeLabelColor();
    public Boolean getShowBiEdgeMiniLabels();
    public Float getBiEdgeMiniLabelAddedRadius();
    public Integer getBiEdgeMiniLabelMaxChar();
    public Font getBiEdgeMiniLabelFont();
    public String getBiEdgeMiniLabelColor();
    public Boolean getShowBiEdgeArrows();
    public Float getBiEdgeArrowAddedRadius();
    public Float getBiEdgeArrowSize();
    public String getBiEdgeArrowColor();

    public void setShowNodes(Boolean value);
    public void setNodeBorderWidth(Float value);
    public void setNodeColor(String value);
    public void setNodeBorderColor(String value);
    public void setShowNodeLabels(Boolean value);
    public void setNodeLabelFont(Font value);
    public void setNodeLabelMaxChar(Integer value);
    public void setNodeLabelColor(String value);
    public void setShowNodeLabelBorders(Boolean value);
    public void setNodeLabelBorderColor(String value);
    public void setShowEdges(Boolean value);
    public void setCurvedUniEdges(Boolean value);
    public void setCurvedBiEdges(Boolean value);
    public void setUniEdgeColor(String value);
    public void setBiEdgeColor(String value);
    public void setShowSelfLoops(Boolean value);
    public void setSelfLoopColor(String value);
    public void setShowUniEdgeLabels(Boolean value);
    public void setUniEdgeLabelMaxChar(Integer value);
    public void setUniEdgeLabelFont(Font value);
    public void setUniEdgeLabelColor(String value);
    public void setShowUniEdgeMiniLabels(Boolean value);
    public void setUniEdgeMiniLabelAddedRadius(Float value);
    public void setUniEdgeMiniLabelMaxChar(Integer value);
    public void setUniEdgeMiniLabelFont(Font value);
    public void setUniEdgeMiniLabelColor(String value);
    public void setShowUniEdgeArrows(Boolean value);
    public void setUniEdgeArrowAddedRadius(Float value);
    public void setUniEdgeArrowSize(Float value);
    public void setUniEdgeArrowColor(String value);
    public void setShowBiEdgeLabels(Boolean value);
    public void setBiEdgeLabelMaxChar(Integer value);
    public void setBiEdgeLabelFont(Font value);
    public void setBiEdgeLabelColor(String value);
    public void setShowBiEdgeMiniLabels(Boolean value);
    public void setBiEdgeMiniLabelAddedRadius(Float value);
    public void setBiEdgeMiniLabelMaxChar(Integer value);
    public void setBiEdgeMiniLabelFont(Font value);
    public void setBiEdgeMiniLabelColor(String value);
    public void setShowBiEdgeArrows(Boolean value);
    public void setBiEdgeArrowAddedRadius(Float value);
    public void setBiEdgeArrowSize(Float value);
    public void setBiEdgeArrowColor(String value);
}
