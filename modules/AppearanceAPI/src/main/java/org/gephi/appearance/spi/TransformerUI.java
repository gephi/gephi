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
public interface TransformerUI<T extends Transformer> {

    public Category[] getCategories();

    public String getDisplayName();

    public String getDescription();

    public Icon getIcon();

    public Class<? extends T> getTransformerClass();
}
