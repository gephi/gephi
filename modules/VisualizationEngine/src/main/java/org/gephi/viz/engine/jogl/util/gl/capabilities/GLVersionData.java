package org.gephi.viz.engine.jogl.util.gl.capabilities;

/**
 *
 * @author gbarbieri
 */
public class GLVersionData {

    public GLVersionData(Profile profile) {
        PROFILE = profile;
    }

    public Profile PROFILE;
    public int MAJOR_VERSION;
    public int MINOR_VERSION;
    public int CONTEXT_FLAGS;
    public int NUM_EXTENSIONS;
    public String RENDERER;
    public String VENDOR;
    public String VERSION;
    public String SHADING_LANGUAGE_VERSION;

    @Override
    public String toString() {
        return "GLVersionData{" + "PROFILE=" + PROFILE + ", MAJOR_VERSION=" + MAJOR_VERSION + ", MINOR_VERSION=" + MINOR_VERSION + ", CONTEXT_FLAGS=" + CONTEXT_FLAGS + ", NUM_EXTENSIONS=" + NUM_EXTENSIONS + ", RENDERER=" + RENDERER + ", VENDOR=" + VENDOR + ", VERSION=" + VERSION + ", SHADING_LANGUAGE_VERSION=" + SHADING_LANGUAGE_VERSION + '}';
    }
}
