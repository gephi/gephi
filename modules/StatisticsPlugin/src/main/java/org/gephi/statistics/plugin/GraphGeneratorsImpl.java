/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Anna
 */

@ServiceProvider(service = GraphGenerator.class)
public class GraphGeneratorsImpl implements GraphGenerator {

    @Override
    public GraphModel generateNullUndirectedGraph(int n) {
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
        Node[] nodes=new Node[n];
        for (int i=0; i<n; i++) {
             Node currentNode=graphModel.factory().newNode(((Integer)i).toString());
             undirectedGraph.addNode(currentNode);
        }
        return graphModel;
    }

    @Override
    public GraphModel generateCompleteUndirectedGraph(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphModel generatePathUndirectedGraph(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphModel generateCyclicUndirectedGraph(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphModel generateStarUndirectedGraph(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphModel generateNullDirectedGraph(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphModel generateCompleteDirectedGraph(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphModel generatePathDirectedGraph(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GraphModel generateCyclicDirectedGraph(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
  
}
