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

package org.gephi.desktop.banner;

import java.awt.event.ActionListener;
import org.gephi.desktop.banner.perspective.plugin.LaboratoryPerspective;
import org.gephi.desktop.banner.perspective.plugin.OverviewPerspective;
import org.gephi.desktop.banner.perspective.plugin.PreviewPerspective;
import org.gephi.perspective.api.PerspectiveController;
import org.gephi.perspective.spi.Perspective;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;

/**
 * Keyboard-accessible equivalents of the perspective buttons in the banner.
 */
public final class PerspectiveActions {

    private PerspectiveActions() {
    }

    @ActionID(category = "Window", id = "org.gephi.desktop.banner.OverviewPerspective")
    @ActionRegistration(displayName = "#CTL_OverviewPerspective")
    @ActionReferences({
        @ActionReference(path = "Menu/Window", position = 20),
        @ActionReference(path = "Shortcuts", name = "D-1")
    })
    public static ActionListener overview() {
        return select(OverviewPerspective.class);
    }

    @ActionID(category = "Window", id = "org.gephi.desktop.banner.LaboratoryPerspective")
    @ActionRegistration(displayName = "#CTL_LaboratoryPerspective")
    @ActionReferences({
        @ActionReference(path = "Menu/Window", position = 40),
        @ActionReference(path = "Shortcuts", name = "D-2")
    })
    public static ActionListener laboratory() {
        return select(LaboratoryPerspective.class);
    }

    @ActionID(category = "Window", id = "org.gephi.desktop.banner.PreviewPerspective")
    @ActionRegistration(displayName = "#CTL_PreviewPerspective")
    @ActionReferences({
        @ActionReference(path = "Menu/Window", position = 60, separatorAfter = 80),
        @ActionReference(path = "Shortcuts", name = "D-3")
    })
    public static ActionListener preview() {
        return select(PreviewPerspective.class);
    }

    private static ActionListener select(Class<? extends Perspective> perspectiveClass) {
        return e -> {
            PerspectiveController controller = Lookup.getDefault().lookup(PerspectiveController.class);
            for (Perspective perspective : controller.getPerspectives()) {
                if (perspectiveClass.isInstance(perspective)) {
                    controller.selectPerspective(perspective);
                    return;
                }
            }
        };
    }
}
