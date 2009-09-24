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
package org.gephi.ui.components.gradientslider;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

/** This is the abstract UI for <code>MultiThumbSliders</code>
 * 
 */
//Author Jeremy Wood
public abstract class MultiThumbSliderUI extends ComponentUI implements MouseListener, MouseMotionListener {
	protected MultiThumbSlider slider;

	/** The maximum width returned by <code>getMaximumSize()</code>.
	 * (or if the slider is vertical, this is the maximum height.)
	 */
	int MAX_LENGTH = 300;

	/** The minimum width returned by <code>getMinimumSize()</code>.
	 * (or if the slider is vertical, this is the minimum height.)
	 */
	int MIN_LENGTH = 50;

	/** The maximum width returned by <code>getPreferredSize()</code>.
	 * (or if the slider is vertical, this is the preferred height.)
	 */
	int PREF_LENGTH = 140;

	/** The height of a horizontal slider -- or width of a vertical slider.
	 */
	int DEPTH = 15;

	/** The pixel position of the thumbs.  This may be x or y coordinates, depending on
	 * whether this slider is horizontal or vertical
	 */
	int[] thumbPositions = new int[0];

	/** A float from zero to one, indicating whether that thumb should be highlighted
	 * or not.  
	 */
	protected float[] thumbIndications = new float[0];

	/** This is used by the animating thread.  The field indication is updated until it equals this value. */
	private float indicationGoal = 0;

	/** The overall indication of the thumbs.  At one they should be opaque,
	 * at zero they should be transparent.
	 */
	float indication = 0;

	/** The rectangle the track should be painted in.  */
	protected Rectangle trackRect = new Rectangle(0,0,0,0);

	public MultiThumbSliderUI(MultiThumbSlider slider) {
		this.slider = slider;
	}

	public Dimension getMaximumSize(JComponent s) {
		MultiThumbSlider mySlider = (MultiThumbSlider)s;
		if(mySlider.getOrientation()==MultiThumbSlider.HORIZONTAL) {
			return new Dimension(MAX_LENGTH, DEPTH);
		}
		return new Dimension(DEPTH, MAX_LENGTH);
	}

	public Dimension getMinimumSize(JComponent s) {
		MultiThumbSlider mySlider = (MultiThumbSlider)s;
		if(mySlider.getOrientation()==MultiThumbSlider.HORIZONTAL) {
			return new Dimension(MIN_LENGTH, DEPTH);
		}
		return new Dimension(DEPTH, MIN_LENGTH);
	}

	public Dimension getPreferredSize(JComponent s) {
		MultiThumbSlider mySlider = (MultiThumbSlider)s;
		if(mySlider.getOrientation()==MultiThumbSlider.HORIZONTAL) {
			return new Dimension(PREF_LENGTH, DEPTH);
		}
		return new Dimension(DEPTH, PREF_LENGTH);
	}

	/** This records the positions/values of each thumb.
	 * This is used when the mouse is pressed, so as the mouse
	 * is dragged values can get replaced and rearranged freely.
	 * (Including removing and adding thumbs)
	 * 
	 */
	class State {
		Object[] values;
		float[] positions;
		int selectedThumb;
		public State() {
			values = slider.getValues();
			positions = slider.getThumbPositions();
			selectedThumb = slider.getSelectedThumb(false);
		}

		public State(State s) {
			selectedThumb = s.selectedThumb;
			positions = new float[s.positions.length];
			values = new Object[s.values.length];
			System.arraycopy(s.positions,0,positions,0,positions.length);
			System.arraycopy(s.values,0,values,0,values.length);
		}

		/** Strip values outside of [0,1] */
		private void polish() {
			while(positions[0]<0) {
				float[] f2 = new float[positions.length-1];
				System.arraycopy(positions,1,f2,0,positions.length-1);
				Object[] c2 = new Object[values.length-1];
				System.arraycopy(values,1,c2,0,positions.length-1);
				positions = f2;
				values = c2;
				selectedThumb++;
			}
			while(positions[positions.length-1]>1) {
				float[] f2 = new float[positions.length-1];
				System.arraycopy(positions,0,f2,0,positions.length-1);
				Object[] c2 = new Object[values.length-1];
				System.arraycopy(values,0,c2,0,positions.length-1);
				positions = f2;
				values = c2;
				selectedThumb--;
			}
			if(selectedThumb>=positions.length)
				selectedThumb = -1;
		}

		/** Make the slider reflect this object */
		public void install() {
			polish();

			slider.setValues(positions, values);
			slider.setSelectedThumb(selectedThumb);
		}

		public void removeThumb(int index) {
			float[] f = new float[positions.length-1];
			Object[] c = new Object[values.length-1];
			System.arraycopy(positions, 0, f, 0, index);
			System.arraycopy(values, 0, c, 0, index);
			System.arraycopy(positions, index+1, f, index, f.length-index);
			System.arraycopy(values, index+1, c, index, f.length-index);
			positions = f;
			values = c;
			selectedThumb = -1;
		}
	}

	Thread animatingThread = null;

	Runnable animatingRunnable = new Runnable() {
		public void run() {
			boolean finished = false;
			while(!finished) {
				synchronized(MultiThumbSliderUI.this) {
					finished = true;
					for(int a = 0; a<thumbIndications.length; a++) {
						if(a!=slider.getSelectedThumb()) {
							if(a==currentIndicatedThumb) {
								if(thumbIndications[a]<1) {
									thumbIndications[a] = Math.min(1,thumbIndications[a]+.025f);
									finished = false;
								}
							} else {
								if(thumbIndications[a]>0) {
									thumbIndications[a] = Math.max(0,thumbIndications[a]-.025f);
									finished = false;
								}
							}
						} else {
							//the selected thumb is painted as selected,
							//so there's no indication to animate.
							//just set the indication to whatever it should
							//be and move on.  No repainting.
							if(a==currentIndicatedThumb) {
								thumbIndications[a] = 1;
							} else {
								thumbIndications[a] = 0;
							}
						}
					}
					if(indicationGoal>indication+.01f) {
						if(indication<.99f) {
							indication = Math.min(1,indication+.1f);
							finished = false;
						}
					} else if(indicationGoal<indication-.01f){
						if(indication>.01f) {
							indication = Math.max(0,indication-.1f);
							finished = false;
						}
					}
				}
				if(!finished)
					slider.repaint();

				//rest a little bit
				long t = System.currentTimeMillis();
				while(System.currentTimeMillis()-t<20) {
					try {
						Thread.sleep(10);
					} catch(Exception e) {
						Thread.yield();
					}
				}
			}
		}
	};

	private int currentIndicatedThumb = -1;
	private boolean mouseInside = false;
	private boolean mouseIsDown = false;
	private State pressedState;
	private int dx, dy;

	public void mousePressed(MouseEvent e) {
		dx = 0;
		dy = 0;

		if(slider.isEnabled()==false) return;

		if(e.getClickCount()>=2) {
			if(slider.doDoubleClick(e.getX(),e.getY())) {
				e.consume();
				return;
			}
		} else if(e.isPopupTrigger()) {
			int x = e.getX();
			int y = e.getY();
			if(slider.getOrientation()==MultiThumbSlider.HORIZONTAL) {
				if(x<trackRect.x || x>trackRect.x+trackRect.width)
					return;
				y = trackRect.y+trackRect.height;
			} else {
				if(y<trackRect.y || y>trackRect.y+trackRect.height)
					return;
				x = trackRect.x+trackRect.width;
			}
			if(slider.doPopup(x,y)) {
				e.consume();
				return;
			}
		}
		mouseIsDown = true;
		mouseMoved(e);

		if(e.getSource()!=slider) {
			throw new RuntimeException("only install this UI on the GradientSlider it was constructed with");
		}
		slider.requestFocus();

		int index = getIndex(e);
		if(index!=-1) {
			if(slider.getOrientation()==JSlider.HORIZONTAL) {
				dx = -e.getX()+thumbPositions[index];
			} else {
				dy = -e.getY()+thumbPositions[index];
			}
		}

		if(index!=-1) {
			slider.setSelectedThumb(index);
			e.consume();
		} else {
			if(slider.isAutoAdding()) {
				float k;

				int v;
				if(slider.getOrientation()==GradientSlider.HORIZONTAL) {
					v = e.getX();
				} else {
					v = e.getY();
				}

				if(slider.getOrientation()==GradientSlider.HORIZONTAL) {
					k = ((float)(v-trackRect.x))/((float)trackRect.width);
					if(slider.isInverted())
						k = 1-k;
				} else {
					k = ((float)(v-trackRect.y))/((float)trackRect.height);
					if(slider.isInverted()==false)
						k= 1-k;
				}
				if(k>0 && k<1) {
					int added = slider.addThumb(k);
					slider.setSelectedThumb(added);
				}
				e.consume();
			} else {
				if(slider.getSelectedThumb()!=-1) {
					slider.setSelectedThumb(-1);
					e.consume();
				}
			}
		}
		pressedState = new State();
	}

	private int getIndex(MouseEvent e) {
		int v;
		if(slider.getOrientation()==GradientSlider.HORIZONTAL) {
			v = e.getX();
			if(v<trackRect.x-getClickLocationTolerance()+1 || v>trackRect.x+trackRect.width+getClickLocationTolerance()-1) {
				return -1; // didn't click in the track;
			}
		} else {
			v = e.getY();
			if(v<trackRect.y-getClickLocationTolerance()+1 || v>trackRect.y+trackRect.height+getClickLocationTolerance()-1) {
				return -1;
			}
		}
		int min = Math.abs(v-thumbPositions[0]);
		int minIndex = 0;
		for(int a = 1; a<thumbPositions.length; a++) {
			int distance = Math.abs(v-thumbPositions[a]);
			if(distance<min) {
				min = distance;
				minIndex = a;
			}
		}
		if(min<getClickLocationTolerance()) {
			return minIndex;
		}
		return -1;
	}

	public void mouseEntered(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseExited(MouseEvent e) {
		setCurrentIndicatedThumb(-1);
		setMouseInside(false);
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseMoved(MouseEvent e) {
		if(slider.isEnabled()==false) return;

		int i = getIndex(e);
		setCurrentIndicatedThumb(i);
		boolean b = (e.getX()>=0 && e.getX()<slider.getWidth() && e.getY()>=0 && e.getY()<slider.getHeight());
		if(mouseIsDown) b = true;
		setMouseInside(b);
	}

	private void setCurrentIndicatedThumb(int i) {
		if(getProperty(slider,"MultiThumbSlider.indicateThumb","true").equals("false")) {
			//never activate a specific thumb
			i = -1;
		}
		currentIndicatedThumb = i;
		boolean finished = true;
		for(int a = 0; a<thumbIndications.length; a++) {
			if(a==currentIndicatedThumb) {
				if(thumbIndications[a]!=1) {
					finished = false;
				}
			} else {
				if(thumbIndications[a]!=0) {
					finished = false;
				}
			}
		}
		if(!finished) {
			synchronized(MultiThumbSliderUI.this) {
				if(animatingThread==null || animatingThread.isAlive()==false) {
					animatingThread = new Thread(animatingRunnable);
					animatingThread.start();
				}
			}
		}
	}
	private void setMouseInside(boolean b) {
		mouseInside = b;
		updateIndication();
	}

	public void mouseDragged(MouseEvent e) {
		if(slider.isEnabled()==false) return;

		e.translatePoint(dx, dy);

		mouseMoved(e);
		if(pressedState!=null && pressedState.selectedThumb!=-1) {
			slider.setValueIsAdjusting(true);

			State newState = new State(pressedState);
			float v;
			boolean outside;
			if(slider.getOrientation()==GradientSlider.HORIZONTAL) {
				v = ((float)(e.getX()-trackRect.x))/((float)trackRect.width);
				if(slider.isInverted())
					v = 1-v;
				outside = (e.getY()<trackRect.y-10) || (e.getY()>trackRect.y+trackRect.height+10);

				//don't whack the thumb off the slider if you happen to be *near* the edge:
				if(e.getX()>trackRect.x-10 && e.getX()<trackRect.x+trackRect.width+10) {
					if(v<0) v = 0;
					if(v>1) v = 1;
				}
			} else {
				v = ((float)(e.getY()-trackRect.y))/((float)trackRect.height);
				if(slider.isInverted()==false)
					v = 1-v;
				outside = (e.getX()<trackRect.x-10) || (e.getX()>trackRect.x+trackRect.width+10);

				if(e.getY()>trackRect.y-10 && e.getY()<trackRect.y+trackRect.height+10) {
					if(v<0) v = 0;
					if(v>1) v = 1;
				}
			}
			if(newState.positions.length<=2) {
				outside = false; //I don't care if you are outside: no removing!
			}
			newState.positions[newState.selectedThumb] = v;

			//because we delegate mouseReleased() to this method:
			if(outside) {
				newState.removeThumb(newState.selectedThumb);
			}
			if(validatePositions(newState)) {
				newState.install();
			}
			e.consume();
		}
	}

	public void mouseReleased(MouseEvent e) {
		if(slider.isEnabled()==false) return;

		mouseIsDown = false;
		if(pressedState!=null && slider.getThumbCount()<=pressedState.positions.length) {
			mouseDragged(e); //go ahead and commit this final location
		}
		if(slider.isValueAdjusting()) {
			slider.setValueIsAdjusting(false);
		}

		if(e.isPopupTrigger() && slider.doPopup(e.getX(),e.getY())) {
			//on windows popuptriggers happen on mouseRelease
			e.consume();
			return;
		}
	}

	/** This retrieves a property.
	 * If the component has this property manually set (by calling
	 * <code>component.putClientProperty()</code), then that value will be returned.
	 * Otherwise this method refers to <code>UIManager.get()</code>.  If that
	 * value is missing, this returns <code>defaultValue</code>
	 * 
	 * @param jc
	 * @param propertyName the property name
	 * @param defaultValue if no other value is found, this is returned
	 * @return the property value
	 */
	public static String getProperty(JComponent jc,String propertyName,String defaultValue) {
		Object jcValue = jc.getClientProperty(propertyName);
		if(jcValue!=null)
			return jcValue.toString();
		Object uiValue = UIManager.get(propertyName);
		if(uiValue!=null)
			return uiValue.toString();
		return defaultValue;
	}

	/** How many pixels can you deviate from a thumb and and still "click" it.*/
	public abstract int getClickLocationTolerance();

	/** Makes sure the thumbs are in the right order.
	 * 
	 * @param state
	 * @return true if the thumbs are valid.  False if there are two
	 * thumbs with the same value (this is not allowed)
	 */
	protected static boolean validatePositions(State state) {
		float[] p = state.positions;
		Object[] c = state.values;

		/** Don't let the user position a thumb outside of
		 * [0,1] if there are only 2 colors:
		 * colors outside [0,1] are deleted, and we can't delete
		 * colors so we get less than 2.
		 */
		if(p.length<=2) {
			/** Since the user can only manipulate 1 thumb at a time,
			 * only 1 thumb should be outside the domain of [0,1].
			 * So we *don't* have to reorganize c when we change p
			 */
			for(int a = 0; a<p.length; a++) {
				if(p[a]<0) {
					p[a] = 0;
				} else if(p[a]>1) {
					p[a] = 1;
				}
			}
		}

		//validate the new positions:
		boolean checkAgain = true;
		while(checkAgain) {
			checkAgain = false;
			for(int a = 0; a<p.length-1; a++) {
				if(p[a]==p[a+1])
					return false; //we can't make two equal
				if(p[a]>p[a+1]) {
					checkAgain = true;

					float swap1 = p[a];
					p[a] = p[a+1];
					p[a+1] = swap1;
					Object swap2 = c[a];
					c[a] = c[a+1];
					c[a+1] = swap2;

					if(a==state.selectedThumb) {
						state.selectedThumb = a+1;
					} else if(a+1==state.selectedThumb) {
						state.selectedThumb = a;
					}
				}
			}
		}

		return true;
	}

	FocusListener focusListener = new FocusListener() {
		public void focusLost(FocusEvent e) {
			Component c = (Component)e.getSource();
			if( getProperty(slider,"MultiThumbSlider.indicateComponent","true").toString().equals("true") ) {
				slider.setSelectedThumb(-1);
			}
			updateIndication();
			c.repaint();
		}
		public void focusGained(FocusEvent e) {
			Component c = (Component)e.getSource();
			int i = slider.getSelectedThumb(false);
			if(i==-1) {
				int direction = 1;
				if(slider.getOrientation()==MultiThumbSlider.VERTICAL)
					direction *= -1;
				if(slider.isInverted())
					direction *= -1;
				slider.setSelectedThumb( (direction==1) ? 0 : slider.getThumbCount()-1 );
			}
			updateIndication();
			c.repaint();
		}
	};

	/** This will try to add a thumb between index1 and index2.
	 * <P>This method will not add a thumb if there is already a very
	 * small distance between these two endpoints
	 * 
	 * @param index1
	 * @param index2
	 * @return true if a new thumb was added
	 */
	protected boolean addThumb(int index1,int index2) {
		float pos1 = 0;
		float pos2 = 1;
		int min;
		int max;
		if(index1<index2) {
			min = index1;
			max = index2;
		} else {
			min = index2;
			max = index1;
		}
		float[] positions = slider.getThumbPositions();
		if(min>=0)
			pos1 = positions[min];
		if(max<positions.length)
			pos2 = positions[max];

		if(pos2-pos1<.05)
			return false;

		float newPosition = (pos1+pos2)/2f;
		slider.setSelectedThumb(slider.addThumb(newPosition));


		return true;
	}

	KeyListener keyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if(slider.isEnabled()==false) return;

			if(e.getSource()!=slider)
				throw new RuntimeException("only install this UI on the GradientSlider it was constructed with");
			int i = slider.getSelectedThumb();
			int code = e.getKeyCode();
			int orientation = slider.getOrientation();
			if( i!=-1 &&
					(code==KeyEvent.VK_RIGHT || code==KeyEvent.VK_LEFT) &&
					orientation==MultiThumbSlider.HORIZONTAL && 
					e.getModifiers()==Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
				//insert a new thumb
				int i2;
				if( (code==KeyEvent.VK_RIGHT && slider.isInverted()==false) ||
						(code==KeyEvent.VK_LEFT && slider.isInverted()==true)) {
					i2 = i+1;
				} else {
					i2 = i-1;
				}
				addThumb(i,i2);
				e.consume();
				return;
			} else if( i!=-1 &&
					(code==KeyEvent.VK_UP || code==KeyEvent.VK_DOWN) &&
					orientation==MultiThumbSlider.VERTICAL && 
					e.getModifiers()==Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
				//insert a new thumb
				int i2;
				if( (code==KeyEvent.VK_UP && slider.isInverted()==false) ||
						(code==KeyEvent.VK_DOWN && slider.isInverted()==true)) {
					i2 = i+1;
				} else {
					i2 = i-1;
				}
				addThumb(i,i2);
				e.consume();
				return;
			} else if(code==KeyEvent.VK_DOWN && 
					orientation==MultiThumbSlider.HORIZONTAL &&
					i!=-1) {
				//popup up!
				int x = slider.isInverted() ?
						(int)(trackRect.x+trackRect.width*(1-slider.getThumbPositions()[i])) :
							(int)(trackRect.x+trackRect.width*slider.getThumbPositions()[i]);
						int y = trackRect.y+trackRect.height;
						if(slider.doPopup(x, y)) {
							e.consume();
							return;
						}
			} else if(code==KeyEvent.VK_RIGHT && 
					orientation==MultiThumbSlider.VERTICAL &&
					i!=-1) {
				//popup up!
				int y = slider.isInverted() ?
						(int)(trackRect.y+trackRect.height*slider.getThumbPositions()[i]) :
							(int)(trackRect.y+trackRect.height*(1-slider.getThumbPositions()[i]));
						int x = trackRect.x+trackRect.width;
						if(slider.doPopup(x, y)) {
							e.consume();
							return;
						}
			}
			if(i!=-1) {
				//move the selected thumb
				if(code==KeyEvent.VK_RIGHT || code==KeyEvent.VK_DOWN) {
					nudge(i,1);
					e.consume();
				} else if(code==KeyEvent.VK_LEFT || code==KeyEvent.VK_UP) {
					nudge(i,-1);
					e.consume();
				} else if(code==KeyEvent.VK_DELETE || code==KeyEvent.VK_BACK_SPACE) {
					if(slider.getThumbCount()>2) {
						slider.removeThumb(i);
						e.consume();
					}
				} else if(code==KeyEvent.VK_SPACE || code==KeyEvent.VK_ENTER) {
					slider.doDoubleClick(-1, -1);
				}
			}
		}

		public void keyReleased(KeyEvent e) {}

		public void keyTyped(KeyEvent e) {}
	};

	PropertyChangeListener propertyListener = new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent e) {
			String name = e.getPropertyName();
			if(name.equals(MultiThumbSlider.VALUES_PROPERTY) ||
					name.equals(MultiThumbSlider.ORIENTATION_PROPERTY) ||
					name.equals(MultiThumbSlider.INVERTED_PROPERTY)) {
				calculateGeometry();
				slider.repaint();
			} else if(name.equals(MultiThumbSlider.SELECTED_THUMB_PROPERTY) ||
					name.equals(MultiThumbSlider.PAINT_TICKS_PROPERTY)) {
				slider.repaint();
			} else if(name.equals("MultiThumbSlider.indicateComponent")) {
				setMouseInside(mouseInside);
				slider.repaint();
			}
		}

	};

	ComponentListener compListener = new ComponentListener() {

		public void componentHidden(ComponentEvent e) {}

		public void componentMoved(ComponentEvent e) {}

		public void componentResized(ComponentEvent e) {
			calculateGeometry();
			Component c = (Component)e.getSource();
			c.repaint();
		}

		public void componentShown(ComponentEvent e) {}
	};

	protected void updateIndication() {
		synchronized(MultiThumbSliderUI.this) {
			if(slider.isEnabled() && (slider.hasFocus() || mouseInside)) {
				indicationGoal = 1;
			} else {
				indicationGoal = 0;
			}

			if(getProperty(slider,"MultiThumbSlider.indicateComponent","true").equals("false")) {
				//always turn on the "indication", so controls are always visible
				indicationGoal = 1;
				if(slider.isVisible()==false) { //when the component isn't yet initialized
					indication = 1; //initialize it to fully indicated
				}
			}

			if(indication!=indicationGoal) {
				if(animatingThread==null || animatingThread.isAlive()==false) {
					animatingThread = new Thread(animatingRunnable);
					animatingThread.start();
				}
			}
		}
	}

	protected synchronized void calculateGeometry() {
		trackRect = calculateTrackRect();

		float[] pos = slider.getThumbPositions();

		if(thumbPositions.length!=pos.length) {
			thumbPositions = new int[pos.length];
			thumbIndications = new float[pos.length];
		}
		if(slider.getOrientation()==GradientSlider.HORIZONTAL) {
			for(int a = 0; a<thumbPositions.length; a++) {
				if(slider.isInverted()==false) {
					thumbPositions[a] = trackRect.x+(int)(trackRect.width*pos[a]);
				} else {
					thumbPositions[a] = trackRect.x+(int)(trackRect.width*(1-pos[a]));
				}
				thumbIndications[a] = 0;
			}
		} else {
			for(int a = 0; a<thumbPositions.length; a++) {
				if(slider.isInverted()) {
					thumbPositions[a] = trackRect.y+(int)(trackRect.height*pos[a]);
				} else {
					thumbPositions[a] = trackRect.y+(int)(trackRect.height*(1-pos[a]));
				}
				thumbIndications[a] = 0;
			}
		}
	}

	protected Rectangle calculateTrackRect() {
		Insets i = new Insets(5,5,5,5);
		int w, h;
		if(slider.getOrientation()==MultiThumbSlider.HORIZONTAL) {
			w = slider.getWidth()-i.left-i.right;
			h = Math.min(DEPTH, slider.getHeight()-i.top-i.bottom);
		} else {
			h = slider.getHeight()-i.top-i.bottom;
			w = Math.min(DEPTH, slider.getWidth()-i.left-i.right);
		}
		return new Rectangle(slider.getWidth()/2-w/2,slider.getHeight()/2-h/2, w, h);
	}

	private void nudge(int thumbIndex,int direction) {
		float pixelFraction;
		if(slider.getOrientation()==GradientSlider.HORIZONTAL) {
			pixelFraction = 1f/((float)trackRect.width);
		} else {
			pixelFraction = 1f/((float)trackRect.height);
		}
		if(direction<0)
			pixelFraction *= -1;
		if(slider.isInverted())
			pixelFraction *= -1;
		if(slider.getOrientation()==MultiThumbSlider.VERTICAL)
			pixelFraction *= -1;

		//repeat a couple of times: it's possible we'll nudge two values
		//so they're exactly equal, which will make validate() fail.
		//in that case: move the value ANOTHER nudge to the left/right
		//to really make a change.  But make sure we still respect the [0,1] limits.
		State state = new State();
		while(state.positions[thumbIndex]>=0 && state.positions[thumbIndex]<=1) {
			state.positions[thumbIndex]+=pixelFraction;
			if(validatePositions(state)) {
				state.install();
				return;
			}
		}
	}

	public void installUI(JComponent slider) {
		slider.addMouseListener(this);
		slider.addMouseMotionListener(this);
		slider.addFocusListener(focusListener);
		slider.addKeyListener(keyListener);
		slider.addComponentListener(compListener);
		slider.addPropertyChangeListener(propertyListener);
	}

	public void paint(Graphics g, JComponent slider2) {
		if(slider2!=slider)
			throw new RuntimeException("only use this UI on the GradientSlider it was constructed with");

		Graphics2D g2 = (Graphics2D)g;
		int w = slider.getWidth();
		int h = slider.getHeight();

		if(slider.isOpaque()) {
			g.setColor(slider.getBackground());
			g.fillRect(0,0,w,h);
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_OFF);
		paintTrack(g2);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		paintFocus(g2);
		paintThumbs(g2);
	}

	protected abstract void paintTrack(Graphics2D g);
	protected abstract void paintFocus(Graphics2D g);
	protected abstract void paintThumbs(Graphics2D g);

	public void uninstallUI(JComponent slider) {
		slider.removeMouseListener(this);
		slider.removeMouseMotionListener(this);
		slider.removeFocusListener(focusListener);
		slider.removeKeyListener(keyListener);
		slider.removeComponentListener(compListener);
		slider.removePropertyChangeListener(propertyListener);
		super.uninstallUI(slider);
	}
}
