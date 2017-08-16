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
package org.gephi.visualization.opengl;

import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Class dedicated to the analysis and tuning of the engine for the detected
 * configuration (graphic card, cpu...)
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
    private final GLProfile profile = GLProfile.get(GLProfile.GL2);
    private final GLCapabilities caps = new GLCapabilities(profile);
    private final AbstractGraphicsDevice device = GLDrawableFactory.getFactory(profile).getDefaultDevice();

    public void checkGeneralCompatibility(GL2 gl) {
        if (messageDelivered) {
            return;
        }

        try {
            //Vendor
            vendor = gl.glGetString(GL2.GL_VENDOR);
            renderer = gl.glGetString(GL2.GL_RENDERER);
            versionStr = gl.glGetString(GL2.GL_VERSION);
            String currentConfig = String.format(NbBundle.getMessage(GraphicalConfiguration.class, "graphicalConfiguration_currentConfig"), vendor, renderer, versionStr);

            // Check version.
            if (!gl.isExtensionAvailable("GL_VERSION_1_2")) {
                String err = String.format(NbBundle.getMessage(GraphicalConfiguration.class, "graphicalConfiguration_exception"), versionStr, currentConfig);
                throw new GraphicalConfigurationException(err);
            }

            //VBO
            boolean vboExtension = gl.isExtensionAvailable("GL_ARB_vertex_buffer_object");
            boolean vboFunctions = gl.isFunctionAvailable("glGenBuffersARB")
                    && gl.isFunctionAvailable("glBindBufferARB")
                    && gl.isFunctionAvailable("glBufferDataARB")
                    && gl.isFunctionAvailable("glDeleteBuffersARB");
            vboSupport = vboExtension && vboFunctions;

            //Pbuffer
            pBufferSupport = GLDrawableFactory.getDesktopFactory().canCreateGLPbuffer(device, profile);

        } catch (final GraphicalConfigurationException e) {
            messageDelivered = true;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), e.getMessage(), "Configuration", JOptionPane.WARNING_MESSAGE);
                    Exceptions.printStackTrace(e);
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

    public boolean isIntelVendor() {
        return vendor.toLowerCase().contains("intel");
    }
}
