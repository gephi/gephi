/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.layout.api;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 *
 * @author Mathieu Bastian
 */
public interface LayoutUI {

    /**
     * The description of the of the Layout's provided by this Builder.
     * @return
     */
    public String getDescription();

    /**
     * The icon that represents the Layout's provided by this Builder.
     * @return
     */
    public Icon getIcon();

    public JPanel getSimplePanel(Layout layout);

    public int getQualityRank();

    public int getSpeedRank();
}
