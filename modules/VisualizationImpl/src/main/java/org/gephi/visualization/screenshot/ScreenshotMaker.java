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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.util.GLPixelBuffer;
import com.jogamp.opengl.util.GLPixelBuffer.GLPixelAttributes;
import com.jogamp.opengl.util.TileRenderer;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import java.awt.Cursor;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.*;
import org.gephi.visualization.text.TextManager;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class ScreenshotMaker implements VizArchitecture, LongTask, Runnable {

    //Const
    private final String LAST_PATH = "ScreenshotMaker_Last_Path";
    private final String LAST_PATH_DEFAULT = "ScreenshotMaker_Last_Path_Default";
    private final String ANTIALIASING_DEFAULT = "ScreenshotMaker_Antialiasing_Default";
    private final String WIDTH_DEFAULT = "ScreenshotMaker_Width_Default";
    private final String HEIGHT_DEFAULT = "ScreenshotMaker_Height_Default";
    private final String TRANSPARENT_BACKGROUND_DEFAULT = "ScreenshotMaker_TransparentBackground_Default";
    private final String AUTOSAVE_DEFAULT = "ScreenshotMaker_Autosave_Default";
    private final String SHOW_MESSAGE = "ScreenshotMaker_Show_Message";
    //Architecture
    private AbstractEngine engine;
    private VizConfig vizConfig;
    private TextManager textManager;
    //Executor
    private final LongTaskExecutor executor;
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
        transparentBackground = NbPreferences.forModule(ScreenshotMaker.class).getBoolean(TRANSPARENT_BACKGROUND_DEFAULT, transparentBackground);
        autoSave = NbPreferences.forModule(ScreenshotMaker.class).getBoolean(AUTOSAVE_DEFAULT, autoSave);
        finishedMessage = NbPreferences.forModule(ScreenshotMaker.class).getBoolean(SHOW_MESSAGE, finishedMessage);

        executor = new LongTaskExecutor(true, "Screenshot Maker");

        tileWidth = width / 16;
        tileHeight = height / 12;
    }

    @Override
    public void initArchitecture() {
        engine = VizController.getInstance().getEngine();
        vizConfig = VizController.getInstance().getVizConfig();
        textManager = VizController.getInstance().getTextManager();
    }

    public void takeScreenshot() {
        executor.execute(this, this, NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.progress.message"), null);
    }

    @Override
    public void run() {
        beforeTaking();

        try {
            // Stop display
            engine.stopDisplay();

            // Start progress
            Progress.start(progressTicket);

            Thread.sleep(100);

            final OffscreenCanvas drawable = new OffscreenCanvas(tileWidth, tileHeight, transparentBackground, antiAliasing);
            GLAutoDrawable autoDrawable = drawable.getGLAutoDrawable();

            //Tile rendering
            final TileRenderer renderer = new TileRenderer();
            renderer.setImageSize(width, height);
            renderer.setTileSize(tileWidth, tileHeight, 0);
            renderer.attachAutoDrawable(autoDrawable);

            final GLPixelBuffer.GLPixelBufferProvider pixelBufferProvider = GLPixelBuffer.defaultProviderWithRowStride;
            final boolean[] flipVertically = {false};

            final GLEventListener preTileGLEL = new GLEventListener() {
                @Override
                public void init(final GLAutoDrawable drawable) {
                    final GL2 gl = drawable.getGL().getGL2();
                    final GLPixelAttributes pixelAttribs = pixelBufferProvider.getAttributes(gl, transparentBackground ? 4 : 3, true);

                    final GLPixelBuffer imageBuffer = pixelBufferProvider.allocate(gl, null, pixelAttribs, true, width, height, 1, 0);
                    renderer.setImageBuffer(imageBuffer);

                    flipVertically[0] = !drawable.isGLOriented();
                }

                @Override
                public void dispose(final GLAutoDrawable drawable) {
                }

                @Override
                public void display(final GLAutoDrawable drawable) {
                }

                @Override
                public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height) {
                }
            };
            renderer.setGLEventListener(preTileGLEL, null);

            vizConfig.setDisableLOD(true);
            engine.updateLOD();

            // Render tiles
            int tiles = renderer.getParam(TileRenderer.TR_COLUMNS) * renderer.getParam(TileRenderer.TR_ROWS);
            Progress.switchToDeterminate(progressTicket, tiles);
            while (!renderer.eot() && !cancel) {
                renderer.display();
                Progress.progress(progressTicket);
            }
            Progress.switchToIndeterminate(progressTicket);

            renderer.detachAutoDrawable();

            autoDrawable.invoke(true, new GLRunnable() {
                @Override
                public boolean run(final GLAutoDrawable drawable) {
                    drawable.getGL().glViewport(0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
//                drawable.reshape(drawable, 0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
                    return false;
                }
            });

            vizConfig.setDisableLOD(false);
            engine.updateLOD();

            if (!cancel) {
                final GLPixelBuffer imageBuffer = renderer.getImageBuffer();

                final TextureData textureData = new TextureData(
                        autoDrawable.getChosenGLCapabilities().getGLProfile(),
                        transparentBackground ? GL.GL_RGBA : GL.GL_RGB,
                        width, height,
                        0,
                        imageBuffer.pixelAttributes,
                        false, false,
                        flipVertically[0],
                        imageBuffer.buffer,
                        null /* Flusher */);

                // Get File
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        if (!autoSave) {
                            //Get last directory
                            String lastPathDefault = NbPreferences.forModule(ScreenshotMaker.class).get(LAST_PATH_DEFAULT, null);
                            String lastPath = NbPreferences.forModule(ScreenshotMaker.class).get(LAST_PATH, lastPathDefault);
                            final JFileChooser chooser = new JFileChooser(lastPath);
                            chooser.setAcceptAllFileFilterUsed(false);
                            chooser.setDialogTitle(NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.filechooser.title"));
                            DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.filechooser.pngDescription"));
                            dialogFileFilter.addExtension("png");
                            chooser.addChoosableFileFilter(dialogFileFilter);
                            File selectedFile = new File(chooser.getCurrentDirectory(), getDefaultFileName() + ".png");
                            chooser.setSelectedFile(selectedFile);
                            int returnFile = chooser.showSaveDialog(null);
                            if (returnFile != JFileChooser.APPROVE_OPTION) {
                                return;
                            }
                            ScreenshotMaker.this.file = chooser.getSelectedFile();

                            if (!ScreenshotMaker.this.file.getPath().endsWith(".png")) {
                                ScreenshotMaker.this.file = new File(ScreenshotMaker.this.file.getPath() + ".png");
                            }

                            //Save last path
                            defaultDirectory = ScreenshotMaker.this.file.getParentFile().getAbsolutePath();
                            NbPreferences.forModule(ScreenshotMaker.class).put(LAST_PATH, defaultDirectory);

                        } else {
                            ScreenshotMaker.this.file = new File(defaultDirectory, getDefaultFileName() + ".png");
                        }
                    }
                });

                // Write file
                if (file != null) {
                    TextureIO.write(textureData, file);
                }
            }

            autoDrawable.destroy();

            //Reinit text renderer
            textManager.reinitRenderers();

            engine.startDisplay();

            //Progress finish
            Progress.finish(progressTicket);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

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
                        final String msg = NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.message", file.getAbsolutePath());
                        StatusDisplayer.getDefault().setStatusText(msg);
                    } else {
                        final String msg = NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.message", file.getName());
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.title"), JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }

    private static final String DATE_FORMAT_NOW = "HHmmss";

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
        if (validationPanel.showOkCancelDialog(NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.configure.title"))) {
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
