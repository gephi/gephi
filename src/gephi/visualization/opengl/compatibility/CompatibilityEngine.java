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

import gephi.visualization.VizController;
import gephi.visualization.initializer.NodeInitializer;
import gephi.visualization.objects.Object3dClass;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.opengl.compatibility.initializer.CompatibilityNodeInitializer;
import gephi.visualization.opengl.compatibility.initializer.CompatibilityNodeSphereInitializer;
import gephi.visualization.opengl.compatibility.initializer.CompatibilityObject3dInitializer;
import gephi.visualization.opengl.compatibility.nodeobjects.NodeSphereObject;
import gephi.visualization.opengl.octree.Octree;
import gephi.visualization.scheduler.Scheduler;
import gephi.visualization.selection.SelectionArea;
import gephi.visualization.swing.GraphDrawable;
import gephi.visualization.swing.GraphIO;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

/**
 *
 * @author Mathieu
 */
public class CompatibilityEngine extends AbstractEngine {

    private static final int CLASS_NODE = 0;
    private static final int CLASS_EDGE = 1;
    private static final int CLASS_ARROW = 2;
    public Octree octree;
    private CompatibilityScheduler scheduler;

    //User config
    protected CompatibilityObject3dClass[] object3dClasses;
    protected CompatibilityObject3dClass[] lodClasses;
    protected CompatibilityObject3dClass[] selectableClasses;

    public CompatibilityEngine() {
        super();

        //Init
        octree = new Octree(5, 1000, 3);
    }

    @Override
    public void initArchitecture()
    {
        super.initArchitecture();
        scheduler = (CompatibilityScheduler)VizController.getInstance().getScheduler();
        octree.initArchitecture();
        vizEventManager = VizController.getInstance().getVizEventManager();
    }

   public void updateSelection(GL gl,GLU glu)
   {
       octree.updateSelectedOctant(gl, glu, graphIO.getMousePosition(), currentSelectionArea.getSelectionAreaRectancle());
   }

    @Override
    public void beforeDisplay(GL gl, GLU glu) {
        
    }

    @Override
    public void display(GL gl, GLU glu) {

        for (Iterator<Object3d> itr = octree.getObjectIterator(CLASS_NODE); itr.hasNext();) {
            Object3d obj = itr.next();
            object3dClasses[CLASS_NODE].getCurrentObject3dInitializer().chooseModel(obj);
            setViewportPosition(obj);
        }
        long startTime = System.currentTimeMillis();
        if (object3dClasses[0].isEnabled()) {
            for (Iterator<Object3d> itr = octree.getObjectIterator(CLASS_NODE); itr.hasNext();) {
                Object3d obj = itr.next();
                if (obj.markTime != startTime) {
                    obj.display(gl, glu);
                    obj.markTime = startTime;
                }
            }
        }
        octree.displayOctree(gl);
    }

     @Override
    public void afterDisplay(GL gl, GLU glu) {
        
    }

    @Override
    public void cameraHasBeenMoved(GL gl, GLU glu) {
        
    }

    @Override
    public void initEngine(final GL gl, final GLU glu) {
        initDisplayLists(gl, glu);
        scheduler.cameraMoved.set(true);
        scheduler.mouseMoved.set(true);
    }

    @Override
    public void mouseClick() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseDrag() {
        float[] drag = graphIO.getMouseDrag();
		for(Object3d obj : selectedObjects)
		{
			float[] mouseDistance = obj.getDragDistanceFromMouse();
			obj.getObj().setX(drag[0]+mouseDistance[0]);
			obj.getObj().setY(drag[1]+mouseDistance[1]);
		}
    }

    private ConcurrentLinkedQueue<Object3d> selectedObjects = new ConcurrentLinkedQueue<Object3d>();

    @Override
    public void mouseMove() {

        List<Object3d> newSelectedObjects   =null;
        List<Object3d> unSelectedObjects    =null;

        if(vizEventManager.hasSelectionListeners())
        {
            newSelectedObjects = new ArrayList<Object3d>();
            unSelectedObjects = new ArrayList<Object3d>();
        }

        long markTime = System.currentTimeMillis();
        for (Object3dClass objClass : selectableClasses) {
            for (Iterator<Object3d> itr = octree.getSelectedObjectIterator(objClass.getClassId()); itr.hasNext();) {
                Object3d obj = itr.next();
                if (isUnderMouse(obj) && currentSelectionArea.select(obj.getObj())) {
                    if (!obj.isSelected()) {
                        //New selected
                        obj.setSelected(true);
                         if(vizEventManager.hasSelectionListeners())
                            newSelectedObjects.add(obj);
                        selectedObjects.add(obj);
                    }
                    obj.markTime = markTime;
                } else if (currentSelectionArea.unselect(obj.getObj())) {
                    if (vizEventManager.hasSelectionListeners() && obj.isSelected()) {
                        unSelectedObjects.add(obj);
                    }
                }
            }
        }

        for (Iterator<Object3d> itr = selectedObjects.iterator(); itr.hasNext();) {
            Object3d o = itr.next();
            if (o.markTime != markTime) {
                itr.remove();
                o.setSelected(false);
            }
        }
    }

    @Override
    public void refreshGraphLimits() {
       
    }

    @Override
    public void startDrag() {
        float x = graphIO.getMouseDrag()[0];
		float y = graphIO.getMouseDrag()[1];

		for (Iterator<Object3d> itr = selectedObjects.iterator(); itr.hasNext();)
		{
			Object3d o = itr.next();
			float[] tab = o.getDragDistanceFromMouse();
			tab[0] = o.getObj().getX() - x;
			tab[1] = o.getObj().getY() -y;
		}
    }

    @Override
    public void stopDrag() {
        
    }

    private void initDisplayLists(GL gl, GLU glu) {
        //Constants
        float blancCasse[] = {(float) 213 / 255, (float) 208 / 255, (float) 188 / 255, 1.0f};
        float noirCasse[] = {(float) 39 / 255, (float) 25 / 255, (float) 99 / 255, 1.0f};
        float noir[] = {(float) 0 / 255, (float) 0 / 255, (float) 0 / 255, 0.0f};
        float[] shine_low = {10.0f, 0.0f, 0.0f, 0.0f};
        FloatBuffer ambient_metal = FloatBuffer.wrap(noir);
        FloatBuffer diffuse_metal = FloatBuffer.wrap(noirCasse);
        FloatBuffer specular_metal = FloatBuffer.wrap(blancCasse);
        FloatBuffer shininess_metal = FloatBuffer.wrap(shine_low);

        //End

        //Quadric for all the glu models
        GLUquadric quadric = glu.gluNewQuadric();
        int ptr = gl.glGenLists(4);

        // Metal material display list
        int MATTER_METAL = ptr;
        gl.glNewList(MATTER_METAL, GL.GL_COMPILE);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambient_metal);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuse_metal);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specular_metal);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess_metal);
        gl.glEndList();
        //Fin

        for (CompatibilityObject3dInitializer cis : object3dClasses[CLASS_NODE].getObject3dInitializers()) {
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

    public void initObject3dClass() {
        object3dClasses = objectClassLibrary.createObjectClassesCompatibility(this);
        lodClasses = new CompatibilityObject3dClass[0];
        selectableClasses = new CompatibilityObject3dClass[0];

        object3dClasses[0].setEnabled(true);

        //LOD
        ArrayList<Object3dClass> classList = new ArrayList<Object3dClass>();
        for (Object3dClass objClass : object3dClasses) {
            if (objClass.isLod()) {
                classList.add(objClass);
            }
        }
        lodClasses = classList.toArray(lodClasses);

        //Selectable
        classList.clear();
        for (Object3dClass objClass : object3dClasses) {
            if (objClass.isSelectable()) {
                classList.add(objClass);
            }
        }
        selectableClasses = classList.toArray(selectableClasses);
    }

    public CompatibilityObject3dClass[] getObject3dClasses() {
        return object3dClasses;
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }
}
