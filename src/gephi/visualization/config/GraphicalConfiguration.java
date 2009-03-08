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

package gephi.visualization.config;

import javax.media.opengl.GL;
import javax.swing.SwingUtilities;

/**
 * Class dedicated to the analysis and tuning of the engine for the detected configuration
 * (graphic card, cpu...)
 *
 * @author Mathieu Bastian
 */
public class GraphicalConfiguration {

	private static boolean messageDelivered=false;

	public void checkGeneralCompatibility(GL gl)
	{
		if(messageDelivered)
			return;

		try
		{
			//Vendor
			String vendor = gl.glGetString(GL.GL_VENDOR);
			String renderer = gl.glGetString(GL.GL_RENDERER);
			String versionStr = gl.glGetString(GL.GL_VERSION );
			String currentConfig = "\nConfiguration actuelle :\n"+vendor+"\n"+renderer+"\nVersion :"+versionStr;

			// Check version.

			if(!gl.isExtensionAvailable("GL_VERSION_1_2"))
			{
				throw new GraphicalConfigurationException(
						"La version d'OpenGL ("+versionStr+") n'est pas suffisante pour le rendu du graphe. " +
						"\nMettez à jour les pilotes ou la configuration." + currentConfig);
			}

			//VBO
			boolean vboExtension = gl.isExtensionAvailable("GL_ARB_vertex_buffer_object");
			boolean vboFunctions = gl.isFunctionAvailable("glGenBuffersARB") &&
			gl.isFunctionAvailable("glBindBufferARB") &&
			gl.isFunctionAvailable("glBufferDataARB") &&
			gl.isFunctionAvailable("glDeleteBuffersARB");
			boolean vboSupported = vboExtension && vboFunctions;

		} catch(final GraphicalConfigurationException exc)
		{
			messageDelivered=true;
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run() {
					//JOptionPane.showMessageDialog(MainFrame.getInstance(),exc.getMessage(),"Configuration graphique",JOptionPane.WARNING_MESSAGE);
                    exc.printStackTrace();
                }
			});

		}

	}

}
