package org.gephi.ui.components.splineeditor;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JList;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.NbBundle;

public class SplineControlPanelAccessibilityTest {

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
        Assert.assertEquals(NbBundle.getMessage(SplineEditor.class, "splineEditor_templates"), list.getAccessibleContext().getAccessibleName());

        Component first = list.getCellRenderer().getListCellRendererComponent(list, list.getModel().getElementAt(0), 0, false, false);
        Assert.assertEquals("Control points 0.00, 0.00 and 1.00, 1.00",
            first.getAccessibleContext().getAccessibleName());
        Component last = list.getCellRenderer().getListCellRendererComponent(list,
            list.getModel().getElementAt(list.getModel().getSize() - 1), list.getModel().getSize() - 1, false, false);
        Assert.assertEquals("Control points 1.00, 0.00 and 1.00, 0.00",
            last.getAccessibleContext().getAccessibleName());
    }
}
