/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.desktop.visualization.actions;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.gephi.project.api.ProjectController;
import org.gephi.visualization.VizController;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.util.actions.InputActionsProcessor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Menu action navigating the graph view, which does the same as the matching key in the graph view canvas.
 */
abstract class GraphViewAction extends AbstractAction {

    GraphViewAction(String bundleKey) {
        super(NbBundle.getMessage(GraphViewAction.class, bundleKey));
    }

    abstract void perform(VizController controller);

    static void zoomAroundCenter(VizController controller, double zoomQuantity) {
        controller.getEngine().filter(GraphViewAction::hasSize).ifPresent(engine -> new InputActionsProcessor(engine)
            .processZoomEvent(zoomQuantity, engine.getWidth() / 2, engine.getHeight() / 2));
    }

    /**
     * The engine has no size until its first reshape, and zooming around the centre of nothing gives NaN.
     */
    private static boolean hasSize(VizEngine<?, ?> engine) {
        return engine.getWidth() > 0 && engine.getHeight() > 0;
    }

    @Override
    public boolean isEnabled() {
        final VizController controller = Lookup.getDefault().lookup(VizController.class);
        return controller != null
            && controller.getEngine().filter(GraphViewAction::hasSize).isPresent()
            && controller.getCanvasManager().isCanvasShowing()
            && Lookup.getDefault().lookup(ProjectController.class).hasCurrentProject();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) {
            return;
        }
        final VizController controller = Lookup.getDefault().lookup(VizController.class);
        perform(controller);

        // Keep the keys of the graph view working if the menu left the focus nowhere
        SwingUtilities.invokeLater(() -> {
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner() == null) {
                controller.getCanvasManager().requestCanvasFocus();
            }
        });
    }
}
