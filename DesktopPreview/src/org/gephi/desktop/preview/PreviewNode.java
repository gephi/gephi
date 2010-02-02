package org.gephi.desktop.preview;

import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.SupervisorPropery;
import org.gephi.preview.api.supervisors.EdgeSupervisor;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisors.NodeSupervisor;
import org.gephi.preview.api.supervisors.SelfLoopSupervisor;
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
        Sheet sheet = Sheet.createDefault();
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel model = controller.getModel();
        if (model != null) {
            NodeSupervisor ns = model.getNodeSupervisor();
            GlobalEdgeSupervisor ges = model.getGlobalEdgeSupervisor();
            SelfLoopSupervisor sls = model.getSelfLoopSupervisor();
            EdgeSupervisor unes = model.getUndirectedEdgeSupervisor();
            EdgeSupervisor ues = model.getUniEdgeSupervisor();
            EdgeSupervisor bes = model.getBiEdgeSupervisor();

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

            for (SupervisorPropery p : ns.getProperties()) {
                nodeSet.put(p.getProperty());
            }

            for (SupervisorPropery p : ges.getProperties()) {
                edgeSet.put(p.getProperty());
            }

            for (SupervisorPropery p : sls.getProperties()) {
                selfLoopSet.put(p.getProperty());
            }

            for (SupervisorPropery p : unes.getProperties()) {
                undirectedEdgeSet.put(p.getProperty());
            }

            for (SupervisorPropery p : ues.getProperties()) {
                uniEdgeSet.put(p.getProperty());
            }

            for (SupervisorPropery p : bes.getProperties()) {
                biEdgeSet.put(p.getProperty());
            }

            sheet.put(nodeSet);
            sheet.put(edgeSet);
            sheet.put(selfLoopSet);
            sheet.put(undirectedEdgeSet);
            sheet.put(uniEdgeSet);
            sheet.put(biEdgeSet);
        }
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
