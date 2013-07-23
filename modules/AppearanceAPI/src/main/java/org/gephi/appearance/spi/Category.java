/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.spi;

import javax.swing.Icon;

/**
 *
 * @author mbastian
 */
public interface Category {

    public String getName();

    public Icon getIcon();

    public boolean isNode();

    public boolean isEdge();
}
