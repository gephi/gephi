package org.gephi.ui.preview;

import org.gephi.ui.preview.propertyeditor.NodeChildColorizerPropertyEditor;
import org.gephi.ui.preview.propertyeditor.GenericColorizerPropertyEditor;
import org.gephi.ui.preview.propertyeditor.NodeColorizerPropertyEditor;
import java.awt.Font;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;
import org.gephi.preview.api.color.colorizer.EdgeColorizer;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizer;
import org.gephi.preview.api.supervisor.EdgeSupervisor;
import org.gephi.preview.api.supervisor.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisor.SelfLoopSupervisor;
import org.gephi.ui.preview.propertyeditor.EdgeChildColorizerPropertyEditor;
import org.gephi.ui.preview.propertyeditor.EdgeColorizerPropertyEditor;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author jeremy
 */
public class PreviewNode extends AbstractNode {

    public PreviewNode() {
        super(Children.LEAF);
        setDisplayName("Preview Settings");
    }

	@Override
    protected Sheet createSheet() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
		GlobalEdgeSupervisor ges = controller.getGlobalEdgeSupervisor();
        SelfLoopSupervisor sls = controller.getSelfLoopSupervisor();
        EdgeSupervisor ues = controller.getUniEdgeSupervisor();
        EdgeSupervisor bes = controller.getBiEdgeSupervisor();

        Sheet.Set nodeSet = Sheet.createPropertiesSet();
        nodeSet.setDisplayName("Node Settings");
        nodeSet.setName("nodes");

        Sheet.Set edgeSet = Sheet.createPropertiesSet();
        edgeSet.setDisplayName("Edge Global Settings");
        edgeSet.setName("edges");

        Sheet.Set selfLoopSet = Sheet.createPropertiesSet();
        selfLoopSet.setDisplayName("Self-Loop Global Settings");
        selfLoopSet.setName("selfLoops");

        Sheet.Set uniEdgeSet = Sheet.createPropertiesSet();
        uniEdgeSet.setDisplayName("Unidirectional Edge Settings");
        uniEdgeSet.setName("uniEdges");

        Sheet.Set biEdgeSet = Sheet.createPropertiesSet();
        biEdgeSet.setDisplayName("Bidirectional Edge Settings");
        biEdgeSet.setName("biEdges");

        try {

            // property initializations
            Property showNodesProp = new PropertySupport.Reflection(controller, Boolean.class, "showNodes");
            Property nodeBorderWidthProp = new PropertySupport.Reflection(controller, Float.class, "nodeBorderWidth");
            PropertySupport.Reflection nodeColorProp = new PropertySupport.Reflection(controller, NodeColorizer.class, "nodeColorizer");
            PropertySupport.Reflection nodeBorderColorProp = new PropertySupport.Reflection(controller, GenericColorizer.class, "nodeBorderColorizer");
            Property showNodeLabelsProp = new PropertySupport.Reflection(controller, Boolean.class, "showNodeLabels");
            Property nodeLabelFontProp = new PropertySupport.Reflection(controller, Font.class, "nodeLabelFont");
            Property nodeLabelCharLimitProp = new PropertySupport.Reflection(controller, Integer.class, "nodeLabelMaxChar");
            PropertySupport.Reflection nodeLabelColorProp = new PropertySupport.Reflection(controller, NodeChildColorizer.class, "nodeLabelColorizer");
            Property borderedNodeLabelsProp = new PropertySupport.Reflection(controller, Boolean.class, "showNodeLabelBorders");
            PropertySupport.Reflection nodeLabelBorderColorProp = new PropertySupport.Reflection(controller, NodeChildColorizer.class, "nodeLabelBorderColorizer");
            Property showEdgesProp = new PropertySupport.Reflection(ges, Boolean.class, "showFlag");
            Property curvedUniEdgesProp = new PropertySupport.Reflection(ues, Boolean.class, "curvedFlag");
            Property curvedBiEdgesProp = new PropertySupport.Reflection(bes, Boolean.class, "curvedFlag");
            PropertySupport.Reflection uniEdgeColorProp = new PropertySupport.Reflection(ues, EdgeColorizer.class, "colorizer");
            PropertySupport.Reflection biEdgeColorProp = new PropertySupport.Reflection(bes, EdgeColorizer.class, "colorizer");
            Property showSelfLoopsProp = new PropertySupport.Reflection(sls, Boolean.class, "showSelfLoops");
            PropertySupport.Reflection selfLoopColorProp = new PropertySupport.Reflection(sls, EdgeColorizer.class, "selfLoopColorizer");
            Property showUniEdgeLabelsProp = new PropertySupport.Reflection(ues, Boolean.class, "showLabelsFlag");
            Property uniEdgeLabelCharLimitProp = new PropertySupport.Reflection(ues, Integer.class, "labelMaxChar");
            Property uniEdgeLabelFontProp = new PropertySupport.Reflection(ues, Font.class, "labelFont");
            PropertySupport.Reflection uniEdgeLabelColorProp = new PropertySupport.Reflection(ues, EdgeChildColorizer.class, "labelColorizer");
            Property showUniEdgeMLProp = new PropertySupport.Reflection(ues, Boolean.class, "showMiniLabelsFlag");
            Property uniEdgeMLAddedRadProp = new PropertySupport.Reflection(ues, Float.class, "miniLabelAddedRadius");
            Property uniEdgeMLCharLimitProp = new PropertySupport.Reflection(ues, Integer.class, "miniLabelMaxChar");
            Property uniEdgeMLFontProp = new PropertySupport.Reflection(ues, Font.class, "miniLabelFont");
            PropertySupport.Reflection uniEdgeMLColorProp = new PropertySupport.Reflection(ues, EdgeChildColorizer.class, "miniLabelColorizer");
            Property showUniEdgeArrowsProp = new PropertySupport.Reflection(ues, Boolean.class, "showArrowsFlag");
            Property uniEdgeArrowAddedRadProp = new PropertySupport.Reflection(ues, Float.class, "arrowAddedRadius");
            Property uniEdgeArrowSizeProp = new PropertySupport.Reflection(ues, Float.class, "arrowSize");
            PropertySupport.Reflection uniEdgeArrowColorProp = new PropertySupport.Reflection(ues, EdgeChildColorizer.class, "arrowColorizer");
            Property showBiEdgeLabelsProp = new PropertySupport.Reflection(bes, Boolean.class, "showLabelsFlag");
            Property biEdgeLabelCharLimitProp = new PropertySupport.Reflection(bes, Integer.class, "labelMaxChar");
            Property biEdgeLabelFontProp = new PropertySupport.Reflection(bes, Font.class, "labelFont");
            PropertySupport.Reflection biEdgeLabelColorProp = new PropertySupport.Reflection(bes, EdgeChildColorizer.class, "labelColorizer");
            Property showBiEdgeMLProp = new PropertySupport.Reflection(bes, Boolean.class, "showMiniLabelsFlag");
            Property biEdgeMLAddedRadProp = new PropertySupport.Reflection(bes, Float.class, "miniLabelAddedRadius");
            Property biEdgeMLCharLimitProp = new PropertySupport.Reflection(bes, Integer.class, "miniLabelMaxChar");
            Property biEdgeMLFontProp = new PropertySupport.Reflection(bes, Font.class, "miniLabelFont");
            PropertySupport.Reflection biEdgeMLColorProp = new PropertySupport.Reflection(bes, EdgeChildColorizer.class, "miniLabelColorizer");
            Property showBiEdgeArrowsProp = new PropertySupport.Reflection(bes, Boolean.class, "showArrowsFlag");
            Property biEdgeArrowAddedRadProp = new PropertySupport.Reflection(bes, Float.class, "arrowAddedRadius");
            Property biEdgeArrowSizeProp = new PropertySupport.Reflection(bes, Float.class, "arrowSize");
            PropertySupport.Reflection biEdgeArrowColorProp = new PropertySupport.Reflection(bes, EdgeChildColorizer.class, "arrowColorizer");
            
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
            selfLoopSet.put(showSelfLoopsProp);
            selfLoopSet.put(selfLoopColorProp);
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

		Sheet sheet = Sheet.createDefault();
        sheet.put(nodeSet);
        sheet.put(edgeSet);
        sheet.put(selfLoopSet);
        sheet.put(uniEdgeSet);
        sheet.put(biEdgeSet);
		
        return sheet;
    }
}
