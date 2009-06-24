package org.gephi.visualization.swing;

import com.sun.opengl.util.BufferUtil;
import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.config.GraphicalConfiguration;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.opengl.Lighting;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class GLAbstractListener implements GLEventListener {

    protected GLAutoDrawable drawable;
    protected VizConfig vizConfig;
    protected static final GLU glu = new GLU();
    private static final boolean DEBUG = true;
    private long startTime = 0;
    protected float fps;
    private volatile boolean resizing = false;
    private final float viewField = 30.0f;
    public final float nearDistance = 1.0f;
    public final float farDistance = 100000f;
    private double aspectRatio = 0;
    protected DoubleBuffer projMatrix = BufferUtil.newDoubleBuffer(16);
    protected DoubleBuffer modelMatrix = BufferUtil.newDoubleBuffer(16);
    protected IntBuffer viewport = BufferUtil.newIntBuffer(4);

    protected void initDrawable(GLAutoDrawable drawable) {
        this.drawable = drawable;
        drawable.addGLEventListener(this);
    }

    protected abstract void init(GL gl);

    protected abstract void render3DScene(GL gl, GLU glu);

    protected abstract void reshape3DScene(GL gl);

    protected abstract void setCameraPosition(GL gl, GLU glu);

    protected GLCapabilities getCaps() {
        GLCapabilities caps = new GLCapabilities();
        caps.setAlphaBits(8);		//if NOT opaque
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        //FSAA
        if (vizConfig.getAntialiasing() == 2) {
            caps.setSampleBuffers(true);
            caps.setNumSamples(2);
        } else if (vizConfig.getAntialiasing() == 4) {
            caps.setSampleBuffers(true);
            caps.setNumSamples(4);
        } else if (vizConfig.getAntialiasing() == 8) {
            caps.setSampleBuffers(true);
            caps.setNumSamples(8);
        } else if (vizConfig.getAntialiasing() == 16) {
            caps.setSampleBuffers(true);
            caps.setNumSamples(16);
        }

        return caps;
    }

    public void initConfig(GL gl) {
        //Disable Vertical synchro
        gl.setSwapInterval(0);

        //Depth
        if (vizConfig.use3d()) {
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthFunc(GL.GL_LEQUAL);
            gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);	//Correct texture & colors perspective calculations
        } else {
            gl.glDisable(GL.GL_DEPTH_TEST);
        }

        //Cull face
        if (vizConfig.isCulling()) {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
        }


        if (vizConfig.isPointSmooth()) {
            gl.glEnable(GL.GL_POINT_SMOOTH);
            gl.glHint(GL.GL_POINT_SMOOTH_HINT, GL.GL_NICEST); //Point smoothing
        } else {
            gl.glDisable(GL.GL_POINT_SMOOTH);
        }

        if (vizConfig.isLineSmooth()) {
            gl.glEnable(GL.GL_LINE_SMOOTH);
            if (vizConfig.isLineSmoothNicest()) {
                gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
            } else {
                gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_FASTEST);
            }
        } else {
            gl.glDisable(GL.GL_LINE_SMOOTH);
        }

        gl.glClearDepth(1.0f);

        //Background
        Color backgroundColor = vizConfig.getBackgroundColor();
        gl.glClearColor(backgroundColor.getRed() / 255f, backgroundColor.getGreen() / 255f, backgroundColor.getBlue() / 255f, 1f);

        gl.glShadeModel(GL.GL_SMOOTH);

        //Lighting
        if (vizConfig.isLighting()) {
            gl.glEnable(GL.GL_LIGHTING);
            setLighting(gl);
        } else {
            gl.glDisable(GL.GL_LIGHTING);
        }

        //Blending
        if (vizConfig.isBlending()) {
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        //gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
        // gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
        }
        // gl.glEnable(GL.GL_ALPHA_TEST);
        //gl.glAlphaFunc(GL.GL_GREATER, 0);

        //Material
        if (vizConfig.isMaterial()) {
            gl.glColorMaterial(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE);
            gl.glEnable(GL.GL_COLOR_MATERIAL);
        }
        //Mesh view
        if (vizConfig.isWireFrame()) {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
        }

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_NORMALIZE);

    }

    protected void setLighting(GL gl) {

        //Lights
        if (vizConfig.use3d()) {
            Lighting.setSource(0, Lighting.TYPE_AMBIANT, gl);//
            Lighting.setSource(2, Lighting.TYPE_BAS_ROUGE, gl);//
            Lighting.setSource(3, Lighting.TYPE_GAUCHE_JAUNE, gl);//
            Lighting.setSource(4, Lighting.TYPE_HAUT_BLEU, gl);//
            Lighting.setSource(5, Lighting.TYPE_LATERAL_BLANC, gl);
            Lighting.setSource(6, Lighting.TYPE_LATERAL_MULTI, gl);
            Lighting.setSource(7, Lighting.TYPE_SPOT_BLAFARD, gl);
            Lighting.switchAll(gl, true, false, true, true, true, false, false, false);
        } else {
            gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, new float[] {0f, 0f, 0f, 1f}, 0);
            gl.glEnable(GL.GL_LIGHT0);
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        GraphicalConfiguration graphicalConfiguration = new GraphicalConfiguration();
        graphicalConfiguration.checkGeneralCompatibility(gl);

        resizing = false;
        initConfig(gl);
        init(gl);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (startTime == 0) {
            startTime = System.currentTimeMillis() - 1;
        }

        long endTime = System.currentTimeMillis();
        long tpsEcoule = endTime - startTime;
        startTime = endTime;
        fps = (int) (1000.0f / tpsEcoule);

        GL gl = drawable.getGL();
        if (vizConfig.use3d()) {
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        } else {
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }


        render3DScene(gl, glu);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        if (!resizing) {
            resizing = true;
            if (viewport.get(2) == width && viewport.get(3) == height)//NO need
            {
                return;
            }

            if (height == 0) {
                height = 1;
            }
            if (width == 0) {
                width = 1;
            }

            int viewportW = 0, viewportH = 0, viewportX = width, viewportY = height;

            aspectRatio = (double) width / (double) height;
            viewportH = height;
            viewportW = (int) (height * aspectRatio);
            if (viewportW > width) {
                viewportW = width;
                viewportH = (int) (width * (1 / aspectRatio));
            }
            viewportX = ((width - viewportW) / 2);
            viewportY = ((height - viewportH) / 2);

            GL gl = drawable.getGL();

            gl.glViewport(viewportX, viewportY, viewportW, viewportH);
            gl.glGetIntegerv(GL.GL_VIEWPORT, viewport);//Update viewport buffer

            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glLoadIdentity();
            glu.gluPerspective(viewField, aspectRatio, nearDistance, farDistance);
            gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projMatrix);//Update projection buffer


            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glLoadIdentity();

            reshape3DScene(drawable.getGL());

            if (DEBUG) {
                System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
                System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
                System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
            }
            resizing = false;
        }
    }

    public GL getGL() {
        return drawable.getGL();
    }

    @Override
    public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
    }

    public void setVizConfig(VizConfig vizConfig) {
        this.vizConfig = vizConfig;
    }

    public GLAutoDrawable getGLAutoDrawable() {
        return drawable;
    }
}
