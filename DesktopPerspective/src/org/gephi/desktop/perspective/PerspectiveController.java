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
package org.gephi.desktop.perspective;

import org.gephi.desktop.perspective.spi.Perspective;
import org.gephi.desktop.perspective.spi.PerspectiveMember;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class PerspectiveController {

    private static final String SELECTED_PERSPECTIVE_PREFERENCE = "BannerComponent_selectedPerspective";
    //Data
    private String selectedPerspective;
    private final Perspective[] perspectives;
    private final PerspectiveMember[] members;

    public PerspectiveController() {
        //Load perspectives
        perspectives = Lookup.getDefault().lookupAll(Perspective.class).toArray(new Perspective[0]);
        members = Lookup.getDefault().lookupAll(PerspectiveMember.class).toArray(new PerspectiveMember[0]);

        //Set to previously selected perspective
        selectedPerspective = NbPreferences.forModule(BannerComponent.class).get(SELECTED_PERSPECTIVE_PREFERENCE, perspectives[0].getName());
        Perspective selectedPerspectiveInstance = getSelectedPerspective();

        //Open members
        for (PerspectiveMember perspectiveMember : members) {
            if (perspectiveMember.isMemberOf(selectedPerspectiveInstance)) {
                String pId = perspectiveMember.getTopComponentId();
                TopComponent c = WindowManager.getDefault().findTopComponent(pId);
                if (c != null && !c.isOpened()) {
                    c.open();
                }
            }
        }
    }

    public Perspective[] getPerspectives() {
        return perspectives;
    }

    public Perspective getSelectedPerspective() {
        for (Perspective p : perspectives) {
            if (p.getName().equals(selectedPerspective)) {
                return p;
            }
        }
        return null;
    }

    public int getSelectedPerspectiveIndex() {
        int i = 0;
        for (Perspective p : perspectives) {
            if (p.getName().equals(selectedPerspective)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void select(Perspective perspective) {
        if (perspective.getName().equals(selectedPerspective)) {
            return;
        }

        //Close other perspective based on group name
        for (Perspective g : perspectives) {
            if (g != perspective) {
                TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(g.getName());
                tpg.close();
            }
        }

        //Open perspective
        TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(perspective.getName());
        tpg.open();

        //Close members
        for (TopComponent c : WindowManager.getDefault().getRegistry().getOpened()) {
            String pId = WindowManager.getDefault().findTopComponentID((TopComponent) c);
            for (PerspectiveMember perspectiveMember : members) {
                if (pId.equals(perspectiveMember.getTopComponentId()) && !perspectiveMember.isMemberOf(perspective)) {
                    boolean closed = c.close();
                }
            }
        }


        //Open members
        for (PerspectiveMember perspectiveMember : members) {
            if (perspectiveMember.isMemberOf(perspective)) {
                String pId = perspectiveMember.getTopComponentId();
                TopComponent c = WindowManager.getDefault().findTopComponent(pId);
                if (c != null && !c.isOpened()) {
                    c.open();
                }
            }
        }

        selectedPerspective = perspective.getName();
        NbPreferences.forModule(BannerComponent.class).put(SELECTED_PERSPECTIVE_PREFERENCE, selectedPerspective);
    }
}
