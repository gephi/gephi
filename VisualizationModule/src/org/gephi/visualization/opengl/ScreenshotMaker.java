/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.opengl;

import com.sun.opengl.util.FileUtil;
import com.sun.opengl.util.ImageUtil;
import com.sun.opengl.util.TileRenderer;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.tools.FileObject;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.swing.GLAbstractListener;
import org.gephi.visualization.swing.GraphDrawableImpl;
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

    //Architecture
    private GraphDrawableImpl drawable;
    private AbstractEngine engine;

    //Settings
    private int antiAliasing = 0;
    private int width;
    private int height;
    private boolean transparentBackground = false;
    private boolean finishedMessage = true;
    private boolean askForFilePath = true;

    //Running
    private File file;

    //State
    private boolean takeTicket = false;

    public void initArchitecture() {
        drawable = VizController.getInstance().getDrawable();
        engine = VizController.getInstance().getEngine();
    }

    public void takeScreenshot() {
        this.width = 1024;
        this.height = 1024;

        this.file = new File("test.png");

        takeTicket = true;
    }

    private void take(File file) throws Exception {

        //System.out.println("Take Screenshot to " + file.getName());

        // Fix the image size for now
        int tileWidth = width / 16;
        int tileHeight = height / 12;
        int imageWidth = width;
        int imageHeight = height;

        //Caps
        GLCapabilities caps = new GLCapabilities();
        caps.setAlphaBits(8);
        caps.setDoubleBuffered(false);
        caps.setHardwareAccelerated(true);
        caps.setSampleBuffers(true);
        caps.setNumSamples(antiAliasing);

        //Buffer
        GLPbuffer pbuffer = GLDrawableFactory.getFactory().createGLPbuffer(caps, null, tileWidth, tileHeight, null);
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
            tileRenderer.setImageBuffer(GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, imageBuffer);
        } else {
            tileRenderer.setImageBuffer(GL.GL_BGR, GL.GL_UNSIGNED_BYTE, imageBuffer);
        }
        tileRenderer.trPerspective(drawable.viewField, (float) imageWidth / (float) imageHeight, drawable.nearDistance, drawable.farDistance);

        //Get gl
        //GLContext oldContext = GLContext.getCurrent();
        GLContext context = pbuffer.getContext();
        if (context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT) {
            throw new RuntimeException("Error making pbuffer's context current");
        }
        GL gl = pbuffer.getGL();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();

        //Init
        drawable.initConfig(gl);
        engine.initScreenshot(gl, GLAbstractListener.glu);

        //Render in buffer
        do {
            tileRenderer.beginTile(gl);
            drawable.renderScreenshot(pbuffer);
        } while (tileRenderer.endTile(gl));

        //Clean
        context.release();
        pbuffer.destroy();

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
        if (askForFilePath) {
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
            if (returnFile == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();

                if (!file.getPath().endsWith(".png")) {
                    file = new File(file.getPath() + ".png");
                }

                //Save last path
                NbPreferences.forModule(ScreenshotMaker.class).put(LAST_PATH, file.getAbsolutePath());
            }
        }
        if (!ImageIO.write(image, FileUtil.getFileSuffix(file), file)) {
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

    private void beforeTaking() {
        WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void afterTaking() {
        WindowManager.getDefault().getMainWindow().setCursor(Cursor.getDefaultCursor());
        if (finishedMessage) {
            String msg = NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.message", file.getName());
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(ScreenshotMaker.class, "ScreenshotMaker.finishedMessage.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private static final String DATE_FORMAT_NOW = "HHmmss";

    private String getDefaultFileName() {

        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_NOW);
        String datetime = dateFormat.format(cal.getTime());

        return "screenshot_" + datetime;
    }
}
