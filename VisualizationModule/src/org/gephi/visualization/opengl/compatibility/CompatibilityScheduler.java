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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.Scheduler;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.api.objects.CompatibilityModelClass;
import org.gephi.visualization.scheduler.SimpleFPSAnimator;
import org.gephi.visualization.swing.GraphDrawableImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityScheduler implements Scheduler, VizArchitecture {

    //States
    AtomicBoolean animating = new AtomicBoolean();
    AtomicBoolean cameraMoved = new AtomicBoolean();
    AtomicBoolean mouseMoved = new AtomicBoolean();
    AtomicBoolean objectsMoved = new AtomicBoolean();
    AtomicBoolean startDrag = new AtomicBoolean();
    AtomicBoolean drag = new AtomicBoolean();
    AtomicBoolean stopDrag = new AtomicBoolean();
    AtomicBoolean mouseClick = new AtomicBoolean();
    //Architeture
    private GraphDrawableImpl graphDrawable;
    private CompatibilityEngine engine;
    private VizConfig vizConfig;
    //Current GL
    private GL gl;
    private GLU glu;
    //Animator
    private SimpleFPSAnimator simpleFPSAnimator;
    private float fpsLimit = 30f;

    public void initArchitecture() {
        this.graphDrawable = VizController.getInstance().getDrawable();
        this.engine = (CompatibilityEngine) VizController.getInstance().getEngine();
        this.vizConfig = VizController.getInstance().getVizConfig();
        initPools();
        init();
    }
    private ThreadPoolExecutor pool1;
    private ThreadPoolExecutor pool2;
    private List<Runnable> modelSegments;
    private Semaphore pool1Semaphore = new Semaphore(0);
    private Semaphore pool2Semaphore = new Semaphore(0);
    private Runnable selectionSegment;
    private Runnable startDragSegment;
    private Runnable dragSegment;
    private Runnable refreshLimitsSegment;
    private Runnable mouseClickSegment;

    private void initPools() {
        pool1 = new ThreadPoolExecutor(0, 4, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()) {

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                pool1Semaphore.release();
            }

            @Override
            public void execute(Runnable command) {
                super.execute(command);
            }
        };

        pool2 = new ThreadPoolExecutor(0, 4, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()) {

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                pool2Semaphore.release();
            }

            @Override
            public void execute(Runnable command) {
                super.execute(command);
            }
        };
    }

    public void init() {
        modelSegments = new ArrayList<Runnable>();
        for (final CompatibilityModelClass objClass : engine.lodClasses) {
            modelSegments.add(new Runnable() {

                public void run() {
                    if (objClass.isEnabled()) {
                        objClass.lod(engine.getOctree().getObjectIterator(objClass.getClassId()));
                    }
                }
            });
        }

        selectionSegment = new Runnable() {

            public void run() {
                engine.mouseMove();
            }
        };

        refreshLimitsSegment = new Runnable() {

            public void run() {
            }
        };

        dragSegment = new Runnable() {

            public void run() {

                //Drag
                if (stopDrag.getAndSet(false)) {
                    engine.stopDrag();
                }
                if (startDrag.getAndSet(false)) {
                    engine.startDrag();
                }
                if (drag.getAndSet(false)) {
                    engine.mouseDrag();
                }
            }
        };

        mouseClickSegment = new Runnable() {

            public void run() {
                engine.mouseClick();
            }
        };
    }

    @Override
    public synchronized  void start() {
        simpleFPSAnimator = new SimpleFPSAnimator(this, graphDrawable, fpsLimit);
        simpleFPSAnimator.start();
    }

    @Override
    public synchronized void stop() {
        if(simpleFPSAnimator==null) {
            return;
        }
        if (simpleFPSAnimator.isAnimating()) {
            simpleFPSAnimator.shutdown();
        }
        cameraMoved.set(false);
        mouseMoved.set(false);
        objectsMoved.set(false);
        startDrag.set(false);
        drag.set(false);
        stopDrag.set(false);
        mouseClick.set(false);
    }

    public boolean isAnimating() {
        if (simpleFPSAnimator != null && simpleFPSAnimator.isAnimating()) {
            return true;
        }
        return false;
    }

    @Override
    public void display(GL gl, GLU glu) {
        if (simpleFPSAnimator.isDisplayCall()) {
            this.gl = gl;
            this.glu = glu;

            //Boolean vals
            boolean execMouseClick = mouseClick.getAndSet(false);
            boolean execMouseMove = mouseMoved.getAndSet(false);
            boolean execDrag = drag.get() || startDrag.get() || stopDrag.get();

            //Calculate permits
            int pool1Permit = 0;
            int pool2Permit = 0;
            if (execMouseMove) {
                pool2Permit++;
            } else if (execDrag) {
                pool2Permit++;
            }
            if (execMouseClick) {
                pool2Permit++;
            }

            if (cameraMoved.getAndSet(false)) {
                graphDrawable.setCameraPosition(gl, glu);

                pool1Permit = modelSegments.size();
                engine.getOctree().updateVisibleOctant(gl);
                //Objects iterators in octree are ready

                //Task MODEL
                for (int i = 0; i < modelSegments.size(); i++) {
                    Runnable r = modelSegments.get(i);
                    pool1.execute(r);
                }
            }

            //Task SELECTED
            if (execMouseMove) {
                engine.updateSelection(gl, glu);
                pool2.execute(selectionSegment);
            } else if (execDrag) {
                pool2.execute(dragSegment);
            }


            //Task AFTERSELECTION
            if (execMouseClick) {
                pool2.execute(mouseClickSegment);
            }

            try {
                if (pool1Permit > 0) {
                    pool1Semaphore.acquire(pool1Permit);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Display
            engine.beforeDisplay(gl, glu);
            engine.display(gl, glu);
            engine.afterDisplay(gl, glu);

            try {
                if (pool2Permit > 0) {
                    pool2Semaphore.acquire(pool2Permit);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateWorld() {
        if (engine.updateWorld()) {
            cameraMoved.set(true);
            mouseMoved.set(true);
        }
    }

    @Override
    public void updatePosition() {
        if (objectsMoved.getAndSet(false)) {
            engine.updateObjectsPosition();
            cameraMoved.set(true);
        }
    }

    @Override
    public void requireUpdateVisible() {
        cameraMoved.set(true);
    }

    @Override
    public void requireUpdateSelection() {
        mouseMoved.set(true);
    }

    @Override
    public void requireStartDrag() {
        startDrag.set(true);
    }

    @Override
    public void requireDrag() {
        drag.set(true);
    }

    @Override
    public void requireStopDrag() {
        stopDrag.set(true);
    }

    @Override
    public void requireUpdatePosition() {
        objectsMoved.set(true);
    }

    @Override
    public void requireMouseClick() {
        mouseClick.set(true);
    }

    public void setFps(float maxFps) {
        this.fpsLimit = maxFps;
        if (simpleFPSAnimator != null) {
            simpleFPSAnimator.setFps(maxFps);
        }
    }

    public float getFps() {
        return fpsLimit;
    }
}
