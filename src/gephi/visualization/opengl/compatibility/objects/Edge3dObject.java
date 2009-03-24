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

package gephi.visualization.opengl.compatibility.objects;

import com.sun.opengl.util.BufferUtil;
import gephi.data.network.Edge;
import gephi.data.network.Node;
import gephi.visualization.VizController;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.opengl.gleem.linalg.Vec3d;
import gephi.visualization.opengl.gleem.linalg.Vec3f;
import gephi.visualization.opengl.octree.Octant;
import gephi.visualization.swing.GraphDrawable;
import java.nio.FloatBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 *
 * @author Mathieu
 */
public class Edge3dObject extends Object3d<Edge>
{
    private static float CARDINAL_DIV=10f;  //Set the size of edges according to cardinal

	//Un arc est ajouté dans l'octant de son noeud source et destination. Il n'est donc pas affiché
	//lorsque aucun des deux octant n'est affiché.
    private float[] cameraLocation;

    public Edge3dObject()
    {
        cameraLocation = VizController.getInstance().getDrawable().getCameraLocation();
        octants = new Octant[2];
    }

	@Override
	public int[] octreePosition(float centerX, float centerY, float centerZ,  float size)
	{
		Node nodeFrom = obj.getSource();
		Node nodeTo = obj.getTarget();

		int index1=-1, index2=-1;

		size/=2;

		//True if the point is in the big cube.
		if(!(Math.abs(nodeFrom.getX() - centerX) > size || Math.abs(nodeFrom.getY() - centerY) > size || Math.abs(nodeFrom.getZ() -centerZ) > size))
		{
			index1 = 0;

			//Point1
			if(nodeFrom.getY() < centerY)
				index1+=4;
			if(nodeFrom.getZ() > centerZ)
				index1+=2;
			if(nodeFrom.getX() < centerX)
				index1+=1;
		}

		if(!(Math.abs(nodeTo.getX() - centerX) > size || Math.abs(nodeTo.getY() - centerY) > size || Math.abs(nodeTo.getZ() -centerZ) > size))
		{
			index2 = 0;

			//Point2
			if(nodeTo.getY() < centerY)
				index2+=4;
			if(nodeTo.getZ() > centerZ)
				index2+=2;
			if(nodeTo.getX() < centerX)
				index2+=1;
		}

		if(index1 >= 0 && index2 >= 0)
		{
			if(index1!=index2)
				return new int[] {index1, index2 };
			else
				return new int[] { index1 };
		}
		else if(index1 >= 0)
		{
			return new int[] { index1 };
		}
		else if(index2 >= 0)
		{
			return new int[] { index2 };
		}
		else
		{
			return new int[] {};
		}
	}


	@Override
	public boolean isInOctreeLeaf(Octant leaf)
	{
		Node nodeFrom = obj.getSource();
		Node nodeTo = obj.getTarget();

        if(octants[0] == leaf)
        {
            if(octants[0]!=nodeFrom.getObject3d().getOctants()[0])      //0 = nodeFrom
                return false;
        }
        else
        {
             if(octants[1]!=nodeTo.getObject3d().getOctants()[0])      //1 = nodeTo
                return false;
        }

        return true;
	}


	@Override
	public void display(GL gl, GLU glu)
	{
		//gl.glColor3f(object.getNodeFrom().r, object.getNodeFrom().g, object.getNodeFrom().b);
		//gl.glVertex3f(obj.getSource().getX(), obj.getSource().getY(), obj.getSource().getZ());
		//gl.glVertex3f(obj.getTarget().getX(), obj.getTarget().getY(), obj.getTarget().getZ());

        float x1 = obj.getSource().getX();
        float x2 = obj.getTarget().getX();
        float y1 = obj.getSource().getY();
        float y2 = obj.getTarget().getY();
        float z1 = obj.getSource().getZ();
        float z2 = obj.getTarget().getZ();
        float t1=obj.getSource().getSize()/CARDINAL_DIV;
        float t2=obj.getSource().getSize()/CARDINAL_DIV;

        //CameraVector, from camera location to any point on the line
        float cameraVectorX = x1 - cameraLocation[0];
        float cameraVectorY = y1 - cameraLocation[1];
        float cameraVectorZ = z1 - cameraLocation[2];
        /*float cameraVectorX = cameraVector.x();
        float cameraVectorY = cameraVector.y();
        float cameraVectorZ = cameraVector.z();*/

        //This code has been replaced by followinf more efficient
        //Vec3f edgeVector = new Vec3f(x2 - x1,y2 - y1,z2 - z1);
        //Vec3f cameraVector = new Vec3f(drawable.getCameraLocation()[0] - (x2 - x1)/2f,drawable.getCameraLocation()[1] - (y2 - y1)/2f,drawable.getCameraLocation()[2] - (z2 - z1)/2f);
        //Vec3f sideVector = edgeVector.cross(cameraVector);
        //sideVector.normalize();

        //Vector line
        float edgeVectorX = x2 - x1;
        float edgeVectorY = y2 - y1;
        float edgeVectorZ = z2 - z1;

        //Cross product
        float sideVectorX = edgeVectorY*cameraVectorZ - edgeVectorZ*cameraVectorY;
        float sideVectorY = edgeVectorZ*cameraVectorX - edgeVectorX*cameraVectorZ;
        float sideVectorZ = edgeVectorX*cameraVectorY - edgeVectorY*cameraVectorX;

        //Normalize
        float norm = (float)Math.sqrt(sideVectorX * sideVectorX + sideVectorY * sideVectorY + sideVectorZ * sideVectorZ);
        if (norm > 0f) // Avoid divizion by zero if cameraVector & sideVector colinear
        {
            sideVectorX /= norm;
            sideVectorY /= norm;
            sideVectorZ /= norm;
        }
        else
        {
            sideVectorX = 0f;
            sideVectorY = 0f;
            sideVectorZ = 0f;
        }

        float x1Thick=sideVectorX/2f*t1;
        float x2Thick=sideVectorX/2f*t2;
        float y1Thick=sideVectorY/2f*t1;
        float y2Thick=sideVectorY/2f*t2;
        float z1Thick=sideVectorZ/2f*t1;
        float z2Thick=sideVectorZ/2f*t2;

        gl.glColor4f(obj.r, obj.g, obj.b, obj.a);

		gl.glVertex3f(x1+x1Thick, y1+y1Thick, z1+z1Thick);
		gl.glVertex3f(x1-x1Thick, y1-y1Thick, z1-z1Thick);
		gl.glVertex3f(x2-x2Thick, y2-y2Thick, z2-z2Thick);
        gl.glVertex3f(x2-x2Thick, y2-y2Thick, z2-z2Thick);
		gl.glVertex3f(x2+x2Thick, y2+y2Thick, z2+z2Thick);
        gl.glVertex3f(x1+x1Thick, y1+y1Thick, z1+z1Thick);

        /*double angle = Math.atan2(y2 - y1, x2 - x1);
        double sin = 2*Math.sin(angle);
        double cos = 2*Math.cos(angle);
        float t2sina1 =(float)( t1 / sin);
        float t2cosa1 = (float)( t1 / cos);
        float t2sina2 = (float)( t2 / sin);
        float t2cosa2 = (float)( t2 / cos);


        gl.glVertex3f(x1 + t2sina1, y1 - t2cosa1,z1);
        gl.glVertex3f(x2 + t2sina2, y2 - t2cosa2,z2);
        gl.glVertex3f(x2 - t2sina2, y2 + t2cosa2,z2);
        gl.glVertex3f(x2 - t2sina2, y2 + t2cosa2,z2);
        gl.glVertex3f(x1 - t2sina1, y1 + t2cosa1,z1);
        gl.glVertex3f(x1 + t2sina1, y1 - t2cosa1,z1);
        

        /*float x1 = obj.getSource().getX();
        float x2 = obj.getTarget().getX();
        float y1 = obj.getSource().getY();
        float y2 = obj.getTarget().getY();
        float z1 = 0;
        float z2 = 0;
//        float t=0.1f;

        float distance = (float)Math.sqrt(Math.pow((double)x1 - x2,2d) +
				Math.pow((double)y1 - y2,2d) +
				Math.pow((double)z1 - z2,2d));
        double anglez = Math.atan2(y2 - y1, x2 - x1);

        gl.glPushMatrix();

        gl.glTranslatef(x1+(float)Math.cos(anglez)*distance/2f, y1+(float)Math.sin(anglez)*distance/2f, 1);
        gl.glRotatef((float)Math.toDegrees(anglez), 0, 0, 1);
        gl.glScalef(distance/2f, t, 0f);
        
        gl.glBegin(GL.GL_TRIANGLE_STRIP);
        gl.glVertex3f(1, 1, 0);
        gl.glVertex3f(-1, 1,0);
        gl.glVertex3f(1, -1, 0);
        gl.glVertex3f(-1,-1, 0);
        gl.glEnd();

        gl.glPopMatrix();*/
	}


	@Override
	public boolean selectionTest(Vec3f distanceFromMouse, float selectionSize) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getCollisionDistance(double angle) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toSVG() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOctant(Octant octant) {
		if(obj.getSource().getObject3d().getOctants()[0]==octant)
        {
            octants[0]=octant;
        }
        else
        {
            octants[1] = octant;
        }
	}

    @Override
    public void resetOctant(Octant octant)
    {
        /*if(octants[0]==octant)
        {
            octants[0] = null;
        }
        if(octants[1]==octant)
        {
            octants[1] = null;
        }*/
        octants[0] = null;
        octants[1] = null;
    }
}
