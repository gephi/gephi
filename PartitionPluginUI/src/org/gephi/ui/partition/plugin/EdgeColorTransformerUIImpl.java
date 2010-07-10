/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.partition.plugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.partition.api.Partition;
import org.gephi.partition.plugin.spi.EdgeColorTransformerUI;
import org.gephi.partition.spi.Transformer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = EdgeColorTransformerUI.class)
public class EdgeColorTransformerUIImpl implements EdgeColorTransformerUI {

    private EdgeColorTransformerPanel panel;

    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/ui/partition/plugin/resources/color.png"));
    }

    public String getName() {
        return NbBundle.getMessage(EdgeColorTransformerUIImpl.class, "EdgeColorTransformerBuilder.ui.name");
    }

    public JPanel getPanel() {
        panel = new EdgeColorTransformerPanel();
        return panel;
    }

    public void setup(Partition partition, Transformer transformer) {
        panel.setup(partition, transformer);
    }

    public void unsetup() {
        panel = null;
    }
}
