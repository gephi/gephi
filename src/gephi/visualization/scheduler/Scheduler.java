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

package gephi.visualization.scheduler;

import com.sun.opengl.util.FPSAnimator;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.compatibility.CompatibilityEngine;
import gephi.visualization.swing.GraphDrawable;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 *
 * @author Mathieu
 */
public interface Scheduler {

    public void start();
    public void stop();
    public void updatePosition();
    public void updateWorld();
    public void display(GL gl, GLU glu);

    public void requireUpdateVisible();
    public void requireUpdateSelection();
    public void requireStartDrag();
    public void requireDrag();
    public void requireStopDrag();
    public void requireUpdatePosition();
    public void requireMouseClick();
}
