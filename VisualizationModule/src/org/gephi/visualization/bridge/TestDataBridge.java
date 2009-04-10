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

package org.gephi.visualization.bridge;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeImpl;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeImpl;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.api.initializer.Object3dInitializer;
import org.gephi.visualization.opengl.AbstractEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class TestDataBridge implements DataBridge {

    //Architecture
    protected AbstractEngine engine;

    private AtomicBoolean update = new AtomicBoolean(true);

    @Override
    public void initArchitecture() {
       this.engine = VizController.getInstance().getEngine();
    }

    public void updateWorld() {
        updateNodes();
        updateEdges();
    }

    private ArrayList<Node> nodeList = new ArrayList<Node>();
    private void updateNodes()
    {
        Object3dInitializer nodeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_NODE].getCurrentObject3dInitializer();

         //Remove nodes
        if(nodeList.size()>0)
        {
            for(int i=0;i<10;i++)
            {
                Node n = nodeList.remove((int)Math.random()*nodeList.size());
                engine.removeObject(AbstractEngine.CLASS_NODE, (Object3dImpl)n.getObject3d());
            }
        }

        //Add nodes
        for(int i=0;i<1500;i++)
        {
            NodeImpl n = new NodeImpl();
            nodeList.add(n);
            n.setSize(3f);
            engine.addObject(AbstractEngine.CLASS_NODE, nodeInit.initObject(n));
        }

    }

    private ArrayList<Edge> edgeList = new ArrayList<Edge>();
    private void updateEdges()
    {
        Object3dInitializer edgeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_EDGE].getCurrentObject3dInitializer();
        Object3dInitializer arrowInit = engine.getObject3dClasses()[AbstractEngine.CLASS_ARROW].getCurrentObject3dInitializer();

        //Remove
        if(edgeList.size()>0)
        {
            for(int i=0;i<4000;i++)
            {
                Edge e = edgeList.remove((int)Math.random()*edgeList.size());
                engine.removeObject(AbstractEngine.CLASS_EDGE, (Object3dImpl)e.getObject3d());
            }
        }

        //Add
        for(int i=0;i<50;i++)
        {
            Node source = nodeList.get((int)(Math.random()*nodeList.size()));
            Node target = nodeList.get((int)(Math.random()*nodeList.size()));
            EdgeImpl edge = new EdgeImpl(source,target);
            edge.setCardinal((float)(Math.random()*1f)+1);
            edgeList.add(edge);
            engine.addObject(AbstractEngine.CLASS_EDGE, edgeInit.initObject(edge));
            //engine.addObject(AbstractEngine.CLASS_ARROW, arrowInit.initObject(edge));
        }
    }

    public boolean requireUpdate() {
        return update.getAndSet(false);
    }
    
}
