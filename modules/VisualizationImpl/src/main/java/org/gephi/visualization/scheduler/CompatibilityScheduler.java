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
package org.gephi.visualization.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.Scheduler;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.CompatibilityEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityScheduler implements Scheduler, VizArchitecture {

    //States
    AtomicBoolean animating = new AtomicBoolean();
    AtomicBoolean cameraMoved = new AtomicBoolean();
    AtomicBoolean mouseMoved = new AtomicBoolean();
    AtomicBoolean startDrag = new AtomicBoolean();
    AtomicBoolean drag = new AtomicBoolean();
    AtomicBoolean stopDrag = new AtomicBoolean();
    AtomicBoolean mouseClick = new AtomicBoolean();
    //Architeture
    private GraphDrawable graphDrawable;
    private CompatibilityEngine engine;
    private VizConfig vizConfig;
    //Animators
    private BasicFPSAnimator displayAnimator;
    private BasicFPSAnimator updateAnimator;
    private float displayFpsLimit = 30f;
    private final float updateFpsLimit = 5f;
    private final Object worldLock = new Object();

    @Override
    public void initArchitecture() {
        this.graphDrawable = VizController.getInstance().getDrawable();
        this.engine = (CompatibilityEngine) VizController.getInstance().getEngine();
        this.vizConfig = VizController.getInstance().getVizConfig();
    }

    @Override
    public synchronized void start() {
        if (displayAnimator != null) {
            displayAnimator.shutdown();
        }
        if (updateAnimator != null) {
            updateAnimator.shutdown();
        }
        displayAnimator = new BasicFPSAnimator(new Runnable() {
            @Override
            public void run() {
                graphDrawable.display();
            }
        }, worldLock, "DisplayAnimator", displayFpsLimit);
        displayAnimator.start();

        updateAnimator = new BasicFPSAnimator(new Runnable() {
            @Override
            public void run() {
                updateWorld();
            }
        }, worldLock, "UpdateAnimator", updateFpsLimit);
        updateAnimator.start();
    }

    @Override
    public synchronized void stop() {
        updateAnimator.shutdown();
        displayAnimator.shutdown();

        cameraMoved.set(false);
        mouseMoved.set(false);
        startDrag.set(false);
        drag.set(false);
        stopDrag.set(false);
        mouseClick.set(false);
    }

    @Override
    public boolean isAnimating() {
        return displayAnimator != null && displayAnimator.isAnimating();
    }

    @Override
    public void display(GL2 gl, GLU glu) {
        //Boolean vals
        boolean execMouseClick = mouseClick.getAndSet(false);
        boolean execMouseMove = mouseMoved.getAndSet(false);
        boolean execDrag = drag.get() || startDrag.get() || stopDrag.get();

        if (cameraMoved.getAndSet(false)) {
            graphDrawable.setCameraPosition(gl, glu);

            engine.getOctree().updateVisibleOctant(gl);
            //Objects iterators in octree are ready

            //Task MODEL - LOD
            engine.updateLOD();
        }

        //Task SELECTED
        if (execMouseMove) {
            engine.mouseMove();
            engine.updateSelection(gl, glu);
        } else if (execDrag) {
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

        //Task AFTERSELECTION
        if (execMouseClick) {
            engine.mouseClick();
        }

        //Display
        engine.beforeDisplay(gl, glu);
        engine.display(gl, glu);
        engine.afterDisplay(gl, glu);
    }

    @Override
    public void updateWorld() {
        if (engine.updateWorld()) {
            cameraMoved.set(true);
            mouseMoved.set(true);
        }
    }

    @Override
    public void updatePosition() {
//        if (objectsMoved.getAndSet(false)) {
//            engine.updateObjectsPosition();
//            cameraMoved.set(true);
//        }
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
    public void requireMouseClick() {
        mouseClick.set(true);
    }

    @Override
    public void setFps(float maxFps) {
        this.displayFpsLimit = maxFps;
        if (displayAnimator != null) {
            displayAnimator.setFps(maxFps);
        }
    }

    @Override
    public float getFps() {
        return displayFpsLimit;
    }
}
