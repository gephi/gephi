/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.partition.plugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.partition.api.Partition;
import org.gephi.partition.plugin.spi.NodeColorTransformerUI;
import org.gephi.partition.spi.Transformer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = NodeColorTransformerUI.class)
public class NodeColorTransformerUIImpl implements NodeColorTransformerUI {

    private NodeColorTransformerPanel panel;

    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/ui/partition/plugin/resources/color.png"));
    }

    public String getName() {
        return NbBundle.getMessage(NodeColorTransformerUIImpl.class, "NodeColorTransformerBuilder.ui.name");
    }

    public JPanel getPanel() {
        panel = new NodeColorTransformerPanel();
        return panel;
    }

    public void setup(Partition partition, Transformer transformer) {
        panel.setup(partition, transformer);
    }

    public void unsetup() {
        panel = null;
    }
}
