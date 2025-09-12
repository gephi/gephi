package org.gephi.layout.plugin.mirror;


import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = LayoutBuilder.class)
public class Mirror implements LayoutBuilder {

    private final MirrorLayoutUI ui = new MirrorLayoutUI();

    @Override
    public MirrorLayout buildLayout() {
        return new MirrorLayout(this, false, true);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(Mirror.class, "mirror.name");
    }

    @Override
    public LayoutUI getUI() {
        return ui;
    }

    private static class MirrorLayoutUI implements LayoutUI {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(Mirror.class, "mirror.description");
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
