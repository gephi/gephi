/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.timeline;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Edge;
import org.gephi.timeline.api.TimelineController;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.project.api.Workspace;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jbilcke
 */
@ServiceProvider(service = TimelineController.class)
public class TimelineControllerImpl implements TimelineController {

    public void pushSlice(Workspace workspace, String from, String to, Node node) {

        AttributeModel am = workspace.getLookup().lookup(AttributeModel.class);
        AttributeColumn col = am.getNodeTable().addColumn("dynmicrange", AttributeType.TIME_INTERVAL);
        node.getNodeData().getAttributes().setValue(col.getIndex(), new TimeInterval(0.0, 52.0));

    }

    public void pushSlice(Workspace workspace, String from, String to, Edge edge) {
    }

    public TimelineModel getModel(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Double getFromDouble(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Double getToDouble(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TimeInterval getRangeTimeInterval(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
