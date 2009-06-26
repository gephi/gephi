/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.laboratory;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.ClusteredDirectedGraph;
import org.gephi.graph.api.GraphController;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
final class DataExplorerTopComponent extends TopComponent implements LookupListener {

    private enum ClassDisplayed {

        NONE, NODE, EDGE
    };
    private static DataExplorerTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "DataExplorerTopComponent";

    //Lookup
    final Lookup.Result<AttributeColumn> nodeColumnsResult;
    final Lookup.Result<AttributeColumn> edgeColumnsResult;

    //States
    ClassDisplayed classDisplayed = ClassDisplayed.NONE;
    //Executor
    ExecutorService taskExecutor;

    private DataExplorerTopComponent() {

        taskExecutor = new ThreadPoolExecutor(0, 1, 10L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(20));


        initComponents();
        setName(NbBundle.getMessage(DataExplorerTopComponent.class, "CTL_DataExplorerTopComponent"));
        setToolTipText(NbBundle.getMessage(DataExplorerTopComponent.class, "HINT_DataExplorerTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));

        //Init lookups
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        nodeColumnsResult = attributeController.getNodeColumnsLookup().lookupResult(AttributeColumn.class);
        edgeColumnsResult = attributeController.getEdgeColumnsLookup().lookupResult(AttributeColumn.class);
        nodeColumnsResult.addLookupListener(this);
        edgeColumnsResult.addLookupListener(this);
        initNodesView();
    }

    private void initNodesView() {
        Runnable initNodesRunnable = new Runnable() {

            public void run() {
                try {
                    //Attributes columns
                    Collection<? extends AttributeColumn> attributeColumns = nodeColumnsResult.allInstances();
                    final AttributeColumn[] cols = attributeColumns.toArray(new AttributeColumn[0]);

                    //Nodes from DHNS

                    ClusteredDirectedGraph graph = Lookup.getDefault().lookup(GraphController.class).getClusteredDirectedGraph();
                    graph.readLock();
                    org.gephi.graph.api.Node[] nodes = graph.getTopNodes().toArray();

                    //TreeModel
                    final TreeModel treeMdl = new NodeTreeModel(nodes, graph);
                    graph.readUnlock();

                    //Outline
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, new NodeRowModel(cols), true);
                            outline1.setRootVisible(false);
                            outline1.setRenderDataProvider(new NodeRenderer());
                            outline1.setModel(mdl);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Future future = taskExecutor.submit(initNodesRunnable);
    }

    private void initEdgesView() {
        Runnable initEdgesRunnable = new Runnable() {

            public void run() {
                try {
                    //Attributes columns
                    Collection<? extends AttributeColumn> attributeColumns = edgeColumnsResult.allInstances();
                    final AttributeColumn[] cols = attributeColumns.toArray(new AttributeColumn[0]);

                    //Edges from DHNS
                     ClusteredDirectedGraph graph = Lookup.getDefault().lookup(GraphController.class).getClusteredDirectedGraph();
                    graph.readLock();
                    org.gephi.graph.api.Edge[] edges = graph.getEdges().toArray();

                    //TreeModel
                    final TreeModel treeMdl = new EdgeTreeModel(edges);
                    graph.readUnlock();

                    //Outline
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, new EdgeRowModel(cols), true);
                            outline1.setRootVisible(false);
                            outline1.setRenderDataProvider(new EdgeRenderer());
                            outline1.setModel(mdl);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Future future = taskExecutor.submit(initEdgesRunnable);
    }

    public void resultChanged(LookupEvent ev) {
        if (classDisplayed.equals(ClassDisplayed.NODE)) {
            initNodesView();
        } else if(classDisplayed.equals(ClassDisplayed.EDGE)){
            initEdgesView();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlPanel = new javax.swing.JPanel();
        nodesButton = new javax.swing.JButton();
        edgesButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        outline1 = new org.netbeans.swing.outline.Outline();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(nodesButton, org.openide.util.NbBundle.getMessage(DataExplorerTopComponent.class, "DataExplorerTopComponent.nodesButton.text")); // NOI18N
        nodesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(edgesButton, org.openide.util.NbBundle.getMessage(DataExplorerTopComponent.class, "DataExplorerTopComponent.edgesButton.text")); // NOI18N
        edgesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edgesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nodesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgesButton)
                .addContainerGap(260, Short.MAX_VALUE))
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nodesButton)
                    .addComponent(edgesButton)))
        );

        add(controlPanel, java.awt.BorderLayout.PAGE_START);

        jScrollPane1.setViewportView(outline1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void nodesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodesButtonActionPerformed
        classDisplayed = ClassDisplayed.NODE;
        initNodesView();
    }//GEN-LAST:event_nodesButtonActionPerformed

    private void edgesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edgesButtonActionPerformed
        classDisplayed = ClassDisplayed.EDGE;
        initEdgesView();
    }//GEN-LAST:event_edgesButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton edgesButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton nodesButton;
    private org.netbeans.swing.outline.Outline outline1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized DataExplorerTopComponent getDefault() {
        if (instance == null) {
            instance = new DataExplorerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DataExplorerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DataExplorerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DataExplorerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DataExplorerTopComponent) {
            return (DataExplorerTopComponent) win;
        }
        Logger.getLogger(DataExplorerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return DataExplorerTopComponent.getDefault();
        }
    }
}
