/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.spi;

import javax.swing.Icon;
import org.openide.util.NbBundle;

/**
 *
 * @author mbastian
 */
public interface Category {

    public String getName();

    public Icon getIcon();

    public boolean isNode();

    public boolean isEdge();
    public static Category NODE_SIZE = new Category() {
        @Override
        public String getName() {
            return NbBundle.getMessage(Category.class, "Category.NodeSize.name");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public boolean isNode() {
            return true;
        }

        @Override
        public boolean isEdge() {
            return false;
        }
    };
    public static Category NODE_COLOR = new Category() {
        @Override
        public String getName() {
            return NbBundle.getMessage(Category.class, "Category.NodeColor.name");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public boolean isNode() {
            return true;
        }

        @Override
        public boolean isEdge() {
            return false;
        }
    };
    public static Category EDGE_COLOR = new Category() {
        @Override
        public String getName() {
            return NbBundle.getMessage(Category.class, "Category.EdgeColor.name");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public boolean isNode() {
            return false;
        }

        @Override
        public boolean isEdge() {
            return true;
        }
    };
}
