/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.timeline;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Edge;
import org.gephi.workspace.api.Workspace;

/**
 *
 * @author jbilcke
 */
public class TimelineControllerImpl {

    public void pushSlice(Workspace workspace, String from, String to, Node node) {

        AttributeModel am = workspace.getLookup().lookup(AttributeModel.class);
        AttributeColumn col = am.getNodeTable().addColumn("dynmicrange", AttributeType.TIME_INTERVAL);

        node.getNodeData().getAttributes().setValue(col.getIndex(), new TimeInterval(0., 52.));

    }

    public void pushSlice(Workspace workspace, String from, String to, Edge edge) {
    }
}
