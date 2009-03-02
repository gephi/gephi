package gephi.data.network.node;

import gephi.data.network.avl.ForwardEdgeTree;
import gephi.data.network.avl.BackwardEdgeTree;
import gephi.data.network.avl.DytsEdgeTree;
import gephi.data.network.avl.simple.AVLItem;
import gephi.data.network.avl.simple.SimpleAVLTree;
import gephi.data.network.avl.typed.IntegerAVLTree;
import gephi.data.network.edge.DytsEdge;
import gephi.data.network.edge.EdgeProcessing;
import gephi.data.network.edge.PreEdge;
import gephi.data.network.edge.VirtualEdge;
import gephi.data.network.edge.PreEdge.EdgeType;
import gephi.data.network.node.treelist.PreNodeTreeList;
import gephi.data.network.node.treelist.PreNodeTreeList.AVLNode;

/**
 * Node of the tree. Maintained in a global order tree, the node is build on a <b>pre/post/size/level</b> pane.
 * The <b>pre</b> is the global number in the tree, the <b>size</b> the number of node's child and <b>level</b>
 * the level within the hierarchy. The post is equal to <code>pre-level+size</code> and speed up algorithms 
 * when performing skipping.
 * <p> 
 * To support the concept of view on a hierarchical graph the class also contains <code>enabled</code> and
 * <code>space</code>.
 * <p>
 * If the node contains physical edges, they are stored in AVL trees in this class. For edges linked to a
 * node with a higher <code>pre</code> number, they are stored in a {@link ForwardEdgeTree}. For edges linked
 * to a lower <code>pre</code> number they are stored in {@link BackwardEdgeTree}.
 * <p>
 * Virtual edges are set to {@link DytsEdgeTree} as well and divided in <code>virtualEdgesIN</code> and
 * <code>virtualEdgesIN</code> trees.
 * 
 * @author Mathieu Bastian
 * @see PreNodeTreeList
 * @see EdgeProcessing
 */
public class PreNode implements AVLItem
{
	public int pre;
	public int size;
	public PreNode parent;
	public int level;
	public int post;
	public int space;
	public boolean enabled=false;
	
	public AVLNode avlNode;
	
	private ForwardEdgeTree forwardEdges;
	private BackwardEdgeTree backwardEdges;
	public int preTrace = -1;
	public int preTraceType=0;
	public VirtualEdge lastEdge;
	
	private DytsEdgeTree virtualEdgesIN;
	private DytsEdgeTree virtualEdgesOUT;
	
	//private Node node;
	
	private IntegerAVLTree viewTree;
	
	public PreNode(int pre, int size, int level, PreNode parent)
	{
		this.pre = pre;
		this.size = size;
		this.level = level;
		this.parent = parent;
		this.post = pre-level+size;
		
		forwardEdges = new ForwardEdgeTree();
		backwardEdges = new BackwardEdgeTree();
		
		virtualEdgesIN = new DytsEdgeTree(this);
		virtualEdgesOUT = new DytsEdgeTree(this);
		
		viewTree = new IntegerAVLTree();
		
		//node = new Node();
		//node.setPreNode(this);
	}
	
	public void getPost()
	{
		this.post = pre-level+size;
	}
	
	public String toString()
	{
		return ""+pre;
	}
	
	public void reinitTrace()
	{
		preTrace = -1;
		preTraceType = 0;
		lastEdge = null;
	}
	
	public int getPre()
	{
		return avlNode.getIndex();
	}
	
	
	public void setPre(int pre)
	{
		this.pre = pre;
	}
	
	public DytsEdge getVirtualEdge(PreEdge physicalEdge, int forwardPre)
	{
		if(physicalEdge.edgeType==EdgeType.IN)
			return virtualEdgesIN.getItem(forwardPre);
		else
			return virtualEdgesOUT.getItem(forwardPre);
	}	
	
	public void removeVirtualEdge(VirtualEdge edge)
	{
		if(edge.getPreNodeFrom()==this)
		{
			virtualEdgesOUT.remove(edge);
		}
		else
		{
			virtualEdgesIN.remove(edge);
		}
	}

	public void addForwardEdge(PreEdge edge)
	{
		forwardEdges.add(edge);
	}
	
	public void removeForwardEdge(PreEdge edge)
	{
		forwardEdges.remove(edge);
	}
	
	public void addBackwardEdge(PreEdge edge)
	{
		backwardEdges.add(edge);
	}
	
	public void removeBackwardEdge(PreEdge edge)
	{
		backwardEdges.remove(edge);
	}
	
	public boolean isLeaf()
	{
		return size==0;
	}
	
	public boolean hasView(int view)
	{
		return viewTree.contains(view);
	}
	
	public void addView(int view)
	{
		viewTree.add(view);
	}
	
	public void removeView(int view)
	{
		viewTree.remove(view);
	}
	
	public ForwardEdgeTree getForwardEdges()
	{
		return forwardEdges;
	}
	
	public int countForwardEdges()
	{
		return forwardEdges.getCount();
	}
	
	public int countBackwardEdges()
	{
		return backwardEdges.getCount();
	}

	/*public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}*/
	
	@Override
	public int getNumber() {
		return getPre();
	}

	public BackwardEdgeTree getBackwardEdges() {
		return backwardEdges;
	}

	public DytsEdgeTree getVirtualEdgesIN() {
		return virtualEdgesIN;
	}

	public DytsEdgeTree getVirtualEdgesOUT() {
		return virtualEdgesOUT;
	}
	
	
}
