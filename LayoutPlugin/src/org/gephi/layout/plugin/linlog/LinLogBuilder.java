/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.layout.plugin.linlog;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
//@ServiceProvider(service = LayoutBuilder.class)
public class LinLogBuilder implements LayoutBuilder {

    private LinLogLayoutUI ui = new LinLogLayoutUI();

    public String getName() {
        return NbBundle.getMessage(LinLogBuilder.class, "name");
    }

    public LinLog buildLayout() {
        return new LinLog(this);
    }

    public LayoutUI getUI() {
        return ui;
    }

    private static class LinLogLayoutUI implements LayoutUI {

        public String getDescription() {
            return NbBundle.getMessage(LinLogBuilder.class, "description");
        }

        public Icon getIcon() {
            return null;
        }

        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        public int getQualityRank() {
            return 5;
        }

        public int getSpeedRank() {
            return 3;
        }
    }
}
