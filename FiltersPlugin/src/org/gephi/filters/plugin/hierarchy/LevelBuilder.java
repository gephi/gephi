/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin.hierarchy;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class LevelBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.HIERARCHY;
    }

    public String getName() {
        return NbBundle.getMessage(LevelBuilder.class, "LevelBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public LevelFilter getFilter() {
        return new LevelFilter();
    }

    public JPanel getPanel(Filter filter) {
        LevelUI ui = Lookup.getDefault().lookup(LevelUI.class);
        if (ui != null) {
            return ui.getPanel((LevelFilter) filter);
        }
        return null;
    }

    public static class LevelFilter implements NodeFilter {

        private Integer level = 0;
        private int height;

        public boolean init(Graph graph) {
            HierarchicalGraph hg = (HierarchicalGraph) graph;
            height = hg.getHeight();
            return true;
        }

        public boolean evaluate(Graph graph, Node node) {
            HierarchicalGraph hg = (HierarchicalGraph) graph;
            return hg.getLevel(node) == level.intValue();
        }

        public void finish() {
        }

        public int getHeight() {
            return height;
        }

        public String getName() {
            return NbBundle.getMessage(LevelBuilder.class, "LevelBuilder.name");
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                            FilterProperty.createProperty(this, Integer.class, "level")
                        };
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return new FilterProperty[0];
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }
    }
}
