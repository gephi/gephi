package org.gephi.desktop.visualization.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.junit.Assert;
import org.junit.Test;

public class GraphViewActionTest {

    @Test
    public void testDisabledWithoutEngine() {
        for (Action action : new Action[] {new ZoomIn(), new ZoomOut(), new CenterOnGraph()}) {
            Assert.assertFalse(action.isEnabled());
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        }
    }
}
