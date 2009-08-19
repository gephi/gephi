package org.gephi.preview.api;

/**
 *
 * @author jeremy
 */
public interface Customizer {

    public String getNodeLabelFont();

    public int getNodeLabelFontSize();

    public String getUnidirectionalEdgeLabelFont();

    public int getUnidirectionalEdgeLabelFontSize();

    public String getUnidirectionalEdgeMiniLabelFont();

    public int getUnidirectionalEdgeMiniLabelFontSize();

    public String getBidirectionalEdgeLabelFont();

    public int getBidirectionalEdgeLabelFontSize();

    public String getBidirectionalEdgeMiniLabelFont();

    public int getBidirectionalEdgeMiniLabelFontSize();

    public boolean showEdges();

    public boolean showSelfLoops();

    public boolean showNodes();

    public Color getNodeBorderColor();

    public float getNodeBorderWidth();

    public boolean showNodeLabels();

    public boolean showNodeLabelBorders();



}
