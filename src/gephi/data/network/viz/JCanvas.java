package gephi.data.network.viz;

import gephi.data.network.TreeStructure;
import gephi.data.network.edge.DhnsEdge;
import gephi.data.network.edge.PreEdge;
import gephi.data.network.edge.VirtualEdge;
import gephi.data.network.edge.PreEdge.EdgeType;
import gephi.data.network.node.PreNode;

import gephi.data.network.sight.Sight;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

public class JCanvas extends Canvas 
{
	static final int HGAP = 10;
	static final int VGAP = 10;
	static final int CIRLE_WIDTH=5;
	
	TreeStructure treeStructure;
    Sight mainSight;
	  
	JCanvas(TreeStructure treeStructure, Sight sight)
	{ 
		super();
        this.mainSight = sight;
		this.treeStructure = treeStructure;
	}
	  
	public void paint(Graphics g) 
	{
		super.paint(g);
		initpaint(g);
	}

	public void update(Graphics g) 
	{
		super.update(g);
	}

	public void initpaint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		
		int t = treeStructure.getNodeAt(0).size*VGAP;
		int h = (int)(Math.sqrt(t*t+t*t)/2);
		g2d.translate(0, h);
		g2d.rotate(-Math.PI/4);
		
		for(PreNode p : treeStructure.getTree())
		{
			drawArc(g2d, p);
		}
		
		
		for(PreNode p : treeStructure.getTree())
		{
			int xPos = p.pre*HGAP;
			int yPos = p.post*VGAP;
			
			BasicStroke stroke = new BasicStroke(1);
			g2d.setStroke(stroke);
			
			drawShape(g2d, p, yPos, xPos);
			
			for(PreEdge edge : p.getForwardEdges())
			{
				PreNode node2 = edge.maxNode;
				int xPos2 = node2.pre*HGAP;
				int yPos2 = node2.post*VGAP;
				
				if(edge.edgeType==EdgeType.IN)
					g2d.setColor(Color.GREEN);
				else
					g2d.setColor(Color.PINK);
				
				QuadCurve2D q = new QuadCurve2D.Float();
				// draw QuadCurve2D.Float with set coordinates
				q.setCurve(yPos2, xPos2, yPos, xPos2, yPos, xPos);
				g2d.draw(q);
				//g2d.drawLine(yPos, xPos, yPos2, xPos2);
			}
			
			g2d.setColor(Color.GRAY);
			
			for(DhnsEdge e : p.getVirtualEdgesOUT(mainSight))
			{
				VirtualEdge edge = (VirtualEdge)e;
				PreNode dest = edge.getPreNodeTo();
				int xPos2 = dest.pre*HGAP;
				int yPos2 = dest.post*VGAP;

				Line2D.Float line = new Line2D.Float(yPos, xPos, yPos2, xPos2);
				stroke = new BasicStroke(edge.getCardinal());
				g2d.setStroke(stroke);
				g2d.draw(line);
			}
		}
		
	}
	
	private void drawShape(Graphics2D g, PreNode p, int x, int y)
	{		
		if(p.isEnabled(mainSight))
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.WHITE);
		
		g.fillOval(x-CIRLE_WIDTH/2, y-CIRLE_WIDTH/2, CIRLE_WIDTH, CIRLE_WIDTH);
		
		g.setColor(Color.BLACK);
		g.drawOval(x-CIRLE_WIDTH/2, y-CIRLE_WIDTH/2, CIRLE_WIDTH, CIRLE_WIDTH);
	}
	
	private void drawArc(Graphics2D g,PreNode p)
	{
		g.setColor(Color.BLUE);
		
		int xPos = p.pre*HGAP;
		int yPos = p.post*VGAP;
		
		PreNode parent=treeStructure.getRoot();
		if(p.parent!=null)
		{
			parent = p.parent;
		}
		int xPos2 = parent.pre*HGAP;
		int yPos2 = parent.post*VGAP;
		
		g.drawLine(yPos, xPos, yPos2, xPos2);
	}
	
}
