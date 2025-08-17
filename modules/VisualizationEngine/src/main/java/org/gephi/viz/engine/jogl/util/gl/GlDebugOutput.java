package org.gephi.viz.engine.jogl.util.gl;

import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_LOW;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_NOTIFICATION;
import com.jogamp.opengl.GLDebugListener;
import com.jogamp.opengl.GLDebugMessage;

/**
 *
 * @author GBarbieri
 */
public class GlDebugOutput implements GLDebugListener {

    public int source;
    public int type;
    public int id;
    public int severity;
    public int length;
    public String message;
    public boolean received = false;

    public GlDebugOutput() {

    }

    public GlDebugOutput(final int source, final int type, final int severity) {
        this.source = source;
        this.type = type;
        this.severity = severity;
        this.message = null;
        this.id = -1;

    }

    public GlDebugOutput(final String message, final int id) {
        this.source = -1;
        this.type = -1;
        this.severity = -1;
        this.message = message;
        this.id = id;
    }

    @Override
    public void messageSent(GLDebugMessage event) {

        if (event.getDbgSeverity() == GL_DEBUG_SEVERITY_LOW || event.getDbgSeverity() == GL_DEBUG_SEVERITY_NOTIFICATION) {
            System.out.println("GlDebugOutput.messageSent(): " + event);
        } else {
            System.err.println("GlDebugOutput.messageSent(): " + event);
        }
        if (null != message && message.equals(event.getDbgMsg()) && id == event.getDbgId()) {
            received = true;
        } else if (0 <= source && source == event.getDbgSource()
                && type == event.getDbgType()
                && severity == event.getDbgSeverity()) {
            received = true;
        }
    }

}
