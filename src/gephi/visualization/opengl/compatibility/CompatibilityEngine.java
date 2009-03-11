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

package gephi.visualization.opengl.compatibility;

import gephi.visualization.NodeInitializer;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.opengl.compatibility.nodeinit.CompatibilityNodeInitializer;
import gephi.visualization.opengl.compatibility.nodeinit.CompatibilityNodeSphereInitializer;
import gephi.visualization.opengl.compatibility.nodeobjects.NodeSphereObject;
import gephi.visualization.opengl.octree.Octree;
import gephi.visualization.swing.GraphDrawable;
import gephi.visualization.swing.GraphIO;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

/**
 *
 * @author Mathieu
 */
public class CompatibilityEngine extends AbstractEngine
{
    protected boolean listEnable_0=true;
	protected boolean listEnable_1=true;
	protected boolean listEnable_2=true;

    public Octree octree;
    protected CompatibilityNodeInitializer currentNodeInitializers;
    protected List<CompatibilityNodeInitializer> nodeInitializers;

    public CompatibilityEngine(GraphDrawable graphDrawable, GraphIO graphIO)
    {
        super(graphDrawable,graphIO);

        //Init
        octree = new Octree(graphDrawable,5,1000,3);
        nodeInitializers = new ArrayList<CompatibilityNodeInitializer>();
        nodeInitializers.add(new CompatibilityNodeSphereInitializer(this));

        currentNodeInitializers = nodeInitializers.get(0);
    }

    @Override
    public void afterDisplay(GL gl, GLU glu) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beforeDisplay(GL gl, GLU glu) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cameraHasBeenMoved() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void display(GL gl, GLU glu) {
       octree.updateVisibleOctant(gl);
       for(Iterator<Object3d> itr = octree.getObjectIterator(Octree.CLASS_0);itr.hasNext();)
        {
            Object3d  obj = itr.next();
			currentNodeInitializers.chooseModel(obj);
		}
       long startTime = System.currentTimeMillis();
       if(listEnable_0)
		{
			for(Iterator<Object3d> itr = octree.getObjectIterator(Octree.CLASS_0);itr.hasNext();)
			{
				Object3d obj = itr.next();
				if(obj.markTime != startTime)
				{   
					obj.display(gl, glu);
					obj.markTime = startTime;
				}
			}
        }
       octree.displayOctree(gl);
    }

   
    @Override
    public void initEngine(final GL gl, final GLU glu) {
        initDisplayLists(gl, glu);
        
    }

    @Override
    public void mouseClick() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseDrag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseMove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refreshGraphLimits() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startDrag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopDrag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initDisplayLists(GL gl, GLU glu)
	{
		//Constants
		float blancCasse[]	= {(float) 213/255, (float) 208/255, (float) 188/255, 1.0f};
		float noirCasse[]	= {(float)  39/255, (float)  25/255, (float)  99/255, 1.0f};
		float noir[]		= {(float)   0/255, (float)   0/255, (float)   0/255, 0.0f};
		float[] shine_low	= {10.0f, 0.0f, 0.0f, 0.0f};
		FloatBuffer ambient_metal  = FloatBuffer.wrap(noir);
		FloatBuffer diffuse_metal  = FloatBuffer.wrap(noirCasse);
		FloatBuffer specular_metal = FloatBuffer.wrap(blancCasse);
		FloatBuffer shininess_metal = FloatBuffer.wrap(shine_low);

		//End

		//Quadric for all the glu models
		GLUquadric quadric = glu.gluNewQuadric();
		int ptr = gl.glGenLists(4);

		// Metal material display list
		int MATTER_METAL = ptr;
		gl.glNewList(MATTER_METAL,GL.GL_COMPILE);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT,   ambient_metal);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE,   diffuse_metal);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR,  specular_metal);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess_metal);
		gl.glEndList();
		//Fin

		for(CompatibilityNodeInitializer cis : nodeInitializers)
		{
			int newPtr = cis.initDisplayLists(gl, glu, quadric, ptr);
			ptr = newPtr;
		}


		//Fin

		// Sphere with a texture
		//SHAPE_BILLBOARD = SHAPE_SPHERE32 + 1;
		/*gl.glNewList(SHAPE_BILLBOARD,GL.GL_COMPILE);
		    textures[0].bind();
		    gl.glBegin(GL.GL_TRIANGLE_STRIP);
	    	// Map the texture and create the vertices for the particle.
	    	gl.glTexCoord2d(1, 1);
	    	gl.glVertex3f(0.5f, 0.5f, 0);
	    	gl.glTexCoord2d(0, 1);
	    	gl.glVertex3f(-0.5f, 0.5f,0);
	    	gl.glTexCoord2d(1, 0);
	    	gl.glVertex3f(0.5f, -0.5f, 0);
	    	gl.glTexCoord2d(0, 0);
	    	gl.glVertex3f(-0.5f,-0.5f, 0);
	    	gl.glEnd();

	    	gl.glBindTexture(GL.GL_TEXTURE_2D,0);
	    gl.glEndList();*/
		//Fin

		glu.gluDeleteQuadric(quadric);
	}

     @Override
    public NodeInitializer getCurrentNodeInitializer() {
        return currentNodeInitializers;
    }

    @Override
    public List<? extends NodeInitializer> getNodeInitializers() {
       return nodeInitializers;
    }
}
