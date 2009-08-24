package org.gephi.ui.preview;

import org.gephi.preview.api.GraphCustomizer;
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
        Sheet.Set set = Sheet.createPropertiesSet();
        GraphCustomizer obj = getLookup().lookup(GraphCustomizer.class);

        try {

            // property initializations
            Property showNodesProp = new PropertySupport.Reflection(obj, Boolean.class, "showNodes");
            Property nodeBorderWidthProp = new PropertySupport.Reflection(obj, Float.class, "nodeBorderWidth");
            Property nodeColorProp = new PropertySupport.Reflection(obj, String.class, "nodeColor");
            Property nodeBorderColorProp = new PropertySupport.Reflection(obj, String.class, "nodeBorderColor");
            Property showNodeLabelsProp = new PropertySupport.Reflection(obj, Boolean.class, "showNodeLabels");
            Property nodeLabelFontProp = new PropertySupport.Reflection(obj, String.class, "nodeLabelFont");
            Property nodeLabelFontSizeProp = new PropertySupport.Reflection(obj, Integer.class, "nodeLabelFontSize");
            Property nodeLabelCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "nodeLabelMaxChar");
            Property nodeLabelColorProp = new PropertySupport.Reflection(obj, String.class, "nodeLabelColor");
            Property borderedNodeLabelsProp = new PropertySupport.Reflection(obj, Boolean.class, "showNodeLabelBorders");
            Property nodeLabelBorderColorProp = new PropertySupport.Reflection(obj, String.class, "nodeLabelBorderColor");
            Property showEdgesProp = new PropertySupport.Reflection(obj, Boolean.class, "showEdges");
            Property curvedUniEdgesProp = new PropertySupport.Reflection(obj, Boolean.class, "curvedUniEdges");
            Property curvedBiEdgesProp = new PropertySupport.Reflection(obj, Boolean.class, "curvedBiEdges");
            Property uniEdgeColorProp = new PropertySupport.Reflection(obj, String.class, "uniEdgeColor");
            Property biEdgeColorProp = new PropertySupport.Reflection(obj, String.class, "biEdgeColor");
            Property showSelfLoopsProp = new PropertySupport.Reflection(obj, Boolean.class, "showSelfLoops");
            Property selfLoopColorProp = new PropertySupport.Reflection(obj, String.class, "selfLoopColor");
            Property showUniEdgeLabelsProp = new PropertySupport.Reflection(obj, Boolean.class, "showUniEdgeLabels");
            Property uniEdgeLabelCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "uniEdgeLabelMaxChar");
            Property uniEdgeLabelFontProp = new PropertySupport.Reflection(obj, String.class, "uniEdgeLabelFont");
            Property uniEdgeLabelFontSizeProp = new PropertySupport.Reflection(obj, Integer.class, "uniEdgeLabelFontSize");
            Property uniEdgeLabelColorProp = new PropertySupport.Reflection(obj, String.class, "uniEdgeLabelColor");
            Property showUniEdgeMLProp = new PropertySupport.Reflection(obj, Boolean.class, "showUniEdgeMiniLabels");
            Property uniEdgeMLAddedRadProp = new PropertySupport.Reflection(obj, Float.class, "uniEdgeMiniLabelAddedRadius");
            Property uniEdgeMLCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "uniEdgeMiniLabelMaxChar");
            Property uniEdgeMLFontProp = new PropertySupport.Reflection(obj, String.class, "uniEdgeMiniLabelFont");
            Property uniEdgeMLFontSizeProp = new PropertySupport.Reflection(obj, Integer.class, "uniEdgeMiniLabelFontSize");
            Property uniEdgeMLColorProp = new PropertySupport.Reflection(obj, String.class, "uniEdgeMiniLabelColor");
            Property showUniEdgeArrowsProp = new PropertySupport.Reflection(obj, Boolean.class, "showUniEdgeArrows");
            Property uniEdgeArrowAddedRadProp = new PropertySupport.Reflection(obj, Float.class, "uniEdgeArrowAddedRadius");
            Property uniEdgeArrowSizeProp = new PropertySupport.Reflection(obj, Float.class, "uniEdgeArrowSize");
            Property uniEdgeArrowColorProp = new PropertySupport.Reflection(obj, String.class, "uniEdgeArrowColor");
            Property showBiEdgeLabelsProp = new PropertySupport.Reflection(obj, Boolean.class, "showBiEdgeLabels");
            Property biEdgeLabelCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "biEdgeLabelMaxChar");
            Property biEdgeLabelFontProp = new PropertySupport.Reflection(obj, String.class, "biEdgeLabelFont");
            Property biEdgeLabelFontSizeProp = new PropertySupport.Reflection(obj, Integer.class, "biEdgeLabelFontSize");
            Property biEdgeLabelColorProp = new PropertySupport.Reflection(obj, String.class, "biEdgeLabelColor");
            Property showBiEdgeMLProp = new PropertySupport.Reflection(obj, Boolean.class, "showBiEdgeMiniLabels");
            Property biEdgeMLAddedRadProp = new PropertySupport.Reflection(obj, Float.class, "biEdgeMiniLabelAddedRadius");
            Property biEdgeMLCharLimitProp = new PropertySupport.Reflection(obj, Integer.class, "biEdgeMiniLabelMaxChar");
            Property biEdgeMLFontProp = new PropertySupport.Reflection(obj, String.class, "biEdgeMiniLabelFont");
            Property biEdgeMLFontSizeProp = new PropertySupport.Reflection(obj, Integer.class, "biEdgeMiniLabelFontSize");
            Property biEdgeMLColorProp = new PropertySupport.Reflection(obj, String.class, "biEdgeMiniLabelColor");
            Property showBiEdgeArrowsProp = new PropertySupport.Reflection(obj, Boolean.class, "showBiEdgeArrows");
            Property biEdgeArrowAddedRadProp = new PropertySupport.Reflection(obj, Float.class, "biEdgeArrowAddedRadius");
            Property biEdgeArrowSizeProp = new PropertySupport.Reflection(obj, Float.class, "biEdgeArrowSize");
            Property biEdgeArrowColorProp = new PropertySupport.Reflection(obj, String.class, "biEdgeArrowColor");
            
            // set properties' names
            showNodesProp.setName("showNodes");
            nodeBorderWidthProp.setName("nodeBorderWidth");
            nodeColorProp.setName("nodeColor");
            nodeBorderColorProp.setName("nodeBorderColor");
            showNodeLabelsProp.setName("showNodeLabels");
            nodeLabelFontProp.setName("nodeLabelFont");
            nodeLabelFontSizeProp.setName("nodeLabelFontSize");
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
            uniEdgeLabelFontSizeProp.setName("uniEdgeLabelFontSize");
            uniEdgeLabelColorProp.setName("uniEdgeLabelColor");
            showUniEdgeMLProp.setName("showUniEdgeMiniLabels");
            uniEdgeMLAddedRadProp.setName("uniEdgeMiniLabelAddedRadius");
            uniEdgeMLCharLimitProp.setName("uniEdgeMiniLabelMaxChar");
            uniEdgeMLFontProp.setName("uniEdgeMiniLabelFont");
            uniEdgeMLFontSizeProp.setName("uniEdgeMiniLabelFontSize");
            uniEdgeMLColorProp.setName("uniEdgeMiniLabelColor");
            showUniEdgeArrowsProp.setName("showUniEdgeArrows");
            uniEdgeArrowAddedRadProp.setName("uniEdgeArrowAddedRadius");
            uniEdgeArrowSizeProp.setName("uniEdgeArrowSize");
            uniEdgeArrowColorProp.setName("uniEdgeArrowColor");
            showBiEdgeLabelsProp.setName("showBiEdgeLabels");
            biEdgeLabelCharLimitProp.setName("biEdgeLabelMaxChar");
            biEdgeLabelFontProp.setName("biEdgeLabelFont");
            biEdgeLabelFontSizeProp.setName("biEdgeLabelFontSize");
            biEdgeLabelColorProp.setName("biEdgeLabelColor");
            showBiEdgeMLProp.setName("showBiEdgeMiniLabels");
            biEdgeMLAddedRadProp.setName("biEdgeMiniLabelAddedRadius");
            biEdgeMLCharLimitProp.setName("biEdgeMiniLabelMaxChar");
            biEdgeMLFontProp.setName("biEdgeMiniLabelFont");
            biEdgeMLFontSizeProp.setName("biEdgeMiniLabelFontSize");
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
            nodeLabelFontSizeProp.setDisplayName("Node Label Font Size");
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
            uniEdgeLabelFontSizeProp.setDisplayName("Uni. Edge Label Font Size");
            uniEdgeLabelColorProp.setDisplayName("Uni. Edge Label Color");
            showUniEdgeMLProp.setDisplayName("Show Uni. Edge Mini-Labels");
            uniEdgeMLAddedRadProp.setDisplayName("Uni. Edge Mini-Label Added Radius");
            uniEdgeMLCharLimitProp.setDisplayName("Uni. Edge Mini-Label Char Limit");
            uniEdgeMLFontProp.setDisplayName("Uni. Edge Mini-Label Font");
            uniEdgeMLFontSizeProp.setDisplayName("Uni. Edge Mini-Label Font Size");
            uniEdgeMLColorProp.setDisplayName("Uni. Edge Mini-Label Color");
            showUniEdgeArrowsProp.setDisplayName("Show Uni. Edge Arrows");
            uniEdgeArrowAddedRadProp.setDisplayName("Uni. Edge Arrow Added Radius");
            uniEdgeArrowSizeProp.setDisplayName("Uni. Edge Arrow Size");
            uniEdgeArrowColorProp.setDisplayName("Uni. Edge Arrow Color");
            showBiEdgeLabelsProp.setDisplayName("Show Bi. Edge Labels");
            biEdgeLabelCharLimitProp.setDisplayName("Bi. Edge Label Char Limit");
            biEdgeLabelFontProp.setDisplayName("Bi. Edge Label Font");
            biEdgeLabelFontSizeProp.setDisplayName("Bi. Edge Label Font Size");
            biEdgeLabelColorProp.setDisplayName("Bi. Edge Label Color");
            showBiEdgeMLProp.setDisplayName("Show Bi. Edge Mini-Labels");
            biEdgeMLAddedRadProp.setDisplayName("Bi. Edge Mini-Label Added Radius");
            biEdgeMLCharLimitProp.setDisplayName("Bi. Edge Mini-Label Char Limit");
            biEdgeMLFontProp.setDisplayName("Bi. Edge Mini-Label Font");
            biEdgeMLFontSizeProp.setDisplayName("Bi. Edge Mini-Label Font Size");
            biEdgeMLColorProp.setDisplayName("Bi. Edge Mini-Label Color");
            showBiEdgeArrowsProp.setDisplayName("Show Bi. Edge Arrows");
            biEdgeArrowAddedRadProp.setDisplayName("Bi. Edge Arrow Added Radius");
            biEdgeArrowSizeProp.setDisplayName("Bi. Edge Arrow Size");
            biEdgeArrowColorProp.setDisplayName("Bi. Edge Arrow Color");

            // add properties to the property set
            set.put(showNodesProp);
            set.put(nodeBorderWidthProp);
            set.put(nodeColorProp);
            set.put(nodeBorderColorProp);
            set.put(showNodeLabelsProp);
            set.put(nodeLabelFontProp);
            set.put(nodeLabelFontSizeProp);
            set.put(nodeLabelCharLimitProp);
            set.put(nodeLabelColorProp);
            set.put(borderedNodeLabelsProp);
            set.put(nodeLabelBorderColorProp);
            set.put(showEdgesProp);
            set.put(curvedUniEdgesProp);
            set.put(curvedBiEdgesProp);
            set.put(uniEdgeColorProp);
            set.put(biEdgeColorProp);
            set.put(showSelfLoopsProp);
            set.put(selfLoopColorProp);
            set.put(showUniEdgeLabelsProp);
            set.put(uniEdgeLabelCharLimitProp);
            set.put(uniEdgeLabelFontProp);
            set.put(uniEdgeLabelFontSizeProp);
            set.put(uniEdgeLabelColorProp);
            set.put(showUniEdgeMLProp);
            set.put(uniEdgeMLAddedRadProp);
            set.put(uniEdgeMLCharLimitProp);
            set.put(uniEdgeMLFontProp);
            set.put(uniEdgeMLFontSizeProp);
            set.put(uniEdgeMLColorProp);
            set.put(showUniEdgeArrowsProp);
            set.put(uniEdgeArrowAddedRadProp);
            set.put(uniEdgeArrowSizeProp);
            set.put(uniEdgeArrowColorProp);
            set.put(showBiEdgeLabelsProp);
            set.put(biEdgeLabelCharLimitProp);
            set.put(biEdgeLabelFontProp);
            set.put(biEdgeLabelFontSizeProp);
            set.put(biEdgeLabelColorProp);
            set.put(showBiEdgeMLProp);
            set.put(biEdgeMLAddedRadProp);
            set.put(biEdgeMLCharLimitProp);
            set.put(biEdgeMLFontProp);
            set.put(biEdgeMLFontSizeProp);
            set.put(biEdgeMLColorProp);
            set.put(showBiEdgeArrowsProp);
            set.put(biEdgeArrowAddedRadProp);
            set.put(biEdgeArrowSizeProp);
            set.put(biEdgeArrowColorProp);

        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }

        sheet.put(set);
        return sheet;
    }
}
