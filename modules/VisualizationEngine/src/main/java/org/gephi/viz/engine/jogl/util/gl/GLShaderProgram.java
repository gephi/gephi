package org.gephi.viz.engine.jogl.util.gl;

import com.jogamp.opengl.GL2ES2;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eduardo Ramos
 */
public class GLShaderProgram {

    private final String srcRoot;
    private final String vertBasename;
    private final String fragBasename;
    private int id = -1;

    private final Map<String, Integer> uniformLocations;
    private final Map<String, Integer> attribLocations;
    private boolean initDone = false;

    public GLShaderProgram(String srcRoot, String vertBasename) {
        this(srcRoot, vertBasename, null);
    }

    public GLShaderProgram(String srcRoot, String vertBasename, String fragBasename) {
        this.srcRoot = srcRoot;
        this.vertBasename = vertBasename;
        this.fragBasename = fragBasename;
        this.uniformLocations = new HashMap<>();
        this.attribLocations = new HashMap<>();
    }

    public GLShaderProgram addUniformName(String name) {
        uniformLocations.put(name, null);
        return this;
    }

    public GLShaderProgram addAttribName(String name) {
        attribLocations.put(name, null);
        return this;
    }

    public GLShaderProgram addAttribLocation(String name, int location) {
        attribLocations.put(name, location);
        return this;
    }

    public GLShaderProgram init(GL2ES2 gl) {
        if (initDone) {
            throw new IllegalStateException("Already initialized");
        }

        ShaderProgram shaderProgram = new ShaderProgram();

        ShaderCode vertShaderCode = ShaderCode.create(
                gl, GL_VERTEX_SHADER, this.getClass(), srcRoot, null,
                vertBasename, "vert", null, true
        );

        shaderProgram.add(vertShaderCode);
        if (fragBasename != null) {
            ShaderCode fragShaderCode = ShaderCode.create(
                    gl, GL_FRAGMENT_SHADER, this.getClass(), srcRoot, null,
                    fragBasename, "frag", null, true
            );

            shaderProgram.add(fragShaderCode);
        }

        shaderProgram.init(gl);

        id = shaderProgram.program();

        //Set explicit locations:
        for (String name : attribLocations.keySet().toArray(new String[0])) {
            if (attribLocations.get(name) != null) {
                gl.glBindAttribLocation(id, attribLocations.get(name), name);
            }
        }
        
        shaderProgram.link(gl, System.out);

        // Get variables locations
        for (String name : uniformLocations.keySet().toArray(new String[0])) {
            uniformLocations.put(name, gl.glGetUniformLocation(id, name));
        }

        for (String name : attribLocations.keySet().toArray(new String[0])) {
            if (attribLocations.get(name) == null) {
                attribLocations.put(name, gl.glGetAttribLocation(id, name));
            }
        }

        initDone = true;

        return this;
    }

    public boolean isInitialized() {
        return initDone;
    }

    public int id() {
        return id;
    }

    public int getUniformLocation(String name) {
        if (!isInitialized()) {
            throw new IllegalStateException("Initialize the program first!");
        }

        Integer loc = uniformLocations.get(name);
        if (loc == null) {
            throw new IllegalArgumentException("Name of uniform " + name + " was not added before init");
        }

        return loc;
    }

    public int getAttribLocation(String name) {
        if (!isInitialized()) {
            throw new IllegalStateException("Initialize the program first!");
        }

        Integer loc = attribLocations.get(name);
        if (loc == null) {
            throw new IllegalArgumentException("Name of attribute " + name + " was not added before init");
        }

        return loc;
    }

    public void use(GL2ES2 gl) {
        if (!isInitialized()) {
            throw new IllegalStateException("Initialize the program first!");
        }

        gl.glUseProgram(id);
    }

    public void stopUsing(GL2ES2 gl) {
        gl.glUseProgram(0);
    }
}
