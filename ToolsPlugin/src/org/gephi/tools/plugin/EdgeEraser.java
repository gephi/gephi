/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.tools.plugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.graph.api.*;
import org.gephi.tools.spi.*;
import org.gephi.ui.tools.plugin.EdgeEraserPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author purplebleed
 */
@ServiceProvider(service = Tool.class)
public class EdgeEraser implements Tool{
    //Architecture
    private ToolEventListener[] listeners;
    private EdgeEraserPanel edgeeraserpanel;
    //Settings
    private Node sourceNode;

    public EdgeEraser() {
        //Default settings
    }

    public void select() {
    }

    public void unselect() {
        listeners = null;
        edgeeraserpanel = null;
    }

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[2];
        listeners[0] = new NodeClickEventListener() {

            public void clickNodes(Node[] nodes) {
                Node n = nodes[0];
                if (sourceNode == null) {
                    sourceNode = n;
                    edgeeraserpanel.setStatus(NbBundle.getMessage(EdgeEraser.class, "EdgePencil.status2"));
                } else {
                    GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                    Graph graph = gc.getModel().getGraph();
                    Edge e = graph.getEdge(sourceNode, n);
                    if(e != null)
                        graph.removeEdge(e);

                    sourceNode = null;
                     edgeeraserpanel.setStatus(NbBundle.getMessage(EdgeEraser.class, "EdgePencil.status1"));
                }
            }
        };
        listeners[1] = new MouseClickEventListener() {

            public void mouseClick(int[] positionViewport, float[] position3d) {
                if (sourceNode != null) {
                    //Cancel
                    edgeeraserpanel.setStatus(NbBundle.getMessage(EdgeEraser.class, "EdgePencil.status1"));
                    sourceNode = null;
                }
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                edgeeraserpanel = new EdgeEraserPanel();
                edgeeraserpanel.setStatus(NbBundle.getMessage(EdgeEraser.class, "EdgePencil.status1"));
                return edgeeraserpanel;
            }

            public String getName() {
                return NbBundle.getMessage(NodePencil.class, "EdgeEraser.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/plugin/resources/eraser.png"));
            }

            public String getDescription() {
                return NbBundle.getMessage(NodePencil.class, "EdgeEraser.description");
            }

            public int getPosition() {
                return 250;
            }
        };
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}
