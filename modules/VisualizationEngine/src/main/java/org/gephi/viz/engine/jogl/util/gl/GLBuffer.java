package org.gephi.viz.engine.jogl.util.gl;

import com.jogamp.opengl.GL;
import java.nio.Buffer;

/**
 *
 * @author Eduardo Ramos
 */
public interface GLBuffer {

    void init(GL gl, long sizeBytes, int usageFlags);

    void init(GL gl, Buffer buffer, int usageFlags);

    void update(GL gl, Buffer buffer);

    void update(GL gl, Buffer buffer, long sizeBytes);

    void update(GL gl, Buffer buffer, long offsetBytes, long sizeBytes);
    
    void updateWithOrphaning(GL gl, Buffer buffer);
    
    void updateWithOrphaning(GL gl, Buffer buffer, long sizeBytes);

    void bind(GL gl);

    void destroy(GL gl);

    int getId();

    long getSizeBytes();

    int getType();

    int getUsageFlags();

    boolean isBound(GL gl);

    boolean isInitialized();

    long size();

    void unbind(GL gl);

    boolean isMutable();
}
