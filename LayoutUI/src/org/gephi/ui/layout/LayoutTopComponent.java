/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.layout;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutControllerObserver;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.view.ChoiceView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
final class LayoutTopComponent extends TopComponent
    implements LayoutControllerObserver, ExplorerManager.Provider {

    private static LayoutTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "LayoutTopComponent";
    private LayoutController layoutController;
    private Action requestPlayAction;
    private Action requestStopAction;
    private Action chooseLayoutAction;
    private final ExplorerManager mgr = new ExplorerManager();
    private String valor;
    private Node bean;
    PropertySheet ps2;

    public void setValor(String valor) {
        System.out.println("!!!!!! setValor");
        this.valor = valor;
    }

    public String getValor() {
        System.out.println("!!!!!! getValor");
        return valor;
    }

    private LayoutTopComponent() {
        initActions();
        initLayoutController();
        initComponents();
        mgr.setRootContext(new AbstractNode(new RootNode()));

        setName(NbBundle.getMessage(LayoutTopComponent.class, "CTL_LayoutTopComponent"));
        setToolTipText(NbBundle.getMessage(LayoutTopComponent.class, "HINT_LayoutTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        putClientProperty("netbeans.winsys.tc.keep_preferred_size_when_slided_in", Boolean.TRUE);
    }

    private void initActions() {
        requestPlayAction = new RequestPlayAction();
        requestStopAction = new RequestStopAction();
        chooseLayoutAction = new ChooseLayoutAction();
    }

    private void initLayoutController() {
        layoutController = Lookup.getDefault().lookup(LayoutController.class);
        layoutController.addObserver(this);
    }

    public class MyNode extends AbstractNode {

        private String valor;

        public void setValor(String valor) {
            System.out.println("!!!!!! setValor");
            this.valor = valor;
        }

        public String getValor() {
            System.out.println("!!!!!! getValor");
            return valor;
        }

        public MyNode(LayoutBuilder obj) {
            super(new RootNode(), Lookups.singleton(obj));
            setDisplayName("LayoutBuilder " + obj.getName());
        }

        public MyNode() {
            super(new RootNode());
            setDisplayName("Root");
        }

        @Override
        public String getName() {
            System.out.println("!!!!!! getName");
            return "NOME";
        }

        @Override
        public String getShortDescription() {
            System.out.println("!!!!!! getShortDescription");
            return "SHORT DESC";
        }

        @Override
        public PropertySet[] getPropertySets() {
            System.out.println("!!!!!! getPropertySets");
            Sheet.Set set = Sheet.createPropertiesSet();
            LayoutBuilder obj = getLookup().lookup(LayoutBuilder.class);

            Property indexProp;
            try {
                indexProp = new PropertySupport.Reflection(this, String.class, "getValor", "setValor");
                indexProp.setName("valor");
                set.put(indexProp);
            } catch (NoSuchMethodException ex) {
                System.out.println("!!!!!! getPropertySets EXCEPTION");
                ex.printStackTrace();
            }
            return new PropertySet[]{set};
        }
    }

    class RootNode extends Children.Keys {

        public RootNode() {
        }

        @Override
        protected Node[] createNodes(Object o) {
            LayoutBuilder layoutBuilder = (LayoutBuilder) o;
            Node node = null;
            node = new MyNode(layoutBuilder);
            return new Node[]{node};
        }

        @Override
        protected void addNotify() {
            setKeys(layoutController.getLayouts().toArray());
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
    }

    private void initLayoutComboBox() {
        layoutComboBox.setAction(chooseLayoutAction);
        List<LayoutBuilder> layouts = layoutController.getLayouts();
        System.out.println("layouts: " + layouts.size());
        for (LayoutBuilder layoutBuilder : layouts) {
            layoutComboBox.addItem(new LayoutBuilderWrapper(layoutBuilder));
            System.out.println(layoutBuilder.getClass().getName());
            System.out.println(layoutBuilder.getName());
            System.out.println(layoutBuilder.getDescription() + "\n");
        }
        chooseLayoutAction.actionPerformed(null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topLabel = new javax.swing.JLabel();
        layoutSourcePanel = new javax.swing.JPanel();
        layoutComboBox = new javax.swing.JComboBox();
        addLayoutButton = new javax.swing.JButton();
        layoutsPanel = new javax.swing.JPanel();
        jComboBox1 = new ChoiceView();
        propertySheetPanel = new javax.swing.JPanel();
        playButton = new javax.swing.JButton(requestPlayAction);
        stopButton = new javax.swing.JButton(requestStopAction);

        org.openide.awt.Mnemonics.setLocalizedText(topLabel, org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.topLabel.text")); // NOI18N

        layoutSourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.layoutSourcePanel.border.title"))); // NOI18N

        layoutComboBox.setFont(new java.awt.Font("Tahoma", 1, 14));
        initLayoutComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(addLayoutButton, org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.addLayoutButton.text")); // NOI18N
        addLayoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLayoutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layoutSourcePanelLayout = new javax.swing.GroupLayout(layoutSourcePanel);
        layoutSourcePanel.setLayout(layoutSourcePanelLayout);
        layoutSourcePanelLayout.setHorizontalGroup(
            layoutSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layoutSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(layoutComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addLayoutButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layoutSourcePanelLayout.setVerticalGroup(
            layoutSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layoutSourcePanelLayout.createSequentialGroup()
                .addGroup(layoutSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addLayoutButton, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                    .addComponent(layoutComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layoutsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.layoutsPanel.border.title"))); // NOI18N
        layoutsPanel.setFocusCycleRoot(true);

        propertySheetPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout propertySheetPanelLayout = new javax.swing.GroupLayout(propertySheetPanel);
        propertySheetPanel.setLayout(propertySheetPanelLayout);
        propertySheetPanelLayout.setHorizontalGroup(
            propertySheetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 230, Short.MAX_VALUE)
        );
        propertySheetPanelLayout.setVerticalGroup(
            propertySheetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 264, Short.MAX_VALUE)
        );

        propertySheetPanel.setLayout(new FlowLayout());
        ps2 = new PropertySheet();
        propertySheetPanel.add(ps2);

        org.openide.awt.Mnemonics.setLocalizedText(playButton, org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.playButton.text")); // NOI18N
        playButton.setToolTipText(org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.playButton.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(stopButton, org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.stopButton.text")); // NOI18N
        stopButton.setToolTipText(org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.stopButton.toolTipText")); // NOI18N
        stopButton.setActionCommand(org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.stopButton.actionCommand")); // NOI18N
        stopButton.setEnabled(false);

        javax.swing.GroupLayout layoutsPanelLayout = new javax.swing.GroupLayout(layoutsPanel);
        layoutsPanel.setLayout(layoutsPanelLayout);
        layoutsPanelLayout.setHorizontalGroup(
            layoutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layoutsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layoutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layoutsPanelLayout.createSequentialGroup()
                        .addComponent(playButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopButton))
                    .addComponent(jComboBox1, 0, 234, Short.MAX_VALUE)
                    .addComponent(propertySheetPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layoutsPanelLayout.setVerticalGroup(
            layoutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layoutsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(propertySheetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layoutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(playButton)
                    .addComponent(stopButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(layoutsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(topLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(layoutSourcePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(layoutSourcePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(layoutsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addLayoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLayoutButtonActionPerformed
        // TODO add your handling code here:
//        PropertySheetView ps = (PropertySheetView) jPanel1;
//        ps.setNodes(mgr.getSelectedNodes());
//        ps.setVisible(true);
//        ps.addNotify();
        ps2.setNodes(mgr.getSelectedNodes());
        Node node = mgr.getSelectedNodes()[0];
        PropertySet prop = node.getPropertySets()[0];
        System.out.println("Properties: ");
        for (Property p : prop.getProperties()) {
            System.out.println(p.getDisplayName());
        }

    //((PropertySheetView)jPanel1).updateUI();

}//GEN-LAST:event_addLayoutButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addLayoutButton;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox layoutComboBox;
    private javax.swing.JPanel layoutSourcePanel;
    private javax.swing.JPanel layoutsPanel;
    private javax.swing.JButton playButton;
    private javax.swing.JPanel propertySheetPanel;
    private javax.swing.JButton stopButton;
    private javax.swing.JLabel topLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized LayoutTopComponent getDefault() {
        if (instance == null) {
            instance = new LayoutTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the LayoutTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized LayoutTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(LayoutTopComponent.class.getName()).warning(
                "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof LayoutTopComponent) {
            return (LayoutTopComponent) win;
        }
        Logger.getLogger(LayoutTopComponent.class.getName()).warning(
            "There seem to be multiple components with the '" + PREFERRED_ID +
            "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
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

    public void executeEvent() {
        stopButton.setEnabled(true);
        playButton.setEnabled(false);
    }

    public void stopEvent() {
        stopButton.setEnabled(false);
        playButton.setEnabled(true);
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return LayoutTopComponent.getDefault();
        }
    }

    class RequestPlayAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            layoutController.executeLayout();
        }
    }

    class RequestStopAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            layoutController.stopLayout();
        }
    }

    class ChooseLayoutAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            LayoutBuilderWrapper selected = (LayoutBuilderWrapper) layoutComboBox.getSelectedItem();
            Layout layout = selected.getLayoutBuilder().buildLayout();
            System.out.println(layout.getClass().getName());
            layoutController.setLayout(layout);
        }
    }
}

class LayoutBuilderWrapper {

    private LayoutBuilder layoutBuilder;

    public LayoutBuilderWrapper(LayoutBuilder layoutBuilder) {
        this.layoutBuilder = layoutBuilder;
    }

    public LayoutBuilder getLayoutBuilder() {
        return layoutBuilder;
    }

    @Override
    public String toString() {
        return layoutBuilder.getName();
    }
}
