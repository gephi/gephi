package org.gephi.layout.plugin.scale;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = LayoutBuilder.class)
public class Scale implements LayoutBuilder {
    private final ScaleLayoutUI ui = new ScaleLayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(Scale.class, "scale.name");
    }

    @Override
    public LayoutUI getUI() {
        return ui;
    }

    @Override
    public ScaleLayout buildLayout() {
        return new ScaleLayout(this, 1.2);
    }

    private static class ScaleLayoutUI implements LayoutUI {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(Scale.class, "scale.description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return -1;
        }

        @Override
        public int getSpeedRank() {
            return -1;
        }
    }
}
