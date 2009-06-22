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

package org.gephi.statistics;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import org.gephi.statistics.api.Statistics;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Edge;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.NodeIterable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;






/**
 *
 * @author pjmcswee
 */
class Renumbering implements Comparator<ArrayWrapper>
{
    public int compare(ArrayWrapper o1, ArrayWrapper o2) {
        if(o1.getID() < o2.getID())
        {
            return -1;
        }
        else
            return 1;
    }  
}
/**
 * 
 * @author pjmcswee
 */
class ArrayWrapper implements Comparable{
    private ArrayWrapper[] mArray;
    private int mID;

    /** Empty Constructor/ */
    ArrayWrapper(){}

    /**
     *
     * @return The ID of this array wrapper
     */
    public int getID(){return mID;}

    /**
     *
     * @return The adjacency array
     */
    public ArrayWrapper[] getArray() { return mArray;}
    public void setArray(ArrayWrapper[] pArray)
    {  mArray = pArray;}

    /**
     *
     * @param pArray
     */
    ArrayWrapper(int pID, ArrayWrapper[] pArray) {
        mArray = pArray;
        mID = pID;
    }


    public void setID(int pID)
    {
        mID = pID;
    }
    /**
     *
     * @param pIndex
     * @return
     */
    public int get(int pIndex) {
       if(pIndex >= mArray.length) {
            return -1;
       }

       return mArray[pIndex].mID;
    }


    /**
     * 
     * @return
     */
    public int length() {
        return mArray.length;
    }

    /**
     * 
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        ArrayWrapper aw = (ArrayWrapper)o;
        if(aw.length() < length())
            return -1;
        if(aw.length() > length())
            return 1;
        return 0;
    }


}
class ClusteringThread implements Runnable {

    public static int[] mTriangles;
    private static ArrayWrapper[] mNetwork;
    private int mStart;
    private int mFinish;
    private int mId;
    private int mN;
    private static int mK;


    /**
     * Initialize the threaded clustering coefficient.
     * @param pNodes
     */
    public static void init(ArrayWrapper[] pNetwork){
        mNetwork = pNetwork;
        mTriangles = new int[pNetwork.length];
        mK = (int)Math.sqrt(mNetwork.length);
    }



    /**
     *
     * @param pStart
     * @param pFinish
     */
    ClusteringThread(int pId, int pStart, int pFinish){
        mStart = pStart;
        mFinish = pFinish;
        mId = pId;
        mN = mNetwork.length;
    }

    /**
     * 
     * @param v
     * @return
     */
    public int closest_in_array(int v){
        //int right = g->degrees[v]-1;
        int right = mNetwork[v].length() - 1;
 
        /* optimization for extreme cases */
        if (right<0) return(-1);
        if (mNetwork[v].get(0) >= v )  return(-1);
        if (mNetwork[v].get(right) < v) return(right);
        if (mNetwork[v].get(right) == v) return(right-1);

        int left = 0, mid;
        while (right>left) {
            mid = (left+right)/2;
            
            if (v < mNetwork[v].get(mid))
                right = mid-1;
            else if (v > mNetwork[v].get(mid))
                left = mid+1;
            else return(mid-1);
       }
        
        if (v>mNetwork[v].get(right))
            return(right);
        else
            return(right-1);
 }


    



    /**
     *
     * @param v - The specific node to count the triangles on.
     */
    public void newVertex(int v){
        boolean[] A = new boolean [mN];

        if(v == 0)
            System.out.println("Test");
      
        for(int i = mNetwork[v].length() - 1; (i >= 0)  && (mNetwork[v].get(i) > v); i--){
            int neighbor = mNetwork[v].get(i);
            A[neighbor] = true;
        }


        for(int i = mNetwork[v].length() - 1; i >= 0; i--) {
        
            int neighbor = mNetwork[v].get(i);

            for(int j = closest_in_array(neighbor); j >= 0; j--)
            {
                int next = mNetwork[neighbor].get(j);
                if(A[next])
                {
                    mTriangles[next]++;
                    mTriangles[v]++;
                    mTriangles[neighbor]++;
                }
            }
        }
    }


    /**
     * 
     * @param u
     * @param v
     */
    public void tr_link_nohigh(int u, int v){
        int iu = 0, iv = 0, w;

        while ((iu < mNetwork[u].length()) && (iv < mNetwork[v].length())) {
            if (mNetwork[u].get(iu) < mNetwork[v].get(iv)) {
                iu++;
            }
            else if (mNetwork[u].get(iu) > mNetwork[v].get(iv)) {
                iv++;
            }
            else { /* neighbor in common */
                w = mNetwork[u].get(iu);
                if (w >= mK)
                    mTriangles[w]++;
                iu++;
                iv++;
            }
        }
    }



    /**
     *
     */
    public void run(){
       
            for (int v = mStart; v < mK && v < mFinish; v++) {
                newVertex(v);
            }
            
            /* remaining links */
            for (int v = Math.min(mFinish-1,mNetwork.length - 1); (v >= mStart) && (v >= mK); v--) {
                for (int i = closest_in_array(v); i >= 0; i--) {
                    int u = mNetwork[v].get(i);
                    if (u >= mK) {
                        tr_link_nohigh(u,v);
                    }
                }
            }
    }
}


/**
 *
 * @author Patrick J. McSweeney
 */
public class ClusteringCoefficient implements Statistics {


    private double avgClusteringCoeff;
    private boolean useBruteForce;
    private boolean undirectedOverride;
    private boolean isCanceled;

    public void confirm()
    {
    }
    public String toString(){

        return new String("Clustering Coefficient");
    }


    /**
     * 
     * @return
     */
     public String getName() {
        return NbBundle.getMessage(GraphDensity.class, "GraphDensity_name");
    }

    /**
     *
     * @param synchReader
     */
    public void execute(GraphController graphController,
            ProgressMonitor progressMonitor) {
        isCanceled = false;
        
        DirectedGraph digraph = graphController.getDirectedGraph();
        
        
        if(useBruteForce){
            bruteForce(graphController);
            return;
        }

    
        int N = digraph.getNodeCount();
        Node[] nodes = new Node[N];

        /** Create network for processing */
        ArrayWrapper[] network = new ArrayWrapper[N];

        /**  */
        for(int i = 0; i < N; i++)
        {   network[i] = new ArrayWrapper();}
      

/*
        for(Node node : digraph.getNodes())
        {
            ArrayWrapper[] edges = new ArrayWrapper[digraph.getOutDegree(node) + digraph.getInDegree(node)];
            int j = 0;
            
            for(Edge edge : digraph.getOutEdges(node)) {
                digraph.get
                Node neighbor = edge.getTarget();
                if(neighbor.getIndex() == node.getIndex()) {
                    neighbor = edge.getSource();
                }
                edges[j] = network[neighbor.getIndex()-1];
                j++;
            }
            Iterator<? extends EdgeWrap> iter2 =  node.getEdgesIn().iterator();
            while(iter2.hasNext())
            {
                Edge edge = iter2.next().getEdge();
                Node neighbor = edge.getTarget();
                if(neighbor.getIndex() == node.getIndex()) {
                    neighbor = edge.getSource();
                }
                edges[j] = network[neighbor.getIndex()-1];
                j++;
            }

            network[i].setArray(edges);
            i++;
        }
        
        Arrays.sort(network);
        for(int j = 0; j < N; j++)
        {  network[j].setID(j);}

        for(int j = 0; j < N; j++)
        {  Arrays.sort(network[j].getArray(), new Renumbering());}
        

        ClusteringThread.init(network);

        int proc = Runtime.getRuntime().availableProcessors();
		LinkedList<Thread> threads = new LinkedList<Thread>();
        int perThread = synchReader.getNodeCount() / proc;
        for(int p = 0; p < proc; p++) {

            int end = (p+1)*perThread;
            if(p == proc -1)
                end = network.length;
			ClusteringThread ct = new ClusteringThread(p, p * perThread,end );
            Thread thread = new Thread(ct);
            thread.start();
            threads.add(thread);
		}

        //Wait for all of the threads to be finished
        for(Thread t: threads){
            try{
                t.join();
            }catch(Exception e){e.printStackTrace();}
        }

        double clustering = 0;
        for(int v = 0; v < network.length; v++){
            if(network[v].length() > 1)
            {
                clustering += ClusteringThread.mTriangles[v] /
                    (network[v].length() * (network[v].length()-1) * .5f);
            }
        }

        NotifyDescriptor.Message msg = new NotifyDescriptor.Message("Clustering : " + clustering/nodes.length);
        DialogDisplayer.getDefault().notify(msg);
 *
 */
    }


    public void  bruteForce(GraphController graphController)
    {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeClass nodeClass = ac.getTemporaryAttributeManager().getNodeClass();
        AttributeColumn clusteringCol = nodeClass.addAttributeColumn("clustering", "Clustering Coefficient", AttributeType.FLOAT, AttributeOrigin.COMPUTED, 0);


        float totalCC = 0;
        Graph graph = null;
        if(undirectedOverride)
           graph = graphController.getUndirectedGraph();
        else
           graph = graphController.getDirectedGraph();


        for(Node node : graph.getNodes()) {
            float nodeCC = 0;
            int neighborhood = 0;
            NodeIterable neighbors1 = graph.getNeighbors(node);
            for(Node neighbor1 : neighbors1) {
                neighborhood++;
                NodeIterable neighbors2 = graph.getNeighbors(node);
                for(Node neighbor2 : neighbors2) {

                    if(neighbor1 == neighbor2) {
                        continue;
                    }

                    if(graph.isAdjacent(neighbor1, neighbor2)){
                        nodeCC++;
                    }
                }
            }

            nodeCC /= 2.0;
            
            if(neighborhood > 1)
            {
               float cc = nodeCC / (.5f * neighborhood * (neighborhood - 1));
               if(!undirectedOverride){
                   cc = nodeCC / (neighborhood * (neighborhood - 1));
               }

               AttributeRow row = (AttributeRow)node.getNodeData().getAttributes();
               row.setValue(clusteringCol, cc);

               totalCC += cc;
            }
        }
        avgClusteringCoeff = totalCC / graph.getNodeCount();
        
    }





    public boolean isParamerizable() {
        return false;
    }

    public JPanel getPanel() {
        return null;
    }

    public String getReport() {
       return new String("Average Clustering Coefficient: " + avgClusteringCoeff);
    }

    public void addActionListener(ActionListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
       
    
