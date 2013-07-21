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
public interface RankingTransformerUI extends TransformerUI<RankingTransformer> {

    public JPanel getPanel(RankingTransformer transformer, Number min, Number max);
}
