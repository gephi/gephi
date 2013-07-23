/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.appearance.plugin.category;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.gephi.appearance.spi.Category;
import org.openide.util.NbBundle;

/**
 *
 * @author mbastian
 */
public class DefaultCategory {

    public static Category NODE_SIZE = new Category() {
        @Override
        public String getName() {
            return NbBundle.getMessage(DefaultCategory.class, "Category.NodeSize.name");
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(getClass().getResource("/org/gephi/ui/appearance/plugin/resources/size.png"));
        }

        @Override
        public boolean isNode() {
            return true;
        }

        @Override
        public boolean isEdge() {
            return false;
        }

        @Override
        public String toString() {
            return "NODE_SIZE";
        }
    };
    public static Category NODE_COLOR = new Category() {
        @Override
        public String getName() {
            return NbBundle.getMessage(DefaultCategory.class, "Category.NodeColor.name");
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(getClass().getResource("/org/gephi/ui/appearance/plugin/resources/color.png"));
        }

        @Override
        public boolean isNode() {
            return true;
        }

        @Override
        public boolean isEdge() {
            return false;
        }

        @Override
        public String toString() {
            return "NODE_COLOR";
        }
    };
    public static Category EDGE_COLOR = new Category() {
        @Override
        public String getName() {
            return NbBundle.getMessage(DefaultCategory.class, "Category.EdgeColor.name");
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(getClass().getResource("/org/gephi/ui/appearance/plugin/resources/color.png"));
        }

        @Override
        public boolean isNode() {
            return false;
        }

        @Override
        public boolean isEdge() {
            return true;
        }

        @Override
        public String toString() {
            return "EDGE_COLOR";
        }
    };
}
