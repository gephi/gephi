/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.preview.presets;

import org.gephi.preview.api.PreviewPreset;

/**
 *
 * @author Mathieu Bastian
 */
public class DefaultPreset extends PreviewPreset {

    public DefaultPreset() {
        super("Default");

        properties.put("showNodes", "true");
        properties.put("Directed_arrowAddedRadius", "65.0");
        properties.put("Directed_miniLabelColorizer", "parent");
        properties.put("Bidirectional_shortenLabelsFlag", "false");
        properties.put("Bidirectional_showArrowsFlag", "true");
        properties.put("Bidirectional_labelColorizer", "parent");
        properties.put("Bidirectional_arrowSize", "20.0");
        properties.put("Bidirectional_miniLabelFont", "SansSerif 8 Plain");
        properties.put("Bidirectional_arrowAddedRadius", "65.0");
        properties.put("Bidirectional_baseLabelFont", "SansSerif 10 Plain");
        properties.put("nodeLabelColorizer", "custom [0,0,0]");
        properties.put("nodeLabelBorderColorizer", "custom [255,255,255]");
        properties.put("Bidirectional_colorizer", "mixed");
        properties.put("Directed_labelColorizer", "parent");
        properties.put("showNodeLabelBorders", "false");
        properties.put("SelfLoop_showFlag", "true");
        properties.put("GlobalEdge_showFlag", "true");
        properties.put("Directed_miniLabelAddedRadius", "15.0");
        properties.put("showNodeLabels", "false");
        properties.put("Directed_colorizer", "source");
        properties.put("Directed_miniLabelMaxChar", "10");
        properties.put("Undirected_shortenLabelsFlag", "false");
        properties.put("Directed_shortenLabelsFlag", "false");
        properties.put("Bidirectional_curvedFlag", "false");
        properties.put("baseNodeLabelFont", "SansSerif 14 Plain");
        properties.put("Directed_showMiniLabelsFlag", "false");
        properties.put("Directed_arrowSize", "20.0");
        properties.put("SelfLoop_edgeScale", "1.0");
        properties.put("SelfLoop_colorizer", "custom [0,0,0]");
        properties.put("Directed_showArrowsFlag", "true");
        properties.put("Undirected_baseLabelFont", "SansSerif 10 Plain");
        properties.put("Undirected_showLabelsFlag", "false");
        properties.put("Directed_edgeScale", "1.0");
        properties.put("Undirected_labelMaxChar", "10");
        properties.put("shortenLabelsFlag", "false");
        properties.put("nodeLabelMaxChar", "10");
        properties.put("Undirected_colorizer", "mixed");
        properties.put("Bidirectional_miniLabelMaxChar", "10");
        properties.put("Directed_labelMaxChar", "10");
        properties.put("nodeBorderWidth", "1.0");
        properties.put("Undirected_labelColorizer", "parent");
        properties.put("Directed_arrowColorizer", "parent");
        properties.put("Bidirectional_arrowColorizer", "parent");
        properties.put("nodeBorderColorizer", "custom [0,0,0]");
        properties.put("Bidirectional_edgeScale", "1.0");
        properties.put("Bidirectional_labelMaxChar", "10");
        properties.put("Directed_showLabelsFlag", "false");
        properties.put("Bidirectional_miniLabelColorizer", "parent");
        properties.put("Directed_shortenMiniLabelsFlag", "false");
        properties.put("Bidirectional_shortenMiniLabelsFlag", "false");
        properties.put("Directed_curvedFlag", "true");
        properties.put("Undirected_curvedFlag", "false");
        properties.put("Bidirectional_miniLabelAddedRadius", "15.0");
        properties.put("nodeColorizer", "original");
        properties.put("Undirected_edgeScale", "1.0");
        properties.put("Directed_baseLabelFont", "SansSerif 10 Plain");
        properties.put("Bidirectional_showMiniLabelsFlag", "false");
        properties.put("Bidirectional_showLabelsFlag", "false");
        properties.put("Directed_miniLabelFont", "SansSerif 8 Plain");
    }
}
