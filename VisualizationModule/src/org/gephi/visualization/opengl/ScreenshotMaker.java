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

import com.sun.opengl.util.ImageUtil;
import com.sun.opengl.util.TileRenderer;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.swing.GLAbstractListener;
import org.gephi.visualization.swing.GraphDrawableImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class ScreenshotMaker implements VizArchitecture {

    private GraphDrawableImpl drawable;
    private int antiAliasing = 2;
    private int width;
    private int height;
    private boolean transparentBackground;
    private AbstractEngine engine;
    private File file;

    //State
    private boolean takeTicket = false;

    public void initArchitecture() {
        drawable = VizController.getInstance().getDrawable();
        engine = VizController.getInstance().getEngine();
    }

    public void takeScreenshot() {
        this.width = 2048;
        this.height = 2048;

        this.file = new File("test.png");

        takeTicket = true;
    }

    private void take(File file) throws Exception {

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
        GLContext oldContext = GLContext.getCurrent();
        GLContext context = pbuffer.getContext();
        if (context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT) {
            throw new RuntimeException("Error making pbuffer's context current");
        }
        GL gl = pbuffer.getGL();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();

        //Init
        drawable.initConfig(gl);
        engine.initEngine(gl, GLAbstractListener.glu);

        //Render in buffer
        do {
            tileRenderer.beginTile(gl);
            drawable.renderScreenshot(pbuffer);
        //drawable.display(pbuffer);
        } while (tileRenderer.endTile(gl));

        //Clean
        context.release();
        pbuffer.destroy();

        //Write image
        ImageUtil.flipImageVertically(image);
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
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
        }

    //oldContext.makeCurrent();
    }

    public void openglSignal(GLAutoDrawable drawable) {
        if (takeTicket) {
            try {
                take(file);
                file = null;
                drawable.getContext().makeCurrent();
            } catch (Exception e) {
                e.printStackTrace();
            }
            takeTicket = false;
        }
    }
}
