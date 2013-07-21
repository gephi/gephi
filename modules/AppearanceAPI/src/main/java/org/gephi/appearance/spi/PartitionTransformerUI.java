/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.spi;

import javax.swing.JPanel;
import org.gephi.appearance.api.Partition;

/**
 *
 * @author mbastian
 */
public interface PartitionTransformerUI extends TransformerUI<PartitionTransformer> {

    public JPanel getPanel(PartitionTransformer transformer, Partition partition);
}
