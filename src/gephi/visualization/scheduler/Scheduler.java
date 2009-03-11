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
import gephi.visualization.swing.GraphDrawable;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Mathieu
 */
public class Scheduler {

    //Architeture
	private GraphDrawable graphDrawable;
	private AbstractEngine engine;

	//Executor
	private ScheduledExecutorService displayExecutor;

    //States
    AtomicBoolean animating         = new AtomicBoolean();

    public Scheduler(GraphDrawable drawable, AbstractEngine engine)
    {
        this.graphDrawable = drawable;
        this.engine = engine;
    }

    public void start()
    {
       //BetterFPSAnimator animator = new BetterFPSAnimator(graphDrawable, 30);
       //animator.start();

       SimpleFPSAnimator simpleFPSAnimator = new SimpleFPSAnimator(graphDrawable, 30);
       simpleFPSAnimator.start();

        //FPSAnimator fPSAnimator = new FPSAnimator(graphDrawable.getGLAutoDrawable(),30,true);
        //fPSAnimator.start();

       /*displayExecutor = Executors.newSingleThreadScheduledExecutor();
        Runnable displayCall = new Runnable() {

            public void run() {
                graphDrawable.display();
            }
        };
        long delay = (long) (1000.0f / (float) 45);
		displayExecutor.scheduleWithFixedDelay(displayCall, 0, delay, TimeUnit.MILLISECONDS);*/
    }

    public void stop()
    {

    }
}
