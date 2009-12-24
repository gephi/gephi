package org.gephi.ui.preview;

import org.gephi.ui.preview.propertyeditors.NodeChildColorizerPropertyEditor;
import org.gephi.ui.preview.propertyeditors.GenericColorizerPropertyEditor;
import org.gephi.ui.preview.propertyeditors.NodeColorizerPropertyEditor;
import java.awt.Font;
import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.GenericColorizer;
import org.gephi.preview.api.NodeChildColorizer;
import org.gephi.preview.api.NodeColorizer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.supervisors.EdgeSupervisor;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisors.NodeSupervisor;
import org.gephi.preview.api.supervisors.SelfLoopSupervisor;
import org.gephi.ui.preview.propertyeditors.EdgeChildColorizerPropertyEditor;
import org.gephi.ui.preview.propertyeditors.EdgeColorizerPropertyEditor;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 * This class provides some sets of properties for the preview UI.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class PreviewNode extends AbstractNode {

    public PreviewNode() {
        super(Children.LEAF);
        setDisplayName("Preview Settings");
    }

    @Override
    protected Sheet createSheet() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        NodeSupervisor ns = controller.getNodeSupervisor();
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

            nodeSet.put(new Property[]{
                        createProperty(ns, Boolean.class, "showNodes", "showNodes", "Show Nodes"),
                        createProperty(ns, Float.class, "nodeBorderWidth", "nodeBorderWidth", "Node Border Width"),
                        createProperty(ns, NodeColorizer.class, "nodeColorizer", "nodeColor", "Node Color", NodeColorizerPropertyEditor.class),
                        createProperty(ns, GenericColorizer.class, "nodeBorderColorizer", "nodeBorderColor", "Node Border Color", GenericColorizerPropertyEditor.class),
                        createProperty(ns, Boolean.class, "showNodeLabels", "showNodeLabels", "Show Node Labels"),
                        createProperty(ns, Font.class, "nodeLabelFont", "nodeLabelFont", "Node Label Font"),
                        createProperty(ns, Integer.class, "nodeLabelMaxChar", "nodeLabelMaxChar", "Node Label Char Limit"),
                        createProperty(ns, NodeChildColorizer.class, "nodeLabelColorizer", "nodeLabelColor", "Node Label Color", NodeChildColorizerPropertyEditor.class),
                        createProperty(ns, Boolean.class, "showNodeLabelBorders", "showNodeLabelBorders", "Bordered Node Labels"),
                        createProperty(ns, NodeChildColorizer.class, "nodeLabelBorderColorizer", "nodeLabelBorderColor", "Node Label Border Color", NodeChildColorizerPropertyEditor.class)
                    });

            edgeSet.put(createProperty(ges, Boolean.class, "showFlag", "showEdges", "Show Edges"));

            selfLoopSet.put(new Property[]{
                        createProperty(sls, Boolean.class, "showFlag", "showSelfLoops", "Show Self-Loops"),
                        createProperty(sls, EdgeColorizer.class, "colorizer", "selfLoopColor", "Self-Loop Color", EdgeColorizerPropertyEditor.class),});

            uniEdgeSet.put(new Property[]{
                        createProperty(ues, Boolean.class, "curvedFlag", "curvedUniEdges", "Curved Uni. Edges"),
                        createProperty(ues, EdgeColorizer.class, "colorizer", "uniEdgeColor", "Uni. Edge Color", EdgeColorizerPropertyEditor.class),
                        createProperty(ues, Boolean.class, "showLabelsFlag", "showUniEdgeLabels", "Show Uni. Edge Labels"),
                        createProperty(ues, Integer.class, "labelMaxChar", "uniEdgeLabelMaxChar", "Uni. Edge Label Char Limit"),
                        createProperty(ues, Font.class, "labelFont", "uniEdgeLabelFont", "Uni. Edge Label Font"),
                        createProperty(ues, EdgeChildColorizer.class, "labelColorizer", "uniEdgeLabelColor", "Uni. Edge Label Color", EdgeChildColorizerPropertyEditor.class),
                        createProperty(ues, Boolean.class, "showMiniLabelsFlag", "showUniEdgeMiniLabels", "Show Uni. Edge Mini-Labels"),
                        createProperty(ues, Float.class, "miniLabelAddedRadius", "uniEdgeMiniLabelAddedRadius", "Uni. Edge Mini-Label Added Radius"),
                        createProperty(ues, Integer.class, "miniLabelMaxChar", "uniEdgeMiniLabelMaxChar", "Uni. Edge Mini-Label Char Limit"),
                        createProperty(ues, Font.class, "miniLabelFont", "uniEdgeMiniLabelFont", "Uni. Edge Mini-Label Font"),
                        createProperty(ues, EdgeChildColorizer.class, "miniLabelColorizer", "uniEdgeMiniLabelColor", "Uni. Edge Mini-Label Color", EdgeChildColorizerPropertyEditor.class),
                        createProperty(ues, Boolean.class, "showArrowsFlag", "showUniEdgeArrows", "Show Uni. Edge Arrows"),
                        createProperty(ues, Float.class, "arrowAddedRadius", "uniEdgeArrowAddedRadius", "Uni. Edge Arrow Added Radius"),
                        createProperty(ues, Float.class, "arrowSize", "uniEdgeArrowSize", "Uni. Edge Arrow Size"),
                        createProperty(ues, EdgeChildColorizer.class, "arrowColorizer", "uniEdgeArrowColor", "Uni. Edge Arrow Color", EdgeChildColorizerPropertyEditor.class)
                    });

            biEdgeSet.put(new Property[]{
                        createProperty(bes, Boolean.class, "curvedFlag", "curvedUniEdges", "Curved Bi. Edges"),
                        createProperty(bes, EdgeColorizer.class, "colorizer", "uniEdgeColor", "Bi. Edge Color", EdgeColorizerPropertyEditor.class),
                        createProperty(bes, Boolean.class, "showLabelsFlag", "showUniEdgeLabels", "Show Bi. Edge Labels"),
                        createProperty(bes, Integer.class, "labelMaxChar", "uniEdgeLabelMaxChar", "Bi. Edge Label Char Limit"),
                        createProperty(bes, Font.class, "labelFont", "uniEdgeLabelFont", "Bi. Edge Label Font"),
                        createProperty(bes, EdgeChildColorizer.class, "labelColorizer", "uniEdgeLabelColor", "Bi. Edge Label Color", EdgeChildColorizerPropertyEditor.class),
                        createProperty(bes, Boolean.class, "showMiniLabelsFlag", "showUniEdgeMiniLabels", "Show Bi. Edge Mini-Labels"),
                        createProperty(bes, Float.class, "miniLabelAddedRadius", "uniEdgeMiniLabelAddedRadius", "Bi. Edge Mini-Label Added Radius"),
                        createProperty(bes, Integer.class, "miniLabelMaxChar", "uniEdgeMiniLabelMaxChar", "Bi. Edge Mini-Label Char Limit"),
                        createProperty(bes, Font.class, "miniLabelFont", "uniEdgeMiniLabelFont", "Bi. Edge Mini-Label Font"),
                        createProperty(bes, EdgeChildColorizer.class, "miniLabelColorizer", "uniEdgeMiniLabelColor", "Bi. Edge Mini-Label Color", EdgeChildColorizerPropertyEditor.class),
                        createProperty(bes, Boolean.class, "showArrowsFlag", "showUniEdgeArrows", "Show Bi. Edge Arrows"),
                        createProperty(bes, Float.class, "arrowAddedRadius", "uniEdgeArrowAddedRadius", "Bi. Edge Arrow Added Radius"),
                        createProperty(bes, Float.class, "arrowSize", "uniEdgeArrowSize", "Bi. Edge Arrow Size"),
                        createProperty(bes, EdgeChildColorizer.class, "arrowColorizer", "uniEdgeArrowColor", "Bi. Edge Arrow Color", EdgeChildColorizerPropertyEditor.class)
                    });

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

    private PropertySupport.Reflection createProperty(Object o, Class type, String method, String name, String displayName) throws NoSuchMethodException {
        PropertySupport.Reflection p = new PropertySupport.Reflection(o, type, method);
        p.setName(name);
        p.setDisplayName(displayName);
        return p;
    }

    private PropertySupport.Reflection createProperty(Object o, Class type, String method, String name, String displayName, Class editor) throws NoSuchMethodException {
        PropertySupport.Reflection p = createProperty(o, type, method, name, displayName);
        p.setPropertyEditorClass(editor);
        return p;
    }
}
