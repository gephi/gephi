/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ranking.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingUIModel {

    //Const
    public static final int NODE_RANKING = 1;
    public static final int EDGE_RANKING = 2;
    //Model
    protected int ranking;
    protected boolean barChartVisible;
    protected boolean listVisible;
    protected Class nodeTransformer;
    protected Class edgeTransformer;
    protected List<Transformer> nodeTransformers;
    protected List<Transformer> edgeTransformers;
    protected String selectedNodeRanking;
    protected String selectedEdgeRanking;
    //Listener
    protected List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    public RankingUIModel() {
        //DefaultValues
        ranking = NODE_RANKING;
        barChartVisible = false;
        listVisible = false;
        nodeTransformers = new ArrayList<Transformer>();
        edgeTransformers = new ArrayList<Transformer>();
    }

    public Class getNodeTransformer() {
        return nodeTransformer;
    }

    public Class getEdgeTransformer() {
        return edgeTransformer;
    }

    public String getSelectedEdgeRanking() {
        return selectedEdgeRanking;
    }

    public String getSelectedNodeRanking() {
        return selectedNodeRanking;
    }

    public void setNodeTransformer(Class nodeTransformer) {
        if (this.nodeTransformer != null && this.nodeTransformer.equals(nodeTransformer)) {
            return;
        }
        Class oldValue = this.nodeTransformer;
        this.nodeTransformer = nodeTransformer;
        firePropertyChangeEvent("nodeTransformer", oldValue, nodeTransformer);
    }

    public void setEdgeTransformer(Class edgeTransformer) {
        if (this.edgeTransformer != null && this.edgeTransformer.equals(edgeTransformer)) {
            return;
        }
        Class oldValue = this.edgeTransformer;
        this.edgeTransformer = edgeTransformer;
        firePropertyChangeEvent("edgeTransformer", oldValue, edgeTransformer);
    }

    public void setSelectedEdgeRanking(String selectedEdgeRanking) {
        if (this.selectedEdgeRanking != null && this.selectedEdgeRanking.equals(selectedEdgeRanking)) {
            return;
        }
        if (this.selectedEdgeRanking == null && selectedEdgeRanking == null) {
            return;
        }
        String oldValue = this.selectedEdgeRanking;
        this.selectedEdgeRanking = selectedEdgeRanking;
        firePropertyChangeEvent("selectedEdgeRanking", oldValue, selectedEdgeRanking);
    }

    public void setSelectedNodeRanking(String selectedNodeRanking) {
        if (this.selectedNodeRanking != null && this.selectedNodeRanking.equals(selectedNodeRanking)) {
            return;
        }
        if (this.selectedNodeRanking == null && selectedNodeRanking == null) {
            return;
        }
        String oldValue = this.selectedNodeRanking;
        this.selectedNodeRanking = selectedNodeRanking;
        firePropertyChangeEvent("selectedNodeRanking", oldValue, selectedNodeRanking);
    }

    public Transformer getSelectedNodeTransformer() {
        if (nodeTransformer != null) {
            for (Transformer t : nodeTransformers) {
                if (nodeTransformer.isAssignableFrom(t.getClass())) {
                    return t;
                }
            }
        }
        return null;
    }

    public Transformer getSelectedEdgeTransformer() {
        if (edgeTransformer != null) {
            for (Transformer t : edgeTransformers) {
                if (edgeTransformer.isAssignableFrom(t.getClass())) {
                    return t;
                }
            }
        }
        return null;
    }

    public void addNodeTransformer(Transformer transformer) {
        nodeTransformers.add(transformer);
    }

    public void addEdgeTransformer(Transformer transformer) {
        edgeTransformers.add(transformer);
    }

    public void resetNodeTransformers() {
        nodeTransformers.clear();
    }

    public void resetEdgeTransformers() {
        edgeTransformers.clear();
    }

    public int getRanking() {
        return ranking;
    }

    public boolean isBarChartVisible() {
        return barChartVisible;
    }

    public boolean isListVisible() {
        return listVisible;
    }

    public RankingUIModel saveModel() {
        RankingUIModel save = new RankingUIModel();
        save.barChartVisible = this.barChartVisible;
        save.edgeTransformer = this.edgeTransformer;
        save.listVisible = this.listVisible;
        save.nodeTransformer = this.nodeTransformer;
        save.ranking = this.ranking;
        save.selectedNodeRanking = this.selectedNodeRanking;
        save.selectedEdgeRanking = this.selectedEdgeRanking;
        save.nodeTransformers.addAll(this.nodeTransformers);
        save.edgeTransformers.addAll(this.edgeTransformers);
        return save;
    }

    public void loadModel(RankingUIModel model) {
        this.nodeTransformers.clear();
        this.edgeTransformers.clear();
//        this.barChartVisible = model.barChartVisible;
//        this.edgeTransformer = model.edgeTransformer;
//        this.listVisible = model.listVisible;
//        this.nodeTransformer = model.nodeTransformer;
//        this.ranking = model.ranking;
//        this.selectedNodeRanking = model.selectedNodeRanking;
//        this.selectedEdgeRanking = model.selectedEdgeRanking;
        this.nodeTransformers.addAll(model.nodeTransformers);
        this.edgeTransformers.addAll(model.edgeTransformers);
        setBarChartVisible(model.barChartVisible);
        setListVisible(model.listVisible);
        setNodeTransformer(model.nodeTransformer);
        setEdgeTransformer(model.edgeTransformer);
        setRanking(model.ranking);
        setSelectedEdgeRanking(model.selectedEdgeRanking);
        setSelectedNodeRanking(model.selectedNodeRanking);
    }

    public void setListVisible(boolean listVisible) {
        if (this.listVisible == listVisible) {
            return;
        }
        boolean oldValue = this.listVisible;
        this.listVisible = listVisible;
        firePropertyChangeEvent("listVisible", oldValue, listVisible);
    }

    public void setBarChartVisible(boolean barChartVisible) {
        if (this.barChartVisible == barChartVisible) {
            return;
        }
        boolean oldValue = this.barChartVisible;
        this.barChartVisible = barChartVisible;
        firePropertyChangeEvent("barChartVisible", oldValue, barChartVisible);
    }

    public void setRanking(int ranking) {
        if (ranking != NODE_RANKING && ranking != EDGE_RANKING) {
            throw new IllegalArgumentException("Ranking must be NODE_RANKING or EDGE_RANKING");
        }
        if (this.ranking == ranking) {
            return;
        }
        int oldValue = this.ranking;
        this.ranking = ranking;
        firePropertyChangeEvent("ranking", oldValue, ranking);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private void firePropertyChangeEvent(String propertyName, Object beforeValue, Object afterValue) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, beforeValue, afterValue);
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(event);
        }
    }

    //XML
    public void readXML(Element modelElement) {
        Element rankingE = (Element) modelElement.getElementsByTagName("ranking").item(0);
        ranking = Integer.parseInt(rankingE.getTextContent());

        Element barchartvisibleE = (Element) modelElement.getElementsByTagName("barchartvisible").item(0);
        barChartVisible = Boolean.parseBoolean(barchartvisibleE.getTextContent());

        Element listvisibleE = (Element) modelElement.getElementsByTagName("listvisible").item(0);
        listVisible = Boolean.parseBoolean(listvisibleE.getTextContent());

    }

    public Element writeXML(Document document) {
        Element rankingModelE = document.createElement("rankinguimodel");

        Element rankingE = document.createElement("ranking");
        rankingE.setTextContent(String.valueOf(ranking));
        rankingModelE.appendChild(rankingE);

        Element barChartE = document.createElement("barchartvisible");
        barChartE.setTextContent(String.valueOf(barChartVisible));
        rankingModelE.appendChild(barChartE);

        Element listE = document.createElement("listvisible");
        listE.setTextContent(String.valueOf(listVisible));
        rankingModelE.appendChild(barChartE);

        /*Element nodeTransformerE = document.createElement("nodetransformer");
        nodeTransformerE.setTextContent(String.valueOf(nodeTransformer));
        rankingModelE.appendChild(nodeTransformerE);

        Element edgeTransformerE = document.createElement("edgetransformer");
        edgeTransformerE.setTextContent(String.valueOf(edgeTransformer));
        rankingModelE.appendChild(edgeTransformerE);*/

        return rankingModelE;
    }
}
