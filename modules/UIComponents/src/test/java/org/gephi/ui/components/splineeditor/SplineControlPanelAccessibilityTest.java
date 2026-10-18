package org.gephi.ui.components.splineeditor;

import java.awt.Component;
import java.awt.Container;
import java.util.Locale;
import javax.swing.JList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.NbBundle;

public class SplineControlPanelAccessibilityTest {

    private Locale defaultLocale;

    @Before
    public void useEnglish() {
        // The expected names are the English ones, translators may add the keys later
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void restoreLocale() {
        Locale.setDefault(defaultLocale);
    }

    private static JList findList(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JList) {
                return (JList) c;
            }
            if (c instanceof Container) {
                JList found = findList((Container) c);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Test
    public void testTemplateListAndItemsAreNamed() {
        JList list = findList(new SplineControlPanel(null));

        Assert.assertNotNull(list);
        Assert.assertEquals(NbBundle.getMessage(SplineEditor.class, "splineEditor_templates"),
            list.getAccessibleContext().getAccessibleName());

        Component first = list.getCellRenderer()
            .getListCellRendererComponent(list, list.getModel().getElementAt(0), 0, false, false);
        Assert.assertEquals("Control points 0.00, 0.00 and 1.00, 1.00",
            first.getAccessibleContext().getAccessibleName());
        int lastIndex = list.getModel().getSize() - 1;
        Component last = list.getCellRenderer()
            .getListCellRendererComponent(list, list.getModel().getElementAt(lastIndex), lastIndex, false, false);
        Assert.assertEquals("Control points 1.00, 0.00 and 1.00, 0.00",
            last.getAccessibleContext().getAccessibleName());
    }
}
