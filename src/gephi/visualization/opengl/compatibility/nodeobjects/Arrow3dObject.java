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

package gephi.visualization.opengl.compatibility.nodeobjects;

import gephi.data.network.Edge;
import gephi.data.network.Node;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.opengl.gleem.linalg.Vec3d;
import gephi.visualization.opengl.gleem.linalg.Vec3f;
import gephi.visualization.opengl.octree.Octant;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 *
 * @author Mathieu
 */
public class Arrow3dObject extends Object3d<Node> {

	private Edge edge;

    public Arrow3dObject()
    {
        super();
    }

	public Arrow3dObject(Edge edge)
	{
        super();
		this.edge = edge;
	}

    @Override
    public void display(GL gl, GLU glu) {
		Node nodeFrom = edge.getSource();
		Node nodeTo = edge.getTarget();

		Vec3d edgeVector = new Vec3d(nodeTo.x - nodeFrom.x,nodeTo.y - nodeFrom.y,nodeTo.z - nodeFrom.z);
		Vec3d p2 = new Vec3d(0,0,1);
		edgeVector.normalize();
		Vec3d normale = edgeVector.cross(p2);

		float arrowWidth=0.15f;
		float arrowHeight=0.45f;

		double angle = Math.atan2(nodeTo.y-nodeFrom.y,nodeTo.x-nodeFrom.x);
		float collisionDistance = nodeTo.getObject3d().getCollisionDistance(angle);
		double collisionDistanceX = collisionDistance*Math.cos(angle);
		double collisionDistanceY = collisionDistance*Math.sin(angle);

		Vec3d point1 = new Vec3d(
				nodeTo.x-(edgeVector.x()*arrowHeight)+(normale.x()*arrowWidth)-collisionDistanceX,
				nodeTo.y-(edgeVector.y()*arrowHeight)+(normale.y()*arrowWidth)-collisionDistanceY,
				nodeTo.z-(edgeVector.z()*arrowHeight)+(normale.z()*arrowWidth));
		Vec3d point2 = new Vec3d(
				nodeTo.x-(edgeVector.x()*arrowHeight)+(normale.x()*-arrowWidth)-collisionDistanceX,
				nodeTo.y-(edgeVector.y()*arrowHeight)+(normale.y()*-arrowWidth)-collisionDistanceY,
				nodeTo.z-(edgeVector.z()*arrowHeight)+(normale.z()*-arrowWidth));

		//gl.glColor3f(nodeFrom.r, nodeFrom.g, nodeFrom.b);
		gl.glVertex3d(point1.x(), point1.y(), point1.z());
		gl.glVertex3d(point2.x(), point2.y(), point2.z());
		gl.glVertex3d(nodeTo.x-collisionDistanceX, nodeTo.y-collisionDistanceY, nodeTo.z);
	}

	@Override
	public float getCollisionDistance(double angle) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isInOctreeLeaf(Octant leaf)
	{
		return obj.getObject3d().getOctant()==octant;
	}

	@Override
	public int[] octreePosition(float centerX, float centerY, float centerZ,
			float size) {
		return null;
	}

	@Override
	public boolean selectionTest(Vec3f distanceFromMouse, float selectionSize) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toSVG() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOctant(Octant octant) {
		// TODO Auto-generated method stub

	}

	@Override
	public Octant getOctant() {
		// TODO Auto-generated method stub
		return null;
	}

}
