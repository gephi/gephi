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
package org.gephi.visualization.opengl.compatibility;

import com.sun.opengl.util.BufferUtil;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.objects.Object3dClass;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.api.initializer.CompatibilityObject3dInitializer;
import org.gephi.visualization.opengl.octree.Octree;
import org.gephi.visualization.api.Scheduler;
import org.gephi.visualization.api.VizConfig.DisplayConfig;
import org.gephi.visualization.api.objects.CompatibilityObject3dClass;
import org.gephi.visualization.opengl.compatibility.objects.Potato3dObject;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityEngine extends AbstractEngine {

    Octree octree;
    private CompatibilityScheduler scheduler;

    //User config
    protected CompatibilityObject3dClass[] object3dClasses;
    protected CompatibilityObject3dClass[] lodClasses;
    protected CompatibilityObject3dClass[] selectableClasses;
    protected CompatibilityObject3dClass[] clickableClasses;

    //Selection
    private ConcurrentLinkedQueue<Object3dImpl>[] selectedObjects;

    //State
    private boolean inited=false;

    public CompatibilityEngine() {
        super();
    }

    @Override
    public void initArchitecture() {
        super.initArchitecture();
        scheduler = (CompatibilityScheduler) VizController.getInstance().getScheduler();
        vizEventManager = VizController.getInstance().getVizEventManager();

        //Init
        octree = new Octree(vizConfig.getOctreeDepth(), vizConfig.getOctreeWidth(), object3dClasses.length);
        octree.initArchitecture();
    }

    public void updateSelection(GL gl, GLU glu) {
        octree.updateSelectedOctant(gl, glu, graphIO.getMousePosition(), currentSelectionArea.getSelectionAreaRectancle());

        //Potatoes selection
        if (object3dClasses[CLASS_POTATO].isEnabled()) {

            int potatoCount = octree.countSelectedObjects(CLASS_POTATO);
            float[] mousePosition = graphIO.getMousePosition();
            float[] pickRectangle = currentSelectionArea.getSelectionAreaRectancle();

            //Update selection
            int capacity = 1 * 4 * potatoCount;      //Each object take in maximium : 4 * name stack depth
            IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);

            gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
            gl.glRenderMode(GL.GL_SELECT);

            gl.glInitNames();
            gl.glPushName(0);

            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            glu.gluPickMatrix(mousePosition[0], mousePosition[1], pickRectangle[0], pickRectangle[1], graphDrawable.getViewport());
            gl.glMultMatrixd(graphDrawable.getProjectionMatrix());

            gl.glMatrixMode(GL.GL_MODELVIEW);

            //Draw the nodes' cube int the select buffer
            int hitName = 1;
            Object3dImpl[] array = new Object3dImpl[potatoCount];
            for (Iterator<Object3dImpl> itr = octree.getSelectedObjectIterator(CLASS_POTATO); itr.hasNext();) {
                Potato3dObject obj = (Potato3dObject)itr.next();
                obj.setUnderMouse(false);
                if(obj.isDisplayReady())
                {
                    array[hitName - 1] = obj;
                    gl.glLoadName(hitName);
                    obj.mark = false;
                    gl.glBegin(GL.GL_TRIANGLES);
                    obj.display(gl, glu);
                    gl.glEnd();
                    obj.mark = true;
                    obj.display(gl, glu);
                    obj.mark = false;
                    hitName++;
                }
            }

            //Restoring the original projection matrix
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glFlush();

            //Returning to normal rendering mode
            int nbRecords = gl.glRenderMode(GL.GL_RENDER);

            //Get the hits and put the node under selection in the selectionArray
            for (int i = 0; i < nbRecords; i++) {
                int hit = hitsBuffer.get(i * 4 + 3) - 1; 		//-1 Because of the glPushName(0)
                Potato3dObject obj = (Potato3dObject)array[hit];
                if(!obj.isParentUnderMouse())
                    obj.setUnderMouse(true);
            }
        }
    }

    @Override
    public boolean updateWorld() {
        if (inited && dataBridge.requireUpdate()) {
            dataBridge.updateWorld();
            return true;
        }
        return false;
    }

    @Override
    public void worldUpdated(int cacheMarker) {
        octree.setCacheMarker(cacheMarker);
        for (Object3dClass objClass : object3dClasses) {
            if (objClass.getCacheMarker() == cacheMarker) {
                octree.cleanDeletedObjects(objClass.getClassId());
            }
        }
    }

    @Override
    public void beforeDisplay(GL gl, GLU glu) {
    }

    @Override
    public void display(GL gl, GLU glu) {

        for (Iterator<Object3dImpl> itr = octree.getObjectIterator(CLASS_NODE); itr.hasNext();) {
            Object3dImpl obj = itr.next();
            object3dClasses[CLASS_NODE].getCurrentObject3dInitializer().chooseModel(obj);
            setViewportPosition(obj);
        }

        long startTime = System.currentTimeMillis();

        if (object3dClasses[CLASS_EDGE].isEnabled()) {
            gl.glDisable(GL.GL_LIGHTING);
            //gl.glLineWidth(obj.getObj().getSize());
            //gl.glDisable(GL.GL_BLEND);
            //gl.glBegin(GL.GL_LINES);
            //gl.glBegin(GL.GL_QUADS);
            gl.glBegin(GL.GL_TRIANGLES);

            if (vizConfig.getDisplayConfig() == DisplayConfig.DISPLAY_ALL) {
                //Normal mode, all edges rendered
                for (Iterator<Object3dImpl> itr = octree.getObjectIterator(CLASS_EDGE); itr.hasNext();) {
                    Object3dImpl obj = itr.next();
                    //Renderable renderable = obj.getObj();

                    if (obj.markTime != startTime) {
                        obj.display(gl, glu);
                        obj.markTime = startTime;
                    }

                }
            } else if (vizConfig.getDisplayConfig() == DisplayConfig.DISPLAY_NODES_EDGES) {
                //Only edges on selected nodes are rendered
                for (Iterator<Object3dImpl> itr = octree.getSelectedObjectIterator(CLASS_EDGE); itr.hasNext();) {
                    Object3dImpl obj = itr.next();
                    if (obj.isSelected() && obj.markTime != startTime) {
                        obj.display(gl, glu);
                        obj.markTime = startTime;
                    }
                }
            } else if (vizConfig.getDisplayConfig() == DisplayConfig.DISPLAY_ALPHA) {
                //Selected edges are rendered with 1f alpha, half otherwise
                for (Iterator<Object3dImpl> itr = octree.getObjectIterator(CLASS_EDGE); itr.hasNext();) {
                    Object3dImpl obj = itr.next();
                    if (obj.markTime != startTime) {
                        obj.getObj().setAlpha(obj.isSelected() ? 1f : 0.2f);
                        obj.display(gl, glu);
                        obj.markTime = startTime;
                    }
                }
            }
            gl.glEnd();
            gl.glEnable(GL.GL_LIGHTING);
        //gl.glEnable(GL.GL_BLEND);
        }

        //Node
        if (object3dClasses[CLASS_NODE].isEnabled()) {
            if (vizConfig.getDisplayConfig() == DisplayConfig.DISPLAY_ALPHA) {
                //Selected nodes are rendered with 1f alpha, half otherwise
                for (Iterator<Object3dImpl> itr = octree.getObjectIterator(CLASS_NODE); itr.hasNext();) {
                    Object3dImpl obj = itr.next();
                    if (obj.markTime != startTime) {
                        obj.getObj().setAlpha(obj.isSelected() ? 1f : 0.2f);
                        obj.display(gl, glu);
                        obj.markTime = startTime;
                    }
                }
            } else {
                //Mode normal
                for (Iterator<Object3dImpl> itr = octree.getObjectIterator(CLASS_NODE); itr.hasNext();) {
                    Object3dImpl obj = itr.next();
                    if (obj.markTime != startTime) {
                        obj.display(gl, glu);
                        obj.markTime = startTime;
                    }
                }
            }
        }

        //Arrows
        if (object3dClasses[CLASS_ARROW].isEnabled()) {
            gl.glBegin(GL.GL_TRIANGLES);
            for (Iterator<Object3dImpl> itr = octree.getObjectIterator(CLASS_ARROW); itr.hasNext();) {
                Object3dImpl obj = itr.next();
                if (obj.markTime != startTime) {
                    obj.display(gl, glu);
                    obj.markTime = startTime;
                }
            }
            gl.glEnd();
        }

        //Potatoes
        if (object3dClasses[CLASS_POTATO].isEnabled()) {
            //gl.glDisable(GL.GL_LIGHTING);

            //Triangles
            gl.glDisable(GL.GL_LIGHTING);
            gl.glBegin(GL.GL_TRIANGLES);
            for (Iterator<Object3dImpl> itr = octree.getObjectIterator(CLASS_POTATO); itr.hasNext();) {
                Object3dImpl obj = itr.next();
                if (!obj.mark) {
                    obj.display(gl, glu);
                    obj.mark = true;
                }
            }
            gl.glEnd();
            gl.glEnable(GL.GL_LIGHTING);

            //Solid disk
            for (Iterator<Object3dImpl> itr = octree.getObjectIterator(CLASS_POTATO); itr.hasNext();) {
                Object3dImpl obj = itr.next();
                if (obj.markTime != startTime) {
                    obj.display(gl, glu);
                    obj.markTime = startTime;
                    obj.mark = false;
                }
            }
        //gl.glEnable(GL.GL_LIGHTING);
        }

        octree.displayOctree(gl);
    /*

    float x1 = -140f;
    float x2 = 200f;
    float y1 = 56;
    float y2 = 150;
    float z1 = 0;
    float z2 = 0;
    float t=3f;

    gl.glBegin(GL.GL_POINTS);
    gl.glVertex3f(x1, y1, z1);
    gl.glVertex3f(x2, y2, z2);
    gl.glEnd();


    float distance = (float)Math.sqrt(Math.pow((double)x1 - x2,2d) +
    Math.pow((double)y1 - y2,2d) +
    Math.pow((double)z1 - z2,2d));
    double anglez = Math.atan2(y2 - y1, x2 - x1);

    gl.glPushMatrix();

    gl.glTranslatef(x1+(float)Math.cos(anglez)*distance/2f, y1+(float)Math.sin(anglez)*distance/2f, 1);
    gl.glRotatef((float)Math.toDegrees(anglez), 0, 0, 1);
    gl.glScalef(distance/2f, 1f, 0f);


    //gl.glRotatef((float)Math.toDegrees(anglez), 0, 0, 1);
    //gl.glTranslatef(x1, 0, 0);
    //gl.glScalef(distance, 1f, 0f);


    gl.glBegin(GL.GL_TRIANGLE_STRIP);
    gl.glVertex3f(1, 1, 0);
    gl.glVertex3f(-1, 1,0);
    gl.glVertex3f(1, -1, 0);
    gl.glVertex3f(-1,-1, 0);
    gl.glEnd();

    gl.glPopMatrix();

    //Reference
    double angle = Math.atan2(y2 - y1, x2 - x1);
    float t2sina1 =(float)( t / 2 * Math.sin(angle));
    float t2cosa1 = (float)( t / 2 * Math.cos(angle));
    float t2sina2 = (float)( t / 2 * Math.sin(angle));
    float t2cosa2 = (float)( t / 2 * Math.cos(angle));

    gl.glColor3i(255, 0, 0);
    gl.glBegin(GL.GL_TRIANGLES);
    gl.glVertex2f(x1 + t2sina1, y1 - t2cosa1+20);
    gl.glVertex2f(x2 + t2sina2, y2 - t2cosa2+20);
    gl.glVertex2f(x2 - t2sina2, y2 + t2cosa2+20);
    gl.glVertex2f(x2 - t2sina2, y2 + t2cosa2+20);
    gl.glVertex2f(x1 - t2sina1, y1 + t2cosa1+20);
    gl.glVertex2f(x1 + t2sina1, y1 - t2cosa1+20);
    gl.glEnd();*/

    /*DhnsController.getInstance().updatePotatoes();

    gl.glColor3f(0.2f,0.2f,0.2f);
    PotatoBuilder potatoBuilder = DhnsController.getInstance().getPotatoBuilder();
    for(Potato p : potatoBuilder.getPotatoes())
    {
    gl.glBegin(GL.GL_TRIANGLES);
    for(Potato.Triangle triangles : p.getTriangles())
    {
    float[] array = triangles.array;
    gl.glVertex3f(array[0], array[1],0f);
    gl.glVertex3f(array[2], array[3],0f);
    gl.glVertex3f(array[4], array[5],0f);
    }
    gl.glEnd();

    gl.glBegin(GL.GL_QUADS);
    for(Potato.Square squares : p.getSquares())
    {
    float[] array = squares.array;

    gl.glVertex2f(array[0], array[1]);
    gl.glVertex2f(array[2], array[3]);
    gl.glVertex2f(array[4], array[5]);
    gl.glVertex2f(array[6], array[7]);
    }
    gl.glEnd();

    for(Potato.Circle circle : p.getCircles())
    {
    gl.glPushMatrix();
    gl.glTranslatef(circle.x,circle.y,0f);
    GLUquadric quadric = glu.gluNewQuadric();
    glu.gluDisk(quadric,0f, circle.rayon, 20, 20);
    glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
    glu.gluDeleteQuadric(quadric);
    gl.glPopMatrix();
    }
    }*/
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
        inited=true;
    }

    @Override
    public void addObject(int classID, Object3dImpl obj) {
        octree.addObject(classID, obj);
    }

    @Override
    public void removeObject(int classID, Object3dImpl obj) {
        octree.removeObject(classID, obj);
    }

    @Override
    public void resetObjectClass(Object3dClass object3dClass) {
        octree.resetObjectClass(object3dClass.getClassId());
    }

    @Override
    public void mouseClick() {
        for (Object3dClass objClass : clickableClasses) {
            Object3dImpl[] objArray = selectedObjects[objClass.getSelectionId()].toArray(new Object3dImpl[0]);
            if (objArray.length > 0) {
                eventBridge.mouseClick(objClass, objArray);
            }
        }
    }

    @Override
    public void mouseDrag() {
        float[] drag = graphIO.getMouseDrag();
        for (Object3dImpl obj : selectedObjects[0]) {
            float[] mouseDistance = obj.getDragDistanceFromMouse();
            obj.getObj().setX(drag[0] + mouseDistance[0]);
            obj.getObj().setY(drag[1] + mouseDistance[1]);
        }
    }

    @Override
    public void mouseMove() {

        List<Object3dImpl> newSelectedObjects = null;
        List<Object3dImpl> unSelectedObjects = null;

        if (vizEventManager.hasSelectionListeners()) {
            newSelectedObjects = new ArrayList<Object3dImpl>();
            unSelectedObjects = new ArrayList<Object3dImpl>();
        }

        long markTime = System.currentTimeMillis();
        int i = 0;
        for (Object3dClass objClass : selectableClasses) {
            for (Iterator<Object3dImpl> itr = octree.getSelectedObjectIterator(objClass.getClassId()); itr.hasNext();) {
                Object3dImpl obj = itr.next();
                if (isUnderMouse(obj) && currentSelectionArea.select(obj.getObj())) {
                    if (!obj.isSelected()) {
                        //New selected
                        obj.setSelected(true);
                        if (vizEventManager.hasSelectionListeners()) {
                            newSelectedObjects.add(obj);
                        }
                        selectedObjects[i].add(obj);
                    }
                    obj.selectionMark = markTime;
                } else if (currentSelectionArea.unselect(obj.getObj())) {
                    if (vizEventManager.hasSelectionListeners() && obj.isSelected()) {
                        unSelectedObjects.add(obj);
                    }
                }
            }

            for (Iterator<Object3dImpl> itr = selectedObjects[i].iterator(); itr.hasNext();) {
                Object3dImpl o = itr.next();
                if (o.selectionMark != markTime) {
                    itr.remove();
                    o.setSelected(false);
                }
            }
            i++;
        }
    }

    @Override
    public void refreshGraphLimits() {
    }

    @Override
    public void startDrag() {
        float x = graphIO.getMouseDrag()[0];
        float y = graphIO.getMouseDrag()[1];

        for (Iterator<Object3dImpl> itr = selectedObjects[0].iterator(); itr.hasNext();) {
            Object3dImpl o = itr.next();
            float[] tab = o.getDragDistanceFromMouse();
            tab[0] = o.getObj().x() - x;
            tab[1] = o.getObj().y() - y;
        }
    }

    @Override
    public void stopDrag() {
        scheduler.requireUpdatePosition();
    }

    @Override
    public void updateObjectsPosition() {
        for (Object3dClass objClass : object3dClasses) {
            if (objClass.isEnabled()) {
                octree.updateObjectsPosition(objClass.getClassId());
            }
        }
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

        object3dClasses[CLASS_POTATO].getCurrentObject3dInitializer().initDisplayLists(gl, glu, quadric, ptr);

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
        clickableClasses = new CompatibilityObject3dClass[0];


        object3dClasses[0].setEnabled(true);
        object3dClasses[1].setEnabled(true);
        object3dClasses[2].setEnabled(true);
        object3dClasses[3].setEnabled(true);

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

        //Clickable
        classList.clear();
        for (Object3dClass objClass : object3dClasses) {
            if (objClass.isClickable()) {
                classList.add(objClass);
            }
        }
        clickableClasses = classList.toArray(clickableClasses);

        //Init selection lists
        selectedObjects = new ConcurrentLinkedQueue[selectableClasses.length];
        int i = 0;
        for (Object3dClass objClass : selectableClasses) {
            objClass.setSelectionId(i);
            selectedObjects[i] = new ConcurrentLinkedQueue<Object3dImpl>();
            i++;
        }
    }

    public CompatibilityObject3dClass[] getObject3dClasses() {
        return object3dClasses;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
