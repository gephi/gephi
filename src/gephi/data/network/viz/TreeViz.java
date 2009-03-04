package gephi.data.network.viz;

import gephi.data.network.TreeStructure;

import gephi.data.network.sight.Sight;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.ScrollPane;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;


public class TreeViz extends JFrame {

	private JDesktopPane desktopPane;
	private JInternalFrame treeFrame;
	private JCanvas canvas;
	private JPanel panel;
	private TreeStructure treeStructure;
	private ControlPanel controlPanel;
    private Sight sight;
	
	public TreeViz(Sight sight)
	{
		super("DYTS");
        this.sight = sight;
		desktopPane = new JDesktopPane();
		desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		setContentPane(desktopPane);
		setVisible(false);
		int state = getExtendedState();
	    state |= JFrame.MAXIMIZED_BOTH;
	    setExtendedState(state);
	    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    
	    controlPanel = new ControlPanel();
	    showControlPanel(controlPanel);
	}
	
	public void setPanelToFrame(JPanel panel, JInternalFrame frame)
	{
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		try {
			frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {}

	}
	
	public void showTree(TreeStructure tree)
	{
		boolean vis = isVisible();
		setVisible(true);
		if(!vis)
			showControlPanel(controlPanel);
		
		if(treeFrame!=null)
		{
			if(this.treeStructure!=tree)
			{
				this.treeStructure =tree;
				canvas.treeStructure = tree;
			}
			
			treeFrame.setVisible(false);
			treeFrame.setVisible(true);
		}
		else
		{
			this.treeStructure = tree;
			
			panel = new JPanel(new BorderLayout());
			panel.setBackground(Color.WHITE);
			panel.setPreferredSize(new Dimension(800,600));
			ScrollPane s = new ScrollPane();
			s.setSize(100,100);
			panel.add(s, BorderLayout.CENTER);
			canvas = new JCanvas(tree, sight);
			canvas.setSize(2000,3000);
			s.add(canvas);
			
			treeFrame = new JInternalFrame("TreeViz", true, true, true, true);
			treeFrame.setLocation(0, 0);
			setPanelToFrame(panel, treeFrame);
			desktopPane.add(treeFrame);
			desktopPane.setVisible(true);
		}
	}
	
	public void showControlPanel(ControlPanel panel)
	{
		JInternalFrame frame = new JInternalFrame("ControlPanel", true, true, true, true);
		frame.setLocation(0, getHeight()-panel.getPreferredSize().height-100);
		setPanelToFrame(panel, frame);
		desktopPane.add(frame);
		desktopPane.setVisible(true);
	}

	public ControlPanel getControlPanel() {
		return controlPanel;
	}
}
