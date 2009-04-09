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
package org.gephi.visualization.opengl.compatibility.objects;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.opengl.gleem.linalg.Vec3f;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class Arrow3dObject extends Object3dImpl<Node> {

    private static float ARROW_WIDTH = 1.5f;
    private static float ARROW_HEIGHT = 4.5f;
    private Edge edge;
    private float[] cameraLocation;

    public Arrow3dObject() {
        super();
        cameraLocation = VizController.getInstance().getDrawable().getCameraLocation();
    }

    public Arrow3dObject(Edge edge) {
        this();
        this.edge = edge;
    }

    @Override
    public void display(GL gl, GLU glu) {
        Node nodeFrom = edge.getSource();
        Node nodeTo = edge.getTarget();

        //Edge vector
        Vec3f edgeVector = new Vec3f(nodeTo.x() - nodeFrom.x(), nodeTo.y() - nodeFrom.y(), nodeTo.z() - nodeFrom.z());
        edgeVector.normalize();

        //Get collision distance between nodeTo and arrow point
        double angle = Math.atan2(nodeTo.y() - nodeFrom.y(), nodeTo.x() - nodeFrom.x());
        float collisionDistance = ((Object3dImpl) nodeTo.getObject3d()).getCollisionDistance(angle);

        float x2 = nodeTo.x();
        float y2 = nodeTo.y();
        float z2 = nodeTo.z();

        //Point of the arrow
        float targetX = x2 - edgeVector.x() * collisionDistance;
        float targetY = y2 - edgeVector.y() * collisionDistance;
        float targetZ = z2 - edgeVector.z() * collisionDistance;

        //Base of the arrow
        float baseX = targetX - edgeVector.x() * ARROW_HEIGHT;
        float baseY = targetY - edgeVector.y() * ARROW_HEIGHT;
        float baseZ = targetZ - edgeVector.z() * ARROW_HEIGHT;

        //Camera vector
        Vec3f cameraVector = new Vec3f(targetX - cameraLocation[0], targetY - cameraLocation[1], targetZ - cameraLocation[2]);

        //Side vector
        Vec3f sideVector = edgeVector.cross(cameraVector);
        sideVector.normalize();

        //Draw the triangle
        gl.glColor4f(edge.r(), edge.g(), edge.b(), edge.alpha());
        gl.glVertex3d(baseX + sideVector.x() * ARROW_WIDTH, baseY + sideVector.y() * ARROW_WIDTH, baseZ + sideVector.z() * ARROW_WIDTH);
        gl.glVertex3d(baseX - sideVector.x() * ARROW_WIDTH, baseY - sideVector.y() * ARROW_WIDTH, baseZ - sideVector.z() * ARROW_WIDTH);
        gl.glVertex3d(targetX, targetY, targetZ);
    }

    @Override
    public float getCollisionDistance(double angle) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        return ((Object3dImpl) obj.getObject3d()).getOctants()[0] == leaf;
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
    }

    @Override
    public Octant[] getOctants() {
        return ((Object3dImpl) obj.getObject3d()).getOctants();
    }

    @Override
    public boolean isCacheMatching(int cacheMarker) {
        if (edge.getObject3d() != null) {
            return ((Object3dImpl) edge.getObject3d()).isCacheMatching(cacheMarker);
        }
        return false;
    }
}
