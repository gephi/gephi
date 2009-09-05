package org.gephi.ui.preview;

import java.awt.Font;
import org.gephi.preview.api.GraphCustomizer;
import org.gephi.preview.api.color.EdgeChildColorizer;
import org.gephi.preview.api.color.EdgeColorizer;
import org.gephi.preview.api.color.GenericColorizer;
import org.gephi.preview.api.color.NodeChildColorizer;
import org.gephi.preview.api.color.NodeColorizer;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jeremy
 */
public class PreviewNode extends AbstractNode {

    public PreviewNode(GraphCustomizer customizer) {
        super(Children.LEAF, Lookups.singleton(customizer));
        setDisplayName("Preview Settings");
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        GraphCustomizer obj = getLookup().lookup(GraphCustomizer.class);

        Sheet.Set nodeSet = Sheet.createPropertiesSet();
        nodeSet.setDisplayName("Node Settings");
        nodeSet.setName("nodes");

        Sheet.Set edgeSet = Sheet.createPropertiesSet();
        edgeSet.setDisplayName("Edge Global Settings");
        edgeSet.setName("edges");

        Sheet.Set uniEdgeSet = Sheet.createPropertiesSet();
        uniEdgeSet.setDisplayName("Unidirectional Edge Settings");
        uniEdgeSet.setName("uniEdges");

        Sheet.Set biEdgeSet = Sheet.createPropertiesSet();
        biEdgeSet.setDisplayName("Bidirectional Edge Settings");
        biEdgeSet.setName("biEdges");

        try {

            // property initializations
            Property showNodesProp = new PropertySupport.Reflection(obj, Boolean.class, "showNodes");
            Property nodeBorderWidthProp = new PropertySupport.Reflection(obj, Float.class, "nodeBorderWidth");
            PropertySupport.Reflection nodeColorProp = new PropertySupport.Reflection(obj, NodeColorizer.class, "nodeColorizer");
            PropertySupport.Reflection nodeBorderColorProp = new PropertySupport.Reflection(obj, GenericColorizer.class, "nodeBorderColorizer");
            Property showNodeLabelsProp = new PropertySupport.Reflection(obj, Boolean.class, "showNodeLabels");
            Property nodeLabelFontProp = new PropertySupport.Reflection(obj, Font.class, "nodeLabelFont");
            Property nodeLabelCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "nodeLabelMaxChar");
            PropertySupport.Reflection nodeLabelColorProp = new PropertySupport.Reflection(obj, NodeChildColorizer.class, "nodeLabelColorizer");
            Property borderedNodeLabelsProp = new PropertySupport.Reflection(obj, Boolean.class, "showNodeLabelBorders");
            PropertySupport.Reflection nodeLabelBorderColorProp = new PropertySupport.Reflection(obj, NodeChildColorizer.class, "nodeLabelBorderColorizer");
            Property showEdgesProp = new PropertySupport.Reflection(obj, Boolean.class, "showEdges");
            Property curvedUniEdgesProp = new PropertySupport.Reflection(obj, Boolean.class, "curvedUniEdges");
            Property curvedBiEdgesProp = new PropertySupport.Reflection(obj, Boolean.class, "curvedBiEdges");
            PropertySupport.Reflection uniEdgeColorProp = new PropertySupport.Reflection(obj, EdgeColorizer.class, "uniEdgeColorizer");
            PropertySupport.Reflection biEdgeColorProp = new PropertySupport.Reflection(obj, EdgeColorizer.class, "biEdgeColorizer");
            Property showSelfLoopsProp = new PropertySupport.Reflection(obj, Boolean.class, "showSelfLoops");
            PropertySupport.Reflection selfLoopColorProp = new PropertySupport.Reflection(obj, EdgeColorizer.class, "selfLoopColorizer");
            Property showUniEdgeLabelsProp = new PropertySupport.Reflection(obj, Boolean.class, "showUniEdgeLabels");
            Property uniEdgeLabelCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "uniEdgeLabelMaxChar");
            Property uniEdgeLabelFontProp = new PropertySupport.Reflection(obj, Font.class, "uniEdgeLabelFont");
            PropertySupport.Reflection uniEdgeLabelColorProp = new PropertySupport.Reflection(obj, EdgeChildColorizer.class, "uniEdgeLabelColorizer");
            Property showUniEdgeMLProp = new PropertySupport.Reflection(obj, Boolean.class, "showUniEdgeMiniLabels");
            Property uniEdgeMLAddedRadProp = new PropertySupport.Reflection(obj, Float.class, "uniEdgeMiniLabelAddedRadius");
            Property uniEdgeMLCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "uniEdgeMiniLabelMaxChar");
            Property uniEdgeMLFontProp = new PropertySupport.Reflection(obj, Font.class, "uniEdgeMiniLabelFont");
            PropertySupport.Reflection uniEdgeMLColorProp = new PropertySupport.Reflection(obj, EdgeChildColorizer.class, "uniEdgeMiniLabelColorizer");
            Property showUniEdgeArrowsProp = new PropertySupport.Reflection(obj, Boolean.class, "showUniEdgeArrows");
            Property uniEdgeArrowAddedRadProp = new PropertySupport.Reflection(obj, Float.class, "uniEdgeArrowAddedRadius");
            Property uniEdgeArrowSizeProp = new PropertySupport.Reflection(obj, Float.class, "uniEdgeArrowSize");
            PropertySupport.Reflection uniEdgeArrowColorProp = new PropertySupport.Reflection(obj, EdgeChildColorizer.class, "uniEdgeArrowColorizer");
            Property showBiEdgeLabelsProp = new PropertySupport.Reflection(obj, Boolean.class, "showBiEdgeLabels");
            Property biEdgeLabelCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "biEdgeLabelMaxChar");
            Property biEdgeLabelFontProp = new PropertySupport.Reflection(obj, Font.class, "biEdgeLabelFont");
            PropertySupport.Reflection biEdgeLabelColorProp = new PropertySupport.Reflection(obj, EdgeChildColorizer.class, "biEdgeLabelColorizer");
            Property showBiEdgeMLProp = new PropertySupport.Reflection(obj, Boolean.class, "showBiEdgeMiniLabels");
            Property biEdgeMLAddedRadProp = new PropertySupport.Reflection(obj, Float.class, "biEdgeMiniLabelAddedRadius");
            Property biEdgeMLCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "biEdgeMiniLabelMaxChar");
            Property biEdgeMLFontProp = new PropertySupport.Reflection(obj, Font.class, "biEdgeMiniLabelFont");
            PropertySupport.Reflection biEdgeMLColorProp = new PropertySupport.Reflection(obj, EdgeChildColorizer.class, "biEdgeMiniLabelColorizer");
            Property showBiEdgeArrowsProp = new PropertySupport.Reflection(obj, Boolean.class, "showBiEdgeArrows");
            Property biEdgeArrowAddedRadProp = new PropertySupport.Reflection(obj, Float.class, "biEdgeArrowAddedRadius");
            Property biEdgeArrowSizeProp = new PropertySupport.Reflection(obj, Float.class, "biEdgeArrowSize");
            PropertySupport.Reflection biEdgeArrowColorProp = new PropertySupport.Reflection(obj, EdgeChildColorizer.class, "biEdgeArrowColorizer");
            
            // set custom property editors
            nodeColorProp.setPropertyEditorClass(NodeColorizerPropertyEditor.class);
            nodeBorderColorProp.setPropertyEditorClass(GenericColorizerPropertyEditor.class);
            nodeLabelColorProp.setPropertyEditorClass(NodeChildColorizerPropertyEditor.class);
            nodeLabelBorderColorProp.setPropertyEditorClass(NodeChildColorizerPropertyEditor.class);
            uniEdgeColorProp.setPropertyEditorClass(EdgeColorizerPropertyEditor.class);
            biEdgeColorProp.setPropertyEditorClass(EdgeColorizerPropertyEditor.class);
            selfLoopColorProp.setPropertyEditorClass(EdgeColorizerPropertyEditor.class);
            uniEdgeLabelColorProp.setPropertyEditorClass(EdgeChildColorizerPropertyEditor.class);
            uniEdgeMLColorProp.setPropertyEditorClass(EdgeChildColorizerPropertyEditor.class);
            uniEdgeArrowColorProp.setPropertyEditorClass(EdgeChildColorizerPropertyEditor.class);
            biEdgeLabelColorProp.setPropertyEditorClass(EdgeChildColorizerPropertyEditor.class);
            biEdgeMLColorProp.setPropertyEditorClass(EdgeChildColorizerPropertyEditor.class);
            biEdgeArrowColorProp.setPropertyEditorClass(EdgeChildColorizerPropertyEditor.class);

            // set properties' names
            showNodesProp.setName("showNodes");
            nodeBorderWidthProp.setName("nodeBorderWidth");
            nodeColorProp.setName("nodeColor");
            nodeBorderColorProp.setName("nodeBorderColor");
            showNodeLabelsProp.setName("showNodeLabels");
            nodeLabelFontProp.setName("nodeLabelFont");
            nodeLabelCharLimitProp.setName("nodeLabelMaxChar");
            nodeLabelColorProp.setName("nodeLabelColor");
            borderedNodeLabelsProp.setName("showNodeLabelBorders");
            nodeLabelBorderColorProp.setName("nodeLabelBorderColor");
            showEdgesProp.setName("showEdges");
            curvedUniEdgesProp.setName("curvedUniEdges");
            curvedBiEdgesProp.setName("curvedBiEdges");
            uniEdgeColorProp.setName("uniEdgeColor");
            biEdgeColorProp.setName("biEdgeColor");
            showSelfLoopsProp.setName("showSelfLoops");
            selfLoopColorProp.setName("selfLoopColor");
            showUniEdgeLabelsProp.setName("showUniEdgeLabels");
            uniEdgeLabelCharLimitProp.setName("uniEdgeLabelMaxChar");
            uniEdgeLabelFontProp.setName("uniEdgeLabelFont");
            uniEdgeLabelColorProp.setName("uniEdgeLabelColor");
            showUniEdgeMLProp.setName("showUniEdgeMiniLabels");
            uniEdgeMLAddedRadProp.setName("uniEdgeMiniLabelAddedRadius");
            uniEdgeMLCharLimitProp.setName("uniEdgeMiniLabelMaxChar");
            uniEdgeMLFontProp.setName("uniEdgeMiniLabelFont");
            uniEdgeMLColorProp.setName("uniEdgeMiniLabelColor");
            showUniEdgeArrowsProp.setName("showUniEdgeArrows");
            uniEdgeArrowAddedRadProp.setName("uniEdgeArrowAddedRadius");
            uniEdgeArrowSizeProp.setName("uniEdgeArrowSize");
            uniEdgeArrowColorProp.setName("uniEdgeArrowColor");
            showBiEdgeLabelsProp.setName("showBiEdgeLabels");
            biEdgeLabelCharLimitProp.setName("biEdgeLabelMaxChar");
            biEdgeLabelFontProp.setName("biEdgeLabelFont");
            biEdgeLabelColorProp.setName("biEdgeLabelColor");
            showBiEdgeMLProp.setName("showBiEdgeMiniLabels");
            biEdgeMLAddedRadProp.setName("biEdgeMiniLabelAddedRadius");
            biEdgeMLCharLimitProp.setName("biEdgeMiniLabelMaxChar");
            biEdgeMLFontProp.setName("biEdgeMiniLabelFont");
            biEdgeMLColorProp.setName("biEdgeMiniLabelColor");
            showBiEdgeArrowsProp.setName("showBiEdgeArrows");
            biEdgeArrowAddedRadProp.setName("biEdgeArrowAddedRadius");
            biEdgeArrowSizeProp.setName("biEdgeArrowSize");
            biEdgeArrowColorProp.setDisplayName("biEdgeArrowColor");
             
            // set properties' display names
            showNodesProp.setDisplayName("Show Nodes");
            nodeBorderWidthProp.setDisplayName("Node Border Width");
            nodeColorProp.setDisplayName("Node Color");
            nodeBorderColorProp.setDisplayName("Node Border Color");
            showNodeLabelsProp.setDisplayName("Show Node Labels");
            nodeLabelFontProp.setDisplayName("Node Label Font");
            nodeLabelCharLimitProp.setDisplayName("Node Label Char Limit");
            nodeLabelColorProp.setDisplayName("Node Label Color");
            borderedNodeLabelsProp.setDisplayName("Bordered Node Labels");
            nodeLabelBorderColorProp.setDisplayName("Node Label Border Color");
            showEdgesProp.setDisplayName("Show Edges");
            curvedUniEdgesProp.setDisplayName("Curved Uni. Edges");
            curvedBiEdgesProp.setDisplayName("Curved Bi. Edges");
            uniEdgeColorProp.setDisplayName("Uni. Edge Color");
            biEdgeColorProp.setDisplayName("Bi. Edge Color");
            showSelfLoopsProp.setDisplayName("Show Self-Loops");
            selfLoopColorProp.setDisplayName("Self-Loop Color");
            showUniEdgeLabelsProp.setDisplayName("Show Uni. Edge Labels");
            uniEdgeLabelCharLimitProp.setDisplayName("Uni. Edge Label Char Limit");
            uniEdgeLabelFontProp.setDisplayName("Uni. Edge Label Font");
            uniEdgeLabelColorProp.setDisplayName("Uni. Edge Label Color");
            showUniEdgeMLProp.setDisplayName("Show Uni. Edge Mini-Labels");
            uniEdgeMLAddedRadProp.setDisplayName("Uni. Edge Mini-Label Added Radius");
            uniEdgeMLCharLimitProp.setDisplayName("Uni. Edge Mini-Label Char Limit");
            uniEdgeMLFontProp.setDisplayName("Uni. Edge Mini-Label Font");
            uniEdgeMLColorProp.setDisplayName("Uni. Edge Mini-Label Color");
            showUniEdgeArrowsProp.setDisplayName("Show Uni. Edge Arrows");
            uniEdgeArrowAddedRadProp.setDisplayName("Uni. Edge Arrow Added Radius");
            uniEdgeArrowSizeProp.setDisplayName("Uni. Edge Arrow Size");
            uniEdgeArrowColorProp.setDisplayName("Uni. Edge Arrow Color");
            showBiEdgeLabelsProp.setDisplayName("Show Bi. Edge Labels");
            biEdgeLabelCharLimitProp.setDisplayName("Bi. Edge Label Char Limit");
            biEdgeLabelFontProp.setDisplayName("Bi. Edge Label Font");
            biEdgeLabelColorProp.setDisplayName("Bi. Edge Label Color");
            showBiEdgeMLProp.setDisplayName("Show Bi. Edge Mini-Labels");
            biEdgeMLAddedRadProp.setDisplayName("Bi. Edge Mini-Label Added Radius");
            biEdgeMLCharLimitProp.setDisplayName("Bi. Edge Mini-Label Char Limit");
            biEdgeMLFontProp.setDisplayName("Bi. Edge Mini-Label Font");
            biEdgeMLColorProp.setDisplayName("Bi. Edge Mini-Label Color");
            showBiEdgeArrowsProp.setDisplayName("Show Bi. Edge Arrows");
            biEdgeArrowAddedRadProp.setDisplayName("Bi. Edge Arrow Added Radius");
            biEdgeArrowSizeProp.setDisplayName("Bi. Edge Arrow Size");
            biEdgeArrowColorProp.setDisplayName("Bi. Edge Arrow Color");

            // add properties to the property set
            nodeSet.put(showNodesProp);
            nodeSet.put(nodeBorderWidthProp);
            nodeSet.put(nodeColorProp);
            nodeSet.put(nodeBorderColorProp);
            nodeSet.put(showNodeLabelsProp);
            nodeSet.put(nodeLabelFontProp);
            nodeSet.put(nodeLabelCharLimitProp);
            nodeSet.put(nodeLabelColorProp);
            nodeSet.put(borderedNodeLabelsProp);
            nodeSet.put(nodeLabelBorderColorProp);
            edgeSet.put(showEdgesProp);
            uniEdgeSet.put(curvedUniEdgesProp);
            biEdgeSet.put(curvedBiEdgesProp);
            uniEdgeSet.put(uniEdgeColorProp);
            biEdgeSet.put(biEdgeColorProp);
            edgeSet.put(showSelfLoopsProp);
            edgeSet.put(selfLoopColorProp);
            uniEdgeSet.put(showUniEdgeLabelsProp);
            uniEdgeSet.put(uniEdgeLabelCharLimitProp);
            uniEdgeSet.put(uniEdgeLabelFontProp);
            uniEdgeSet.put(uniEdgeLabelColorProp);
            uniEdgeSet.put(showUniEdgeMLProp);
            uniEdgeSet.put(uniEdgeMLAddedRadProp);
            uniEdgeSet.put(uniEdgeMLCharLimitProp);
            uniEdgeSet.put(uniEdgeMLFontProp);
            uniEdgeSet.put(uniEdgeMLColorProp);
            uniEdgeSet.put(showUniEdgeArrowsProp);
            uniEdgeSet.put(uniEdgeArrowAddedRadProp);
            uniEdgeSet.put(uniEdgeArrowSizeProp);
            uniEdgeSet.put(uniEdgeArrowColorProp);
            biEdgeSet.put(showBiEdgeLabelsProp);
            biEdgeSet.put(biEdgeLabelCharLimitProp);
            biEdgeSet.put(biEdgeLabelFontProp);
            biEdgeSet.put(biEdgeLabelColorProp);
            biEdgeSet.put(showBiEdgeMLProp);
            biEdgeSet.put(biEdgeMLAddedRadProp);
            biEdgeSet.put(biEdgeMLCharLimitProp);
            biEdgeSet.put(biEdgeMLFontProp);
            biEdgeSet.put(biEdgeMLColorProp);
            biEdgeSet.put(showBiEdgeArrowsProp);
            biEdgeSet.put(biEdgeArrowAddedRadProp);
            biEdgeSet.put(biEdgeArrowSizeProp);
            biEdgeSet.put(biEdgeArrowColorProp);

        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }

        sheet.put(nodeSet);
        sheet.put(edgeSet);
        sheet.put(uniEdgeSet);
        sheet.put(biEdgeSet);
        return sheet;
    }
}
