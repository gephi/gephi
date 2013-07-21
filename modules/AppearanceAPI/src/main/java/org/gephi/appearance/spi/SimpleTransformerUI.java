/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.spi;

import javax.swing.JPanel;

/**
 *
 * @author mbastian
 */
public interface SimpleTransformerUI extends TransformerUI<SimpleTransformer> {

    public JPanel getPanel(SimpleTransformer transformer);
}
