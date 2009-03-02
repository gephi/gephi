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
package gephi.data.network.viz;

import gephi.data.network.edge.PreEdge;
import gephi.data.network.edge.PreEdge.EdgeType;
import gephi.data.network.node.PreNode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ControlPanel extends JPanel {

	public enum ActionType { EXPAND, RETRACT, ADDNODE, DELNODE, DELNODES, GETEDGES, DELEDGE };
	
		
	public ControlPanel()
	{
		initComponents();
		setPreferredSize(new Dimension(450,200));
	}
	
	private JTextField expandContractNumberTextField;
	private JTextField addDeleteNodesNumberTextField;
	private JTextField delMultipleNodesNumberTextField;
	private JButton expandButton;
	private JButton retractButton;
	private JButton addNodeButton;
	private JButton delNodeButton;
	private JButton delMultipleNodesButton;
	
	//DelEdges
	private PreEdge[] edgesTab;
	private JList edgesList;
	private PreNode selectedNode;
	private JButton getEdgeNodeButton;
	private JButton delEdgeButton;
	private JTextField delEdgeNumberTextField;
	
	private void initComponents()
	{
		setLayout(new GridLayout(2,2));
		
		//Expand & Contract
		JPanel expandContractPanel = new JPanel(new BorderLayout());
		expandContractPanel.setBorder(BorderFactory.createTitledBorder("Expand & Contract"));
		expandContractNumberTextField = new JTextField();
		expandContractPanel.add(expandContractNumberTextField, BorderLayout.NORTH);
		expandButton = new JButton();
		retractButton = new JButton();
		expandContractPanel.add(expandButton, BorderLayout.CENTER);
		expandContractPanel.add(retractButton, BorderLayout.SOUTH);
		
		//Add Nodes
		JPanel addDeleteNodesPanel = new JPanel(new BorderLayout());
		addDeleteNodesPanel.setBorder(BorderFactory.createTitledBorder("Add & Delete Nodes"));
		addDeleteNodesNumberTextField = new JTextField();
		addDeleteNodesPanel.add(addDeleteNodesNumberTextField, BorderLayout.NORTH);
		addNodeButton = new JButton();
		delNodeButton = new JButton();
		addDeleteNodesPanel.add(addNodeButton, BorderLayout.CENTER);
		addDeleteNodesPanel.add(delNodeButton, BorderLayout.SOUTH);
		
		//Del Several Nodes
		JPanel delMultipleNodesPanel = new JPanel(new BorderLayout());
		delMultipleNodesPanel.setBorder(BorderFactory.createTitledBorder("Delete multiple Nodes"));
		delMultipleNodesNumberTextField = new JTextField();
		delMultipleNodesPanel.add(delMultipleNodesNumberTextField, BorderLayout.NORTH);
		delMultipleNodesButton = new JButton();
		delMultipleNodesPanel.add(delMultipleNodesButton, BorderLayout.CENTER);
		
		//Del Edges
		JPanel deleteEdgesPanel = new JPanel(new BorderLayout());
		deleteEdgesPanel.setBorder(BorderFactory.createTitledBorder("Delete edges"));
		JPanel deleteEdgesControlePanel = new JPanel(new BorderLayout());
		getEdgeNodeButton = new JButton();
		delEdgeButton = new JButton();
		delEdgeNumberTextField = new JTextField();
		deleteEdgesControlePanel.add(delEdgeNumberTextField, BorderLayout.NORTH);
		deleteEdgesControlePanel.add(getEdgeNodeButton, BorderLayout.CENTER);
		deleteEdgesControlePanel.add(delEdgeButton, BorderLayout.SOUTH);
		deleteEdgesPanel.add(deleteEdgesControlePanel, BorderLayout.EAST);
		edgesList = new JList();
		deleteEdgesPanel.add(edgesList, BorderLayout.CENTER);
		
		add(expandContractPanel);
		add(addDeleteNodesPanel);
		add(delMultipleNodesPanel);
		add(deleteEdgesPanel);
	}
	
	public void setEdgesTab(PreEdge[] edgesTab)
	{
		this.edgesTab = edgesTab;
		DefaultListModel model = new DefaultListModel();
		for(PreEdge e : edgesTab)
		{
			String str="";
			if(e.edgeType==EdgeType.IN)
				str="<- "+e.maxNode;
			else
				str="-> "+e.maxNode;
			
		
			model.addElement(str);
		}
		edgesList.setModel(model);
	}
	
	public PreEdge getSelectedEdge()
	{
		return edgesTab[edgesList.getSelectedIndex()];
	}
	
	public void setSelectedNode(PreNode node)
	{
		selectedNode = node;
	}
	
	public int getDelEdgeNumber()
	{
		return Integer.parseInt(delEdgeNumberTextField.getText());
	}
	
	public int getExpandContractNumber()
	{
		return Integer.parseInt(expandContractNumberTextField.getText());
	}
	
	public int getAddDeleteNodeNumber()
	{
		return Integer.parseInt(addDeleteNodesNumberTextField.getText());
	}
	
	public int[] getMultipleDeleteNodeNumber()
	{
		String str = delMultipleNodesNumberTextField.getText();
		String[] strTab = str.split(" ");
		int[] res = new int[strTab.length];
		for(int i=0;i<strTab.length;i++)
		{
			res[i] = Integer.parseInt(strTab[i]);
		}
		return res;
	}
	
	public void setAction(ActionType actionType, Action action)
	{
		getActionMap().put(actionType, action);
		expandButton.setAction(getActionMap().get(ActionType.EXPAND));
		retractButton.setAction(getActionMap().get(ActionType.RETRACT));
		addNodeButton.setAction(getActionMap().get(ActionType.ADDNODE));
		delNodeButton.setAction(getActionMap().get(ActionType.DELNODE));
		delMultipleNodesButton.setAction(getActionMap().get(ActionType.DELNODES));
		getEdgeNodeButton.setAction(getActionMap().get(ActionType.GETEDGES));
		delEdgeButton.setAction(getActionMap().get(ActionType.DELEDGE));
	}
	
	public void getAction(ActionType actionType)
	{
		getActionMap().get(actionType);
	}
}
