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

package org.gephi.visualization.screenshot;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.gephi.desktop.visualization.screenshot.ScreenshotSettingsPanel;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.visualization.api.ScreenshotController;
import org.gephi.visualization.api.ScreenshotModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.viz.engine.VizEngine;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Mathieu Bastian
 */
public class ScreenshotControllerImpl implements ScreenshotController {

    private final LongTaskExecutor executor;
    //Architecture
    private VizEngine<?, ?> engine;

    public ScreenshotControllerImpl() {
        executor = new LongTaskExecutor(true, "Screenshot Maker");
    }


    public ScreenshotModelImpl newModel(Workspace workspace) {
        VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
        return new ScreenshotModelImpl(vizController.getModel(workspace));
    }

    @Override
    public void setAntiAliasing(int antiAliasing) {

    }

    @Override
    public void setAutoSave(boolean autoSave) {

    }

    @Override
    public void setDefaultDirectory(File directory) {

    }

    @Override
    public void setHeight(int height) {

    }

    @Override
    public void setTransparentBackground(boolean transparentBackground) {

    }

    @Override
    public void setWidth(int width) {

    }

    public void takeScreenshot() {
        // Todo Fix
//        ScreenshotTask task = new ScreenshotTask();
//        executor
//            .execute(this, this, NbBundle.getMessage(ScreenshotControllerImpl.class, "ScreenshotMaker.progress.message"), null);
    }


    // Todo fix
//    private String getDefaultFileName() {
//
//        Calendar cal = Calendar.getInstance();
//        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_NOW);
//        String datetime = dateFormat.format(cal.getTime());
//
//        return "screenshot_" + datetime;
//    }

    public void configure() {
        // TODO fix
//        ScreenshotSettingsPanel panel = new ScreenshotSettingsPanel();
//        panel.setup(this);
//        ValidationPanel validationPanel = ScreenshotSettingsPanel.createValidationPanel(panel);
//        if (validationPanel
//            .showOkCancelDialog(NbBundle.getMessage(ScreenshotControllerImpl.class, "ScreenshotMaker.configure.title"))) {
//            panel.unsetup(this);
//        }
//        DialogDescriptor dd = new DialogDescriptor(validationPanel, NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.configure.title"));
//        Object result = DialogDisplayer.getDefault().notify(dd);
//        if (result == NotifyDescriptor.OK_OPTION) {
//            panel.unsetup(this);
//        }
    }
}
