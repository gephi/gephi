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
package org.gephi.visualization.config;

import javax.media.opengl.GL;
import javax.media.opengl.GLDrawableFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Class dedicated to the analysis and tuning of the engine for the detected configuration
 * (graphic card, cpu...)
 *
 * @author Mathieu Bastian
 */
public class GraphicalConfiguration {

    private static boolean messageDelivered = false;
    private boolean vboSupport = false;
    private boolean pBufferSupport = false;
    private String vendor = "";
    private String renderer = "";
    private String versionStr = "";

    public void checkGeneralCompatibility(GL gl) {
        if (messageDelivered) {
            return;
        }

        try {
            //Vendor
            vendor = gl.glGetString(GL.GL_VENDOR);
            renderer = gl.glGetString(GL.GL_RENDERER);
            versionStr = gl.glGetString(GL.GL_VERSION);
            String currentConfig = String.format(NbBundle.getMessage(GraphicalConfiguration.class, "graphicalConfiguration_currentConfig"), vendor, renderer, versionStr);

            // Check version.
            if (!gl.isExtensionAvailable("GL_VERSION_1_2")) {
                String err = String.format(NbBundle.getMessage(GraphicalConfiguration.class, "graphicalConfiguration_exception"), versionStr, currentConfig);
                throw new GraphicalConfigurationException(err);
            }

            //VBO
            boolean vboExtension = gl.isExtensionAvailable("GL_ARB_vertex_buffer_object");
            boolean vboFunctions = gl.isFunctionAvailable("glGenBuffersARB") &&
                    gl.isFunctionAvailable("glBindBufferARB") &&
                    gl.isFunctionAvailable("glBufferDataARB") &&
                    gl.isFunctionAvailable("glDeleteBuffersARB");
            vboSupport = vboExtension && vboFunctions;

            //Pbuffer
            pBufferSupport = GLDrawableFactory.getFactory().canCreateGLPbuffer();

        } catch (final GraphicalConfigurationException exc) {
            messageDelivered = true;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), exc.getMessage(), "Configuration", JOptionPane.WARNING_MESSAGE);
                    exc.printStackTrace();
                }
            });
        }
    }

    public String getVendor() {
        return vendor;
    }

    public String getRenderer() {
        return renderer;
    }

    public String getVersionStr() {
        return versionStr;
    }

    public boolean isPBufferSupported() {
        return pBufferSupport;
    }

    public boolean isVboSupported() {
        return vboSupport;
    }
}
