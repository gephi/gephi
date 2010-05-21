/*
 * Author: Patrick J. McSweeney
 * Syracuse University
 */

package org.gephi.statistics.plugin;

import java.util.Hashtable;
import java.util.LinkedList;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.NodeIterator;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author pjmcswee
 */
public class ConnectedComponents implements Statistics, LongTask {
    private boolean mDirected;
    private ProgressTicket mProgress;
    private boolean mIsCanceled;
    private String mGraphRevision;
    private int mComponentCount;

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        mIsCanceled = false;
        mComponentCount = 0;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn componentCol = nodeTable.getColumn("componentnumber");
        if (componentCol == null) {
            componentCol = nodeTable.addColumn("componentnumber", "Component ID", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }

        Graph graph = null;
        if (mDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }

        graph.readLock();
        mGraphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";
        
        Hashtable<Node, Integer> indicies = new Hashtable<Node, Integer>();
        int index = 0;
        for (Node s : graph.getNodes()) {
            indicies.put(s, index);
            index++;
        }
        
        
        int N = graph.getNodeCount();
   
        //Keep track of which nodes have been seen
        int[] color = new int[N];
        
        Progress.start(mProgress, graph.getNodeCount());
        int seenCount = 0;       
        while(seenCount < N)
        {
            //The search Q
            LinkedList<Node> Q = new LinkedList<Node>();
            //The component-list
            LinkedList<Node> component = new LinkedList<Node>();

            //Seed the seach Q
            for(Node first : graph.getNodes())
            {               
                if(color[indicies.get(first)] == 0)
                {
                    Q.add(first);
                    
                    break;
                }
            }

            //While there are more nodes to search
            while (!Q.isEmpty()) {
                if (mIsCanceled) {
                    graph.readUnlock();
                    return;
                }
                //Get the next Node and add it to the component list
                Node u = Q.removeFirst();
                component.add(u);

                //Iterate over all of u's neighbors
                EdgeIterable edgeIter = null;
                if (mDirected) {
                    edgeIter = ((DirectedGraph) graph).getOutEdges(u);
                } else {
                    edgeIter = graph.getEdges(u);
                }
                //For each neighbor
                for (Edge edge : edgeIter) {
                    Node reachable = graph.getOpposite(u, edge);
                    int id = indicies.get(reachable);
                    //If this neighbor is unvisited
                    if(color[id] == 0){
                        color[id] = 1;
                        //Add it to the search Q
                        Q.addLast(reachable);
                        //Mark it as used 
                        
                        Progress.progress(mProgress, seenCount);
                    }
                }
                color[indicies.get(u)] = 2;
                seenCount++;
            }
            for (Node s : component) {
                AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
                row.setValue(componentCol, mComponentCount);
            }
            mComponentCount++;
        }
        graph.readUnlock();
    }

  public int getConnectedComponentsCount()
  {
      return mComponentCount;
  }
  /**
     *
     * @param pDirected
     */
    public void setDirected(boolean pDirected) {
        this.mDirected = pDirected;
    }

    public boolean isDirected() {
        return mDirected;
    }

    /**
     *
     * @return
     */
    public String getReport() {

        String report = new String("<HTML> <BODY> <h1>Graph Distance  Report </h1> "
                + "<hr> <br> <h2>Network Revision Number:</h2>"
                + mGraphRevision
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (this.mDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Connected Components: " + mComponentCount + "<br>"
                + "</BODY></HTML>");

        return report;
    }

    /**
     *
     * @return
     */
    public boolean cancel() {
        mIsCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        mProgress = progressTicket;
    }
}
