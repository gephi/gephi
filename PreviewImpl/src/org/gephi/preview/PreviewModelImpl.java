/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.preview;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.SupervisorPropery;
import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisors.NodeSupervisor;
import org.gephi.preview.api.supervisors.SelfLoopSupervisor;
import org.gephi.preview.api.supervisors.Supervisor;
import org.gephi.preview.api.supervisors.UndirectedEdgeSupervisor;
import org.gephi.preview.supervisors.BidirectionalEdgeSupervisorImpl;
import org.gephi.preview.supervisors.GlobalEdgeSupervisorImpl;
import org.gephi.preview.supervisors.NodeSupervisorImpl;
import org.gephi.preview.supervisors.SelfLoopSupervisorImpl;
import org.gephi.preview.supervisors.UndirectedEdgeSupervisorImpl;
import org.gephi.preview.supervisors.UnidirectionalEdgeSupervisorImpl;
import org.gephi.workspace.api.Workspace;

/**
 *
 * @author Mathieu Bastian
 */
public class PreviewModelImpl implements PreviewModel, GraphListener {

    //Supervisors
    private final NodeSupervisorImpl nodeSupervisor;
    private final GlobalEdgeSupervisorImpl globalEdgeSupervisor;
    private final SelfLoopSupervisorImpl selfLoopSupervisor;
    private final UnidirectionalEdgeSupervisorImpl uniEdgeSupervisor;
    private final BidirectionalEdgeSupervisorImpl biEdgeSupervisor;
    private final UndirectedEdgeSupervisorImpl undirectedEdgeSupervisor;
    //States
    private boolean updateFlag = true;
    private Map<String, Object> propertiesValues;
    private float visibilityRatio = 1;

    public PreviewModelImpl() {
        propertiesValues = new HashMap<String, Object>();
        nodeSupervisor = new NodeSupervisorImpl();
        globalEdgeSupervisor = new GlobalEdgeSupervisorImpl();
        selfLoopSupervisor = new SelfLoopSupervisorImpl();
        uniEdgeSupervisor = new UnidirectionalEdgeSupervisorImpl();
        biEdgeSupervisor = new BidirectionalEdgeSupervisorImpl();
        undirectedEdgeSupervisor = new UndirectedEdgeSupervisorImpl();
    }

    public void select(Workspace workspace) {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        graphModel.addGraphListener(this);
    }

    public void unselect(Workspace workspace) {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        graphModel.removeGraphListener(this);
    }

    /**
     * Sets the update flag when the structure of the workspace graph has
     * changed.
     *
     * @see GraphListener#graphChanged(org.gephi.graph.api.GraphEvent)
     */
    public void graphChanged(GraphEvent event) {
        updateFlag = true;
    }

    public void loadProperties(Supervisor[] supervisors) {
        for (Supervisor s : supervisors) {
            for (SupervisorPropery p : s.getProperties()) {
                String propertyName = p.getProperty().getName();
                Object propertyValue = propertiesValues.get(propertyName);
                if (propertyValue != null) {
                    try {
                        p.getProperty().setValue(propertyValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void saveProperties(Supervisor[] supervisors) {
        for (Supervisor s : supervisors) {
            for (SupervisorPropery p : s.getProperties()) {
                String propertyName = p.getProperty().getName();
                try {
                    Object propertyValue = p.getProperty().getValue();
                    if (propertyValue != null) {
                        propertiesValues.put(propertyName, propertyValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Clears the supervisors' lists of supervised elements.
     */
    public void clearSupervisors() {
        nodeSupervisor.clearSupervised();
        globalEdgeSupervisor.clearSupervised();
        selfLoopSupervisor.clearSupervised();
        uniEdgeSupervisor.clearSupervised();
        biEdgeSupervisor.clearSupervised();
    }

    public NodeSupervisor getNodeSupervisor() {
        return nodeSupervisor;
    }

    public GlobalEdgeSupervisor getGlobalEdgeSupervisor() {
        return globalEdgeSupervisor;
    }

    public SelfLoopSupervisor getSelfLoopSupervisor() {
        return selfLoopSupervisor;
    }

    public DirectedEdgeSupervisor getUniEdgeSupervisor() {
        return uniEdgeSupervisor;
    }

    public DirectedEdgeSupervisor getBiEdgeSupervisor() {
        return biEdgeSupervisor;
    }

    public UndirectedEdgeSupervisor getUndirectedEdgeSupervisor() {
        return undirectedEdgeSupervisor;
    }

    public boolean isUpdateFlag() {
        return updateFlag;
    }

    public void setUpdateFlag(boolean updateFlag) {
        this.updateFlag = updateFlag;
    }

    public float getVisibilityRatio() {
        return visibilityRatio;
    }

    public void setVisibilityRatio(float visibilityRatio) {
        this.visibilityRatio = visibilityRatio;
    }
}
