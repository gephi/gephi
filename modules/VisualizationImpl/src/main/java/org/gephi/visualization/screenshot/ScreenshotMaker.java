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

import java.awt.Cursor;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.visualization.VizArchitecture;
import org.gephi.viz.engine.VizEngine;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * @author Mathieu Bastian
 */
public class ScreenshotMaker implements VizArchitecture, LongTask, Runnable {

    private static final String DATE_FORMAT_NOW = "HHmmss";
    //Const
    private final String LAST_PATH = "ScreenshotMaker_Last_Path";
    private final String LAST_PATH_DEFAULT = "ScreenshotMaker_Last_Path_Default";
    private final String ANTIALIASING_DEFAULT = "ScreenshotMaker_Antialiasing_Default";
    private final String WIDTH_DEFAULT = "ScreenshotMaker_Width_Default";
    private final String HEIGHT_DEFAULT = "ScreenshotMaker_Height_Default";
    private final String TRANSPARENT_BACKGROUND_DEFAULT = "ScreenshotMaker_TransparentBackground_Default";
    private final String AUTOSAVE_DEFAULT = "ScreenshotMaker_Autosave_Default";
    private final String SHOW_MESSAGE = "ScreenshotMaker_Show_Message";
    //Executor
    private final LongTaskExecutor executor;
    //Architecture
    private VizEngine<?, ?> engine;
    private ProgressTicket progressTicket;
    private boolean cancel;
    //Settings
    private int antiAliasing = 2;
    private int width = 1024;
    private int height = 768;
    private int tileWidth = width / 16;
    private int tileHeight = height / 12;
    private boolean transparentBackground = false;
    private boolean finishedMessage = true;
    private boolean autoSave = false;
    private String defaultDirectory;
    //Running
    private File file;

    public ScreenshotMaker() {
        //Preferences
        String lastPathDefault = NbPreferences.forModule(ScreenshotMaker.class).get(LAST_PATH_DEFAULT, null);
        defaultDirectory = NbPreferences.forModule(ScreenshotMaker.class).get(LAST_PATH, lastPathDefault);
        antiAliasing = NbPreferences.forModule(ScreenshotMaker.class).getInt(ANTIALIASING_DEFAULT, antiAliasing);
        width = NbPreferences.forModule(ScreenshotMaker.class).getInt(WIDTH_DEFAULT, width);
        height = NbPreferences.forModule(ScreenshotMaker.class).getInt(HEIGHT_DEFAULT, height);
        transparentBackground = NbPreferences.forModule(ScreenshotMaker.class)
            .getBoolean(TRANSPARENT_BACKGROUND_DEFAULT, transparentBackground);
        autoSave = NbPreferences.forModule(ScreenshotMaker.class).getBoolean(AUTOSAVE_DEFAULT, autoSave);
        finishedMessage = NbPreferences.forModule(ScreenshotMaker.class).getBoolean(SHOW_MESSAGE, finishedMessage);

        executor = new LongTaskExecutor(true, "Screenshot Maker");

        tileWidth = width / 16;
        tileHeight = height / 12;
    }

    @Override
    public void initArchitecture() {
        //TODO
//        engine = VizController.getInstance().getEngine();
    }

    public void takeScreenshot() {
        executor
            .execute(this, this, NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.progress.message"), null);
    }

    @Override
    public void run() {
        beforeTaking();

        //TODO

        // After taking
        afterTaking();
    }

    private void beforeTaking() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
    }

    private void afterTaking() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                WindowManager.getDefault().getMainWindow().setCursor(Cursor.getDefaultCursor());
                if (finishedMessage && file != null) {
                    if (autoSave) {
                        final String msg = NbBundle
                            .getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.message",
                                file.getAbsolutePath());
                        StatusDisplayer.getDefault().setStatusText(msg);
                    } else {
                        final String msg = NbBundle
                            .getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.message",
                                file.getName());
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg,
                            NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.title"),
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }

    private String getDefaultFileName() {

        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_NOW);
        String datetime = dateFormat.format(cal.getTime());

        return "screenshot_" + datetime;
    }

    public void configure() {
        ScreenshotSettingsPanel panel = new ScreenshotSettingsPanel();
        panel.setup(this);
        ValidationPanel validationPanel = ScreenshotSettingsPanel.createValidationPanel(panel);
        if (validationPanel
            .showOkCancelDialog(NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.configure.title"))) {
            panel.unsetup(this);
        }
//        DialogDescriptor dd = new DialogDescriptor(validationPanel, NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.configure.title"));
//        Object result = DialogDisplayer.getDefault().notify(dd);
//        if (result == NotifyDescriptor.OK_OPTION) {
//            panel.unsetup(this);
//        }
    }

    public int getAntiAliasing() {
        return antiAliasing;
    }

    public void setAntiAliasing(int antiAliasing) {
        this.antiAliasing = antiAliasing;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    public boolean isTransparentBackground() {
        return transparentBackground;
    }

    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    public void setDefaultDirectory(File directory) {
        if (directory != null && directory.exists()) {
            defaultDirectory = directory.getAbsolutePath();
            NbPreferences.forModule(ScreenshotMaker.class).put(LAST_PATH, defaultDirectory);
        }
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }
}
