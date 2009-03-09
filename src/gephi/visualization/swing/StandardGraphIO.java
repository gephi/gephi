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

package gephi.visualization.swing;

import gephi.visualization.events.VizEventManager;
import gephi.visualization.opengl.AbstractEngine;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.SwingUtilities;
/**
 *
 * @author Mathieu
 */
public class StandardGraphIO implements GraphIO {

    protected GraphDrawable graphDrawable;
    protected AbstractEngine engine;
    protected VizEventManager vizEventManager;
    
    //Listeners data
    protected float[] rightButtonMoving;
	protected float[] leftButtonMoving;
	protected float[] middleButtonMoving;
    protected float[] mousePosition;
	protected float[] mouseDrag;

    //Flags
	protected boolean draggingEnable=false;
	protected boolean dragging = false;

    public StandardGraphIO(GraphDrawable graphDrawable, AbstractEngine engine)
    {
        this.graphDrawable = graphDrawable;
        this.engine = engine;
    }

    public void startMouseListening()
    {
        graphDrawable.graphComponent.addMouseListener(this);
		graphDrawable.graphComponent.addMouseWheelListener(this);
		graphDrawable.graphComponent.addMouseMotionListener(this);
	}

	public void stopMouseListening()
	{
		graphDrawable.graphComponent.removeMouseListener(this);
		graphDrawable.graphComponent.removeMouseMotionListener(this);
		graphDrawable.graphComponent.removeMouseWheelListener(this);
	}

    public void mousePressed(MouseEvent e)
	{
		float x = e.getLocationOnScreen().x - graphDrawable.graphComponent.getLocationOnScreen().x;
		float y = e.getLocationOnScreen().y - graphDrawable.graphComponent.getLocationOnScreen().y;

		if(SwingUtilities.isRightMouseButton(e))
		{
			//Save the coordinate of the start
			rightButtonMoving[0] = x;
			rightButtonMoving[1] = y;
			graphDrawable.graphComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            vizEventManager.mouseRightPress();
		}
		else if(SwingUtilities.isMiddleMouseButton(e))
		{
			middleButtonMoving[0] = x;
			middleButtonMoving[1] = y;
			graphDrawable.graphComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            vizEventManager.mouseMiddlePress();
		}
		else if(SwingUtilities.isLeftMouseButton(e))
		{
			leftButtonMoving[0] = x;
			leftButtonMoving[1] = y;
            vizEventManager.mouseLeftPress();
		}
	}

    public void mouseReleased(MouseEvent e)
    {
		//Disable the right button moving
		rightButtonMoving[0] = -1;
		leftButtonMoving[0] = -1;
		middleButtonMoving[0] = -1;

		//Update mouse position because the movement during dragging
		float x = e.getLocationOnScreen().x - graphDrawable.graphComponent.getLocationOnScreen().x;
		float y = e.getLocationOnScreen().y - graphDrawable.graphComponent.getLocationOnScreen().y;
		mousePosition[0] = x;
		mousePosition[1] =  graphDrawable.viewport.get(3)-y;

		if(dragging)
		{
			dragging = false;
            vizEventManager.stopDrag();
		}
		else
		{
			graphDrawable.graphComponent.setCursor(Cursor.getDefaultCursor());
		}
	}

    public void mouseEntered(MouseEvent e)
    {
		dragging=false;
	}


	public void mouseExited(MouseEvent e)
    {
		if(!dragging)
		{

		}
	}

    public void mouseMoved(MouseEvent e)
	{
		float x = e.getLocationOnScreen().x - graphDrawable.graphComponent.getLocationOnScreen().x;
		float y = e.getLocationOnScreen().y - graphDrawable.graphComponent.getLocationOnScreen().y;
		mousePosition[0] = x;
		mousePosition[1] =  graphDrawable.viewport.get(3)-y;

        vizEventManager.mouseMove();
	}

	/**
	 * Mouse clicked event.
	 */
	public void mouseClicked(MouseEvent e)
	{
		if(SwingUtilities.isLeftMouseButton(e))
		{
            vizEventManager.mouseLeftClick();
		}
		else if(SwingUtilities.isRightMouseButton(e))
		{
            vizEventManager.mouseRightClick();
		}
        else if(SwingUtilities.isMiddleMouseButton(e))
        {
            vizEventManager.mouseMiddleClick();
        }
	}

    public void mouseDragged(MouseEvent e) {

		float x = e.getLocationOnScreen().x - graphDrawable.graphComponent.getLocationOnScreen().x;//TODO Pourqoui ce osnt des float et pas des int
		float y = e.getLocationOnScreen().y - graphDrawable.graphComponent.getLocationOnScreen().y;

		if(rightButtonMoving[0] != -1)
		{
			//The right button is pressed
			float proche = graphDrawable.cameraTarget[2] - graphDrawable.cameraLocation[2];
			proche = proche/300;

			graphDrawable.cameraTarget[0] += (x - rightButtonMoving[0])*proche;
			graphDrawable.cameraTarget[1] += (rightButtonMoving[1]-y)*proche;
			graphDrawable.cameraLocation[0] += (x - rightButtonMoving[0])*proche;
			graphDrawable.cameraLocation[1] += (rightButtonMoving[1]-y)*proche;

			rightButtonMoving[0] = x;
			rightButtonMoving[1] = y;
			graphDrawable.cameraMoved();
		}

		if(middleButtonMoving[0] != -1)
		{
			//The middle button is pressed
			float angleY = y - middleButtonMoving[1];
			if(angleY > 0 || (graphDrawable.cameraTarget[1] - graphDrawable.cameraLocation[1] >0))
			{
				middleButtonMoving[1] = y;

				graphDrawable.cameraLocation[1] = graphDrawable.cameraLocation[1] - Math.abs(graphDrawable.cameraLocation[2]-graphDrawable.cameraTarget[2])*(float)Math.sin(Math.toRadians(angleY));

				graphDrawable.cameraMoved();
			}
		}

		if(leftButtonMoving[0] != -1)
		{
			if(draggingEnable)
			{
				//Remet à jour aussi la mousePosition pendant le drag, notamment pour coller quand drag released
				mousePosition[0] = x;
				mousePosition[1] =  graphDrawable.viewport.get(3)-y;

				mouseDrag[0] = (float)((graphDrawable.viewport.get(2)/2 - x)/graphDrawable.draggingMarker[0] + graphDrawable.cameraTarget[0]);
				mouseDrag[1] = (float)((y-graphDrawable.viewport.get(3)/2)/graphDrawable.draggingMarker[1] + graphDrawable.cameraTarget[1]);

				if(!dragging) {
					//Start drag
					dragging = true;
                    vizEventManager.startDrag();
				}

				vizEventManager.drag();

				leftButtonMoving[0] = x;
				leftButtonMoving[1] = y;
			}
		}
    }

    public void mouseWheelMoved(MouseWheelEvent e) {

        float[] graphLimits = engine.getGraphLimits();
		float graphWidth = Math.abs(graphLimits[1]-graphLimits[0]);
		float graphHeight = Math.abs(graphLimits[3]-graphLimits[2]);

		//On reduit l'hypothenuse et on calcule les depl z et y correpsondant
		double hypotenuse = Math.sqrt(Math.pow(graphDrawable.cameraTarget[1] - graphDrawable.cameraLocation[1],2d) +
				Math.pow(graphDrawable.cameraTarget[2] - graphDrawable.cameraLocation[2],2d));
		float move = e.getUnitsToScroll()*((float)hypotenuse*0.05f);

		float widthRatio = graphWidth/(float)hypotenuse;
		float heightRatio = graphHeight/(float)hypotenuse;
		float distanceRatio = Math.max(widthRatio, heightRatio);

		if(e.getUnitsToScroll() > 0 && distanceRatio < 0.03f)
			return;

		if(hypotenuse + move > 2 ) {
			hypotenuse = hypotenuse + move;

			double disY = hypotenuse*Math.sin(graphDrawable.rotationX);
			double disZ = hypotenuse*Math.cos(graphDrawable.rotationX);
			float moveY = e.getUnitsToScroll()*(float)(disY*1/(8+distanceRatio));
			float moveZ = e.getUnitsToScroll()*(float)(disZ*1/(8+distanceRatio));
			//float moveY = e.getUnitsToScroll()*(float)(disY*0.05f);
			//float moveZ = e.getUnitsToScroll()*(float)(disZ*0.05f);

			graphDrawable.cameraLocation[1] += moveY;
			graphDrawable.cameraLocation[2] += moveZ;
			graphDrawable.cameraMoved();
		}
	}

    public void keyPressed(KeyEvent e)
	{

	}

	public void keyReleased(KeyEvent e)
	{

	}

	public void keyTyped(KeyEvent e)
	{
        
    }

    public VizEventManager getVizEventManager() {
        return vizEventManager;
    }

    public void setVizEventManager(VizEventManager vizEventManager) {
        if(vizEventManager==null)
        {
            throw new NullPointerException("The instance of VizEventManager cannot be null");
        }
        this.vizEventManager = vizEventManager;
    }

    public float[] getMousePosition() {
        return mousePosition;
    }
}
