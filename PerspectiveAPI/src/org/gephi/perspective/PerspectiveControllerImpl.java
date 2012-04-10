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
package org.gephi.perspective;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import org.gephi.perspective.api.PerspectiveController;
import org.gephi.perspective.spi.Perspective;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = PerspectiveController.class)
public class PerspectiveControllerImpl implements PerspectiveController {

    private static final String SELECTED_PERSPECTIVE_PREFERENCE = "PerspectiveControllerImpl_selectedPerspective";
    //Data
    private String selectedPerspective;
    private final Perspective[] perspectives;

    public PerspectiveControllerImpl() {
        //Load perspectives
        perspectives = Lookup.getDefault().lookupAll(Perspective.class).toArray(new Perspective[0]);

        //Find if there is a default
        String firstPerspective = perspectives.length > 0 ? perspectives[0].getName() : null;
        String defaultPerspectiveName = System.getProperty("org.gephi.perspective.default");
        if (defaultPerspectiveName != null) {
            for (Perspective p : perspectives) {
                if (p.getName().equals(defaultPerspectiveName)) {
                    selectedPerspective = p.getName();
                    break;
                }
            }
        }
        if (selectedPerspective == null) {
            selectedPerspective = NbPreferences.root().get(SELECTED_PERSPECTIVE_PREFERENCE, firstPerspective);
        }

        //Store selected in prefs
        NbPreferences.root().put(SELECTED_PERSPECTIVE_PREFERENCE, selectedPerspective);

        Perspective selectedPerspectiveInstance = getSelectedPerspective();

        openAndCloseMembers(selectedPerspectiveInstance);

        WindowManager.getDefault().addWindowSystemListener(new WindowSystemListener() {

            private Dimension lastDimension = null;
            private Integer lastState = null;
            private Point lastLocation = null;

            @Override
            public void beforeLoad(WindowSystemEvent event) {
            }

            @Override
            public void afterLoad(WindowSystemEvent event) {
                Frame mainWindow = WindowManager.getDefault().getMainWindow();
                if (mainWindow != null) {
                    if (lastDimension != null) {
                        mainWindow.setSize(lastDimension);
                    }
                    if(lastLocation!=null){
                        mainWindow.setLocation(lastLocation);
                    }
                    if (lastState != null) {
                        mainWindow.setState(lastState);
                    }
                }
            }

            @Override
            public void beforeSave(WindowSystemEvent event) {
                Frame mainWindow = WindowManager.getDefault().getMainWindow();
                if (mainWindow != null) {
                    lastDimension = mainWindow.getSize();
                    lastLocation = mainWindow.getLocation();
                    lastState = mainWindow.getExtendedState();
                }
            }

            @Override
            public void afterSave(WindowSystemEvent event) {
            }
        });
    }

    @Override
    public Perspective[] getPerspectives() {
        return perspectives;
    }

    @Override
    public Perspective getSelectedPerspective() {
        for (Perspective p : perspectives) {
            if (p.getName().equals(selectedPerspective)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void selectPerspective(Perspective perspective) {
        if (perspective.getName().equals(selectedPerspective)) {
            return;
        }

        openAndCloseMembers(perspective);

        selectedPerspective = perspective.getName();
        NbPreferences.root().put(SELECTED_PERSPECTIVE_PREFERENCE, selectedPerspective);
    }

    private void openAndCloseMembers(Perspective perspective) {
        WindowManager.getDefault().setRole(perspective.getName());
//        //Close other perspective based on group name
//        for (Perspective g : perspectives) {
//            if (g != perspective) {
//                TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(g.getName());
//                if (tpg != null) {
//                    tpg.close();
//                }
//            }
//        }
//
//        //Open perspective
//        TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(perspective.getName());
//        if (tpg != null) {
//            tpg.open();
//        }
//
//
//        //Close members
//        for (TopComponent c : WindowManager.getDefault().getRegistry().getOpened()) {
//            String pId = WindowManager.getDefault().findTopComponentID((TopComponent) c);
//            for (PerspectiveMember perspectiveMember : members) {
//                if (pId.equals(perspectiveMember.getTopComponentId()) && !perspectiveMember.isMemberOf(perspective)) {
//                    boolean closed = c.close();
//                }
//            }
//        }
//
//
//        //Open members
//        for (PerspectiveMember perspectiveMember : members) {
//            if (perspectiveMember.isMemberOf(perspective)) {
//                String pId = perspectiveMember.getTopComponentId();
//                TopComponent c = WindowManager.getDefault().findTopComponent(pId);
//                if (c != null && !c.isOpened()) {
//                    c.open();
//                }
//            }
//        }
    }
}
