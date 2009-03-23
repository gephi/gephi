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

package gephi.visualization.opengl;

import gephi.visualization.objects.Object3dClassLibrary;
import gephi.visualization.objects.Object3dClass;
import gephi.visualization.objects.StandardObject3dClassLibrary;
import java.awt.Component;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import gephi.visualization.initializer.NodeInitializer;
import gephi.visualization.Renderable;
import gephi.visualization.VizArchitecture;
import gephi.visualization.VizController;
import gephi.visualization.bridge.DataBridge;
import gephi.visualization.events.VizEventManager;
import gephi.visualization.opengl.gleem.linalg.Vec3d;
import gephi.visualization.opengl.gleem.linalg.Vec3f;
import gephi.visualization.scheduler.Scheduler;
import gephi.visualization.selection.Point;
import gephi.visualization.selection.SelectionArea;
import gephi.visualization.swing.GraphDrawable;
import gephi.visualization.swing.GraphIO;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.undo.UndoableEditSupport;

/**
 * Abstract graphic engine. Real graphic engines inherit from this class and can use the common functionalities.
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractEngine implements VizArchitecture {

    //Enums
    public enum Limits {MIN_X, MAX_X, MIN_Y, MAX_Y, MIN_Z, MAX_Z};
	public static final int CLASS_NODE = 0;
    public static final int CLASS_EDGE = 1;
    public static final int CLASS_ARROW = 2;

	//Architecture
	protected GraphDrawable graphDrawable;
    protected GraphIO graphIO;
    protected VizEventManager vizEventManager;
    protected SelectionArea currentSelectionArea = new Point();
    protected Object3dClassLibrary objectClassLibrary;
    protected DataBridge dataBridge;

	//AddRemove

    public void initArchitecture()
    {
        this.graphDrawable = VizController.getInstance().getDrawable();
		this.graphIO = VizController.getInstance().getGraphIO();
        this.objectClassLibrary = VizController.getInstance().getObject3dClassLibrary();
        this.dataBridge = VizController.getInstance().getDataBridge();
        initObject3dClass();
    }

	public abstract void beforeDisplay(GL gl, GLU glu);
	public abstract void display(GL gl, GLU glu);
	public abstract void afterDisplay(GL gl, GLU glu);

	public abstract void initEngine(GL gl, GLU glu);

	public abstract void cameraHasBeenMoved(GL gl, GLU glu);
	public abstract void mouseMove();
	public abstract void mouseDrag();
	public abstract void startDrag();
	public abstract void stopDrag();
	public abstract void mouseClick();
    public abstract Scheduler getScheduler();

    public abstract void addObject(int classID, Object3d obj);
    public abstract void removeObject(int classID, Object3d obj);
    public abstract void updateObjectsPosition();
    public abstract boolean updateWorld();
	public abstract void refreshGraphLimits();
    public abstract void initObject3dClass();
    public abstract Object3dClass[] getObject3dClasses();
    

	public float cameraDistance(Object3d object)
	{
        float[] cameraLocation = graphDrawable.getCameraLocation();
		double distance = Math.sqrt(Math.pow((double)object.obj.getX() - cameraLocation[0],2d) +
				Math.pow((double)object.obj.getY() - cameraLocation[1],2d) +
				Math.pow((double)object.obj.getZ() - cameraLocation[2],2d));
		object.setViewportZ((float)distance);

		return (float)distance - object.obj.getRadius();
	}

	protected void setViewportPosition(Object3d object)
	{
		double[] res = graphDrawable.myGluProject(object.obj.getX(), object.obj.getY(), object.obj.getZ());
		object.setViewportX((float)res[0]);
		object.setViewportY((float)res[1]);

		res = graphDrawable.myGluProject(object.obj.getX()+object.obj.getRadius(), object.obj.getY(),object.obj.getZ());
		float rad = Math.abs((float)res[0] - object.getViewportX() );
		object.setViewportRadius(rad);
	}

	protected boolean isUnderMouse(Object3d obj)
	{
		double x1 = graphIO.getMousePosition()[0];
		double y1 = graphIO.getMousePosition()[1];

		double x2 = obj.getViewportX();
		double y2 = obj.getViewportY();

		double xDist = Math.abs(x2-x1);
		double yDist = Math.abs(y2-y1);

		double distance = Math.sqrt(xDist*xDist + yDist*yDist);
		Vec3f d = new Vec3f((float)xDist, (float)yDist, (float)distance);

		return currentSelectionArea.mouseTest(d, obj);
    }


    public IntBuffer getViewportBuffer()
	{
		return graphDrawable.viewport;
	}

	public DoubleBuffer getProjectionMatrix()
	{
		return graphDrawable.projMatrix;
	}
}
