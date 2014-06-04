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

import com.jogamp.opengl.util.awt.ImageUtil;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.media.nativewindow.AbstractGraphicsDevice;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;
import javax.media.opengl.GLProfile;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.*;
import org.gephi.visualization.swing.GLAbstractListener;
import org.gephi.visualization.swing.GraphDrawableImpl;
import org.gephi.visualization.text.TextManager;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class ScreenshotMaker implements VizArchitecture {

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
    private GraphDrawableImpl drawable;
    private AbstractEngine engine;
    private TextManager textManager;
    private VizConfig vizConfig;
    //Settings
    private int antiAliasing = 2;
    private int width = 1024;
    private int height = 768;
    private boolean transparentBackground = false;
    private boolean finishedMessage = true;
    private boolean autoSave = false;
    private String defaultDirectory;
    //Running
    private File file;
    //State
    private boolean takeTicket = false;

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
    }

    @Override
    public void initArchitecture() {
        drawable = VizController.getInstance().getDrawable();
        engine = VizController.getInstance().getEngine();
        textManager = VizController.getInstance().getTextManager();
        vizConfig = VizController.getInstance().getVizConfig();
    }

    public void takeScreenshot() {
        takeTicket = true;
    }

    private static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }

        if (ext == null) {
            return "";
        }
        return ext;
    }

    private void take(File file) throws Exception {

        //System.out.println("Take Screenshot to " + file.getName());

        // Fix the image size for now
        int tileWidth = width / 16;
        int tileHeight = height / 12;
        int imageWidth = width;
        int imageHeight = height;

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        AbstractGraphicsDevice device = GLDrawableFactory.getFactory(profile).getDefaultDevice();
        //Caps

        caps.setAlphaBits(8);
        caps.setDoubleBuffered(false);
        caps.setHardwareAccelerated(true);
        caps.setSampleBuffers(true);
        caps.setNumSamples(antiAliasing);

        //Buffer

        GLPbuffer pbuffer = GLDrawableFactory.getFactory(profile).createGLPbuffer(device, caps, null, tileWidth, tileHeight, null);
        BufferedImage image = null;
        if (transparentBackground) {
            image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        } else {
            image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
        }
        ByteBuffer imageBuffer = ByteBuffer.wrap(((DataBufferByte) image.getRaster().getDataBuffer()).getData());

        //Tile rendering
        TileRenderer tileRenderer = new TileRenderer();
        tileRenderer.setTileSize(tileWidth, tileHeight, 0);
        tileRenderer.setImageSize(imageWidth, imageHeight);
        if (transparentBackground) {
            tileRenderer.setImageBuffer(GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE, imageBuffer);
        } else {
            tileRenderer.setImageBuffer(GL2.GL_BGR, GL2.GL_UNSIGNED_BYTE, imageBuffer);
        }
        tileRenderer.trPerspective(drawable.viewField, (float) imageWidth / (float) imageHeight, drawable.nearDistance, drawable.farDistance);

        //Get gl
        //GLContext oldContext = GLContext.getCurrent();
        GLContext context = pbuffer.getContext();
        if (context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT) {
            throw new RuntimeException("Error making pbuffer's context current");
        }

        System.out.println("Disabling snapshot");

        GL2 gl = pbuffer.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        //Init
        drawable.initConfig(gl);
        vizConfig.setDisableLOD(true);
        engine.initScreenshot(gl, GLAbstractListener.glu);


        //Textrender - swap to 3D
        textManager.setRenderer3d(true);

        //Render in buffer
        do {
            tileRenderer.beginTile(gl);
            drawable.renderScreenshot(pbuffer);
        } while (tileRenderer.endTile(gl));

        //Clean
        context.release();
        pbuffer.destroy();


        //Textrender - back to 2D
        textManager.setRenderer3d(false);
        vizConfig.setDisableLOD(false);
        //Write image
        ImageUtil.flipImageVertically(image);
        writeImage(image);

        /*Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
         if (iter.hasNext()) {
         ImageWriter writer = iter.next();
         ImageWriteParam iwp = writer.getDefaultWriteParam();
         //iwp.setCompressionType("DEFAULT");
         //iwp.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
         //iwp.setCompressionQuality((int)(9*pngCompresssion));
         FileImageOutputStream output = new FileImageOutputStream(file);
         writer.setOutput(output);
         IIOImage img = new IIOImage(image, null, null);
         writer.write(null, img, iwp);
         writer.dispose();
         }*/

        //oldContext.makeCurrent();
    }

    private void writeImage(BufferedImage image) throws Exception {
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
            file = chooser.getSelectedFile();

            if (!file.getPath().endsWith(".png")) {
                file = new File(file.getPath() + ".png");
            }

            //Save last path
            defaultDirectory = file.getParentFile().getAbsolutePath();
            NbPreferences.forModule(ScreenshotMaker.class).put(LAST_PATH, defaultDirectory);

        } else {
            file = new File(defaultDirectory, getDefaultFileName() + ".png");
        }
        String format = "png";
        if (file != null) {
            format = getExtension(file);
        }
        if (!ImageIO.write(image, format, file)) {
            throw new IOException("Unsupported file format");
        }
    }

    public void openglSignal(GLAutoDrawable drawable) {
        if (takeTicket) {
            takeTicket = false;
            try {
                beforeTaking();
                take(file);
                drawable.getContext().makeCurrent();
                afterTaking();
                file = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void beforeTaking() throws InterruptedException, InvocationTargetException {
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
					final String msg = NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.message", file.getName());
					JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.title"), JOptionPane.INFORMATION_MESSAGE);
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
            return;
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
}
