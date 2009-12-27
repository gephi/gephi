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
        EdgeSupervisor unes = controller.getUndirectedEdgeSupervisor();
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

        Sheet.Set undirectedEdgeSet = Sheet.createPropertiesSet();
        undirectedEdgeSet.setDisplayName("Undirected Edge Settings");
        undirectedEdgeSet.setName("undirectedEdges");

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
                        createProperty(sls, EdgeColorizer.class, "colorizer", "selfLoopColor", "Self-Loop Color", EdgeColorizerPropertyEditor.class)
                    });

            undirectedEdgeSet.put(new Property[]{
                        createProperty(unes, Boolean.class, "curvedFlag", "curvedUndirectedEdges", "Curved Undirected Edges"),
                        createProperty(unes, EdgeColorizer.class, "colorizer", "undirectedEdgeColor", "Undirected Edge Color", EdgeColorizerPropertyEditor.class),
                        createProperty(unes, Boolean.class, "showLabelsFlag", "showUndirectedEdgeLabels", "Show Undirected Edge Labels"),
                        createProperty(unes, Integer.class, "labelMaxChar", "undirectedEdgeLabelMaxChar", "Undirected Edge Label Char Limit"),
                        createProperty(unes, Font.class, "labelFont", "undirectedEdgeLabelFont", "Undirected Edge Label Font"),
                        createProperty(unes, EdgeChildColorizer.class, "labelColorizer", "undirectedEdgeLabelColor", "Undirected Edge Label Color", EdgeChildColorizerPropertyEditor.class)
                    });

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
                        createProperty(bes, Boolean.class, "curvedFlag", "curvedBiEdges", "Curved Bi. Edges"),
                        createProperty(bes, EdgeColorizer.class, "colorizer", "biEdgeColor", "Bi. Edge Color", EdgeColorizerPropertyEditor.class),
                        createProperty(bes, Boolean.class, "showLabelsFlag", "showBiEdgeLabels", "Show Bi. Edge Labels"),
                        createProperty(bes, Integer.class, "labelMaxChar", "biEdgeLabelMaxChar", "Bi. Edge Label Char Limit"),
                        createProperty(bes, Font.class, "labelFont", "biEdgeLabelFont", "Bi. Edge Label Font"),
                        createProperty(bes, EdgeChildColorizer.class, "labelColorizer", "biEdgeLabelColor", "Bi. Edge Label Color", EdgeChildColorizerPropertyEditor.class),
                        createProperty(bes, Boolean.class, "showMiniLabelsFlag", "showBiEdgeMiniLabels", "Show Bi. Edge Mini-Labels"),
                        createProperty(bes, Float.class, "miniLabelAddedRadius", "biEdgeMiniLabelAddedRadius", "Bi. Edge Mini-Label Added Radius"),
                        createProperty(bes, Integer.class, "miniLabelMaxChar", "biEdgeMiniLabelMaxChar", "Bi. Edge Mini-Label Char Limit"),
                        createProperty(bes, Font.class, "miniLabelFont", "biEdgeMiniLabelFont", "Bi. Edge Mini-Label Font"),
                        createProperty(bes, EdgeChildColorizer.class, "miniLabelColorizer", "biEdgeMiniLabelColor", "Bi. Edge Mini-Label Color", EdgeChildColorizerPropertyEditor.class),
                        createProperty(bes, Boolean.class, "showArrowsFlag", "showBiEdgeArrows", "Show Bi. Edge Arrows"),
                        createProperty(bes, Float.class, "arrowAddedRadius", "biEdgeArrowAddedRadius", "Bi. Edge Arrow Added Radius"),
                        createProperty(bes, Float.class, "arrowSize", "BiEdgeArrowSize", "Bi. Edge Arrow Size"),
                        createProperty(bes, EdgeChildColorizer.class, "arrowColorizer", "biEdgeArrowColor", "Bi. Edge Arrow Color", EdgeChildColorizerPropertyEditor.class)
                    });

        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }

        Sheet sheet = Sheet.createDefault();
        sheet.put(nodeSet);
        sheet.put(edgeSet);
        sheet.put(selfLoopSet);
        sheet.put(undirectedEdgeSet);
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
