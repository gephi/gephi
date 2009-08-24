package org.gephi.preview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.gephi.preview.api.GraphCustomizer;

/**
 *
 * @author jeremy
 */
public class GraphCustomizerImpl implements GraphCustomizer {

    private Boolean showNodes = true;
    private Float nodeBorderWidth = 1f;
    private String nodeColor = "original";
    private String nodeBorderColor = "0,0,0";
    private Boolean showNodeLabels = true;
    private String nodeLabelFont = "Sans";
    private Integer nodeLabelFontSize = 14;
    private Integer nodeLabelMaxChar = 10;
    private String nodeLabelColor = "0,0,0";
    private Boolean showNodeLabelBorders = true;
    private String nodeLabelBorderColor = "255,255,255";
    private Boolean showEdges = true;
    private Boolean curvedUniEdges = false;
    private Boolean curvedBiEdges = false;
    private String uniEdgeColor = "b1";
    private String biEdgeColor = "both";
    private Boolean showSelfLoops = true;
    private String selfLoopColor = "0,0,0";
    private Boolean showUniEdgeLabels = true;
    private Integer uniEdgeLabelMaxChar = 10;
    private String uniEdgeLabelFont = "Sans";
    private Integer uniEdgeLabelFontSize = 10;
    private String uniEdgeLabelColor = "parent";
    private Boolean showUniEdgeMiniLabels = true;
    private Float uniEdgeMiniLabelAddedRadius = 15f;
    private Integer uniEdgeMiniLabelMaxChar = 10;
    private String uniEdgeMiniLabelFont = "Sans";
    private Integer uniEdgeMiniLabelFontSize = 8;
    private String uniEdgeMiniLabelColor = "parent";
    private Boolean showUniEdgeArrows = true;
    private Float uniEdgeArrowAddedRadius = 65f;
    private Float uniEdgeArrowSize = 20f;
    private String uniEdgeArrowColor = "parent";
    private Boolean showBiEdgeLabels = true;
    private Integer biEdgeLabelMaxChar = 10;
    private String biEdgeLabelFont = "Sans";
    private Integer biEdgeLabelFontSize = 10;
    private String biEdgeLabelColor = "parent";
    private Boolean showBiEdgeMiniLabels = true;
    private Float biEdgeMiniLabelAddedRadius = 15f;
    private Integer biEdgeMiniLabelMaxChar = 10;
    private String biEdgeMiniLabelFont = "Sans";
    private Integer biEdgeMiniLabelFontSize = 8;
    private String biEdgeMiniLabelColor = "parent";
    private Boolean showBiEdgeArrows = true;
    private Float biEdgeArrowAddedRadius = 65f;
    private Float biEdgeArrowSize = 20f;
    private String biEdgeArrowColor = "parent";

    private List<PropertyChangeListener> listeners = Collections.synchronizedList(new LinkedList());

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        listeners.remove(pcl);
    }

    private void fire(String propertyName, Object old, Object nue) {
        for (PropertyChangeListener pcls : listeners)
            pcls.propertyChange(new PropertyChangeEvent(this, propertyName, old, nue));
    }

    public Boolean getShowNodes() {
        return showNodes;
    }

    public Float getNodeBorderWidth() {
        return nodeBorderWidth;
    }

    public String getNodeColor() {
        return nodeColor;
    }

    public String getNodeBorderColor() {
        return nodeBorderColor;
    }

    public Boolean getShowNodeLabels() {
        return showNodeLabels;
    }

    public String getNodeLabelFont() {
        return nodeLabelFont;
    }

    public Integer getNodeLabelFontSize() {
        return nodeLabelFontSize;
    }

    public Integer getNodeLabelMaxChar() {
        return nodeLabelMaxChar;
    }

    public String getNodeLabelColor() {
        return nodeLabelColor;
    }

    public Boolean getShowNodeLabelBorders() {
        return showNodeLabelBorders;
    }

    public String getNodeLabelBorderColor() {
        return nodeLabelBorderColor;
    }

    public Boolean getShowEdges() {
        return showEdges;
    }

    public Boolean getCurvedUniEdges() {
        return curvedUniEdges;
    }

    public Boolean getCurvedBiEdges() {
        return curvedBiEdges;
    }

    public String getUniEdgeColor() {
        return uniEdgeColor;
    }

    public String getBiEdgeColor() {
        return biEdgeColor;
    }

    public Boolean getShowSelfLoops() {
        return showSelfLoops;
    }

    public String getSelfLoopColor() {
        return selfLoopColor;
    }

    public Boolean getShowUniEdgeLabels() {
        return showUniEdgeLabels;
    }

    public Integer getUniEdgeLabelMaxChar() {
        return uniEdgeLabelMaxChar;
    }

    public String getUniEdgeLabelFont() {
        return uniEdgeLabelFont;
    }

    public Integer getUniEdgeLabelFontSize() {
        return uniEdgeLabelFontSize;
    }

    public String getUniEdgeLabelColor() {
        return uniEdgeLabelColor;
    }

    public Boolean getShowUniEdgeMiniLabels() {
        return showUniEdgeMiniLabels;
    }

    public Float getUniEdgeMiniLabelAddedRadius() {
        return uniEdgeMiniLabelAddedRadius;
    }

    public Integer getUniEdgeMiniLabelMaxChar() {
        return uniEdgeMiniLabelMaxChar;
    }

    public String getUniEdgeMiniLabelFont() {
        return uniEdgeMiniLabelFont;
    }

    public Integer getUniEdgeMiniLabelFontSize() {
        return uniEdgeMiniLabelFontSize;
    }

    public String getUniEdgeMiniLabelColor() {
        return uniEdgeMiniLabelColor;
    }

    public Boolean getShowUniEdgeArrows() {
        return showUniEdgeArrows;
    }

    public Float getUniEdgeArrowAddedRadius() {
        return uniEdgeArrowAddedRadius;
    }

    public Float getUniEdgeArrowSize() {
        return uniEdgeArrowSize;
    }

    public String getUniEdgeArrowColor() {
        return uniEdgeArrowColor;
    }

    public Boolean getShowBiEdgeLabels() {
        return showBiEdgeLabels;
    }

    public Integer getBiEdgeLabelMaxChar() {
        return biEdgeLabelMaxChar;
    }

    public String getBiEdgeLabelFont() {
        return biEdgeLabelFont;
    }

    public Integer getBiEdgeLabelFontSize() {
        return biEdgeLabelFontSize;
    }

    public String getBiEdgeLabelColor() {
        return biEdgeLabelColor;
    }

    public Boolean getShowBiEdgeMiniLabels() {
        return showBiEdgeMiniLabels;
    }

    public Float getBiEdgeMiniLabelAddedRadius() {
        return biEdgeMiniLabelAddedRadius;
    }

    public Integer getBiEdgeMiniLabelMaxChar() {
        return biEdgeMiniLabelMaxChar;
    }

    public String getBiEdgeMiniLabelFont() {
        return biEdgeMiniLabelFont;
    }

    public Integer getBiEdgeMiniLabelFontSize() {
        return biEdgeMiniLabelFontSize;
    }

    public String getBiEdgeMiniLabelColor() {
        return biEdgeMiniLabelColor;
    }

    public Boolean getShowBiEdgeArrows() {
        return showBiEdgeArrows;
    }

    public Float getBiEdgeArrowAddedRadius() {
        return biEdgeArrowAddedRadius;
    }

    public Float getBiEdgeArrowSize() {
        return biEdgeArrowSize;
    }

    public String getBiEdgeArrowColor() {
        return biEdgeArrowColor;
    }

    public void setShowNodes(Boolean value) {
        Boolean old = showNodes;
        showNodes = value;
        fire("showNodes", old, showNodes);
    }

    public void setNodeBorderWidth(Float value) {
        Float old = nodeBorderWidth;
        nodeBorderWidth = value;
        fire("nodeBorderWidth", old, nodeBorderWidth);
    }

    public void setNodeColor(String value) {
        String old = nodeColor;
        nodeColor = value;
        fire("nodeColor", old, nodeColor);
    }

    public void setNodeBorderColor(String value) {
        String old = nodeBorderColor;
        nodeBorderColor = value;
        fire("nodeBorderColor", old, nodeBorderColor);
    }

    public void setShowNodeLabels(Boolean value) {
        Boolean old = showNodeLabels;
        showNodeLabels = value;
        fire("showNodeLabels", old, showNodeLabels);
    }

    public void setNodeLabelFont(String value) {
        String old = nodeLabelFont;
        nodeLabelFont = value;
        fire("nodeLabelFont", old, nodeLabelFont);
    }

    public void setNodeLabelFontSize(Integer value) {
        Integer old = nodeLabelFontSize;
        nodeLabelFontSize = value;
        fire("nodeLabelFontSize", old, nodeLabelFontSize);
    }

    public void setNodeLabelMaxChar(Integer value) {
        Integer old = nodeLabelMaxChar;
        nodeLabelMaxChar = value;
        fire("nodeLabelMaxChar", old, nodeLabelMaxChar);
    }

    public void setNodeLabelColor(String value) {
        String old = nodeLabelColor;
        nodeLabelColor = value;
        fire("nodeLabelColor", old, nodeLabelColor);
    }

    public void setShowNodeLabelBorders(Boolean value) {
        Boolean old = showNodeLabelBorders;
        showNodeLabelBorders = value;
        fire("showNodeLabelBorders", old, showNodeLabelBorders);
    }

    public void setNodeLabelBorderColor(String value) {
        String old = nodeLabelBorderColor;
        nodeLabelBorderColor = value;
        fire("nodeLabelBorderColor", old, nodeLabelBorderColor);
    }

    public void setShowEdges(Boolean value) {
        Boolean old = showEdges;
        showEdges = value;
        fire("showEdges", old, showEdges);
    }

    public void setCurvedUniEdges(Boolean value) {
        Boolean old = curvedUniEdges;
        curvedUniEdges = value;
        fire("curvedUniEdges", old, curvedUniEdges);
    }

    public void setCurvedBiEdges(Boolean value) {
        Boolean old = curvedBiEdges;
        curvedBiEdges = value;
        fire("curvedBiEdges", old, curvedBiEdges);
    }

    public void setUniEdgeColor(String value) {
        String old = uniEdgeColor;
        uniEdgeColor = value;
        fire("uniEdgeColor", old, uniEdgeColor);
    }

    public void setBiEdgeColor(String value) {
        String old = biEdgeColor;
        biEdgeColor = value;
        fire("biEdgeColor", old, biEdgeColor);
    }

    public void setShowSelfLoops(Boolean value) {
        Boolean old = showSelfLoops;
        showSelfLoops = value;
        fire("showSelfLoops", old, showSelfLoops);
    }

    public void setSelfLoopColor(String value) {
        String old = selfLoopColor;
        selfLoopColor = value;
        fire("selfLoopColor", old, selfLoopColor);
    }

    public void setShowUniEdgeLabels(Boolean value) {
        Boolean old = showUniEdgeLabels;
        showUniEdgeLabels = value;
        fire("showUniEdgeLabels", old, showUniEdgeLabels);
    }

    public void setUniEdgeLabelMaxChar(Integer value) {
        Integer old = uniEdgeLabelMaxChar;
        uniEdgeLabelMaxChar = value;
        fire("uniEdgeLabelMaxChar", old, uniEdgeLabelMaxChar);
    }

    public void setUniEdgeLabelFont(String value) {
        String old = uniEdgeLabelFont;
        uniEdgeLabelFont = value;
        fire("uniEdgeLabelFont", old, uniEdgeLabelFont);
    }

    public void setUniEdgeLabelFontSize(Integer value) {
        Integer old = uniEdgeLabelFontSize;
        uniEdgeLabelFontSize = value;
        fire("uniEdgeLabelFontSize", old, uniEdgeLabelFontSize);
    }

    public void setUniEdgeLabelColor(String value) {
        String old = uniEdgeLabelColor;
        uniEdgeLabelColor = value;
        fire("uniEdgeLabelColor", old, uniEdgeLabelColor);
    }

    public void setShowUniEdgeMiniLabels(Boolean value) {
        Boolean old = showUniEdgeMiniLabels;
        showUniEdgeMiniLabels = value;
        fire("showUniEdgeMiniLabels", old, showUniEdgeMiniLabels);
    }

    public void setUniEdgeMiniLabelAddedRadius(Float value) {
        Float old = uniEdgeMiniLabelAddedRadius;
        uniEdgeMiniLabelAddedRadius = value;
        fire("uniEdgeMiniLabelAddedRadius", old, uniEdgeMiniLabelAddedRadius);
    }

    public void setUniEdgeMiniLabelMaxChar(Integer value) {
        Integer old = uniEdgeMiniLabelMaxChar;
        uniEdgeMiniLabelMaxChar = value;
        fire("uniEdgeMiniLabelMaxChar", old, uniEdgeMiniLabelMaxChar);
    }

    public void setUniEdgeMiniLabelFont(String value) {
        String old = uniEdgeMiniLabelFont;
        uniEdgeMiniLabelFont = value;
        fire("uniEdgeMiniLabelFont", old, uniEdgeMiniLabelFont);
    }

    public void setUniEdgeMiniLabelFontSize(Integer value) {
        Integer old = uniEdgeMiniLabelFontSize;
        uniEdgeMiniLabelFontSize = value;
        fire("uniEdgeMiniLabelFontSize", old, uniEdgeMiniLabelFontSize);
    }

    public void setUniEdgeMiniLabelColor(String value) {
        String old = uniEdgeMiniLabelColor;
        uniEdgeMiniLabelColor = value;
        fire("uniEdgeMiniLabelColor", old, uniEdgeMiniLabelColor);
    }

    public void setShowUniEdgeArrows(Boolean value) {
        Boolean old = showUniEdgeArrows;
        showUniEdgeArrows = value;
        fire("showUniEdgeArrows", old, showUniEdgeArrows);
    }

    public void setUniEdgeArrowAddedRadius(Float value) {
        Float old = uniEdgeArrowAddedRadius;
        uniEdgeArrowAddedRadius = value;
        fire("uniEdgeArrowAddedRadius", old, uniEdgeArrowAddedRadius);
    }

    public void setUniEdgeArrowSize(Float value) {
        Float old = uniEdgeArrowSize;
        uniEdgeArrowSize = value;
        fire("uniEdgeArrowSize", old, uniEdgeArrowSize);
    }

    public void setUniEdgeArrowColor(String value) {
        String old = uniEdgeArrowColor;
        uniEdgeArrowColor = value;
        fire("uniEdgeArrowColor", old, uniEdgeArrowColor);
    }

    public void setShowBiEdgeLabels(Boolean value) {
        Boolean old = showBiEdgeLabels;
        showBiEdgeLabels = value;
        fire("showBiEdgeLabels", old, showBiEdgeLabels);
    }

    public void setBiEdgeLabelMaxChar(Integer value) {
        Integer old = biEdgeLabelMaxChar;
        biEdgeLabelMaxChar = value;
        fire("biEdgeLabelMaxChar", old, biEdgeLabelMaxChar);
    }

    public void setBiEdgeLabelFont(String value) {
        String old = biEdgeLabelFont;
        biEdgeLabelFont = value;
        fire("biEdgeLabelFont", old, biEdgeLabelFont);
    }

    public void setBiEdgeLabelFontSize(Integer value) {
        Integer old = biEdgeLabelFontSize;
        biEdgeLabelFontSize = value;
        fire("biEdgeLabelFontSize", old, biEdgeLabelFontSize);
    }

    public void setBiEdgeLabelColor(String value) {
        String old = biEdgeLabelColor;
        biEdgeLabelColor = value;
        fire("biEdgeLabelColor", old, biEdgeLabelColor);
    }

    public void setShowBiEdgeMiniLabels(Boolean value) {
        Boolean old = showBiEdgeMiniLabels;
        showBiEdgeMiniLabels = value;
        fire("showBiEdgeMiniLabels", old, showBiEdgeMiniLabels);
    }

    public void setBiEdgeMiniLabelAddedRadius(Float value) {
        Float old = biEdgeMiniLabelAddedRadius;
        biEdgeMiniLabelAddedRadius = value;
        fire("biEdgeMiniLabelAddedRadius", old, biEdgeMiniLabelAddedRadius);
    }

    public void setBiEdgeMiniLabelMaxChar(Integer value) {
        Integer old = biEdgeMiniLabelMaxChar;
        biEdgeMiniLabelMaxChar = value;
        fire("biEdgeMiniLabelMaxChar", old, biEdgeMiniLabelMaxChar);
    }

    public void setBiEdgeMiniLabelFont(String value) {
        String old = biEdgeMiniLabelFont;
        biEdgeMiniLabelFont = value;
        fire("biEdgeMiniLabelFont", old, biEdgeMiniLabelFont);
    }

    public void setBiEdgeMiniLabelFontSize(Integer value) {
        Integer old = biEdgeMiniLabelFontSize;
        biEdgeMiniLabelFontSize = value;
        fire("biEdgeMiniLabelFontSize", old, biEdgeMiniLabelFontSize);
    }

    public void setBiEdgeMiniLabelColor(String value) {
        String old = biEdgeMiniLabelColor;
        biEdgeMiniLabelColor = value;
        fire("biEdgeMiniLabelColor", old, biEdgeMiniLabelColor);
    }

    public void setShowBiEdgeArrows(Boolean value) {
        Boolean old = showBiEdgeArrows;
        showBiEdgeArrows = value;
        fire("showBiEdgeArrows", old, showBiEdgeArrows);
    }

    public void setBiEdgeArrowAddedRadius(Float value) {
        Float old = biEdgeArrowAddedRadius;
        biEdgeArrowAddedRadius = value;
        fire("biEdgeArrowAddedRadius", old, biEdgeArrowAddedRadius);
    }

    public void setBiEdgeArrowSize(Float value) {
        Float old = biEdgeArrowSize;
        biEdgeArrowSize = value;
        fire("biEdgeArrowSize", old, biEdgeArrowSize);
    }

    public void setBiEdgeArrowColor(String value) {
        String old = biEdgeArrowColor;
        biEdgeArrowColor = value;
        fire("biEdgeArrowColor", old, biEdgeArrowColor);
    }
}
