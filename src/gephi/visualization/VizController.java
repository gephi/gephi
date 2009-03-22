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

package gephi.visualization;

import gephi.visualization.config.VizCommander;
import gephi.visualization.config.VizConfig;
import gephi.visualization.events.StandardVizEventManager;
import gephi.visualization.events.VizEventManager;
import gephi.visualization.objects.Object3dClassLibrary;
import gephi.visualization.objects.StandardObject3dClassLibrary;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.compatibility.CompatibilityEngine;
import gephi.visualization.opengl.compatibility.CompatibilityScheduler;
import gephi.visualization.scheduler.Scheduler;
import gephi.visualization.swing.GraphDrawable;
import gephi.visualization.swing.GraphIO;
import gephi.visualization.swing.StandardGraphIO;
import java.awt.Dimension;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author Mathieu
 */
public class VizController {
	private static VizController instance;

	private VizController()
	{ }

	public synchronized static VizController getInstance()
	{
		if(instance == null)
		{
			instance = new VizController();
		}
		return instance;
	}

    //Architecture
    private GraphDrawable drawable;
    private AbstractEngine engine;
    private Scheduler scheduler;
    private VizConfig vizConfig;
    private GraphIO graphIO;
    private VizEventManager vizEventManager;
    private Object3dClassLibrary object3dClassLibrary;
    private GraphLimits limits;

    public void initInstances()
    {
        VizCommander commander = new VizCommander();
        

        vizConfig = new VizConfig();
        graphIO = new StandardGraphIO();
        engine = new CompatibilityEngine();
        vizEventManager = new StandardVizEventManager();
        scheduler = new CompatibilityScheduler();
        object3dClassLibrary = new StandardObject3dClassLibrary();
        limits = new GraphLimits();

        drawable = commander.createPanel();
        drawable.getGraphComponent().setPreferredSize( new Dimension(600, 600));
        drawable.initArchitecture();
        engine.initArchitecture();
        ((CompatibilityScheduler)scheduler).initArchitecture();
        ((StandardGraphIO)graphIO).initArchitecture();
    }

    public GraphDrawable getDrawable() {
        return drawable;
    }

    public AbstractEngine getEngine() {
        return engine;
    }

    public GraphIO getGraphIO() {
        return graphIO;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public VizConfig getVizConfig() {
        return vizConfig;
    }

    public Object3dClassLibrary getObject3dClassLibrary() {
        return object3dClassLibrary;
    }

    public VizEventManager getVizEventManager() {
        return vizEventManager;
    }

    public GraphLimits getLimits() {
        return limits;
    }
    
    
}
