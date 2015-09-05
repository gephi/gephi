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
package org.gephi.ui.components.gradientslider;

import com.bric.swing.ColorPicker;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;

import javax.swing.JColorChooser;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComponentUI;

/** This component lets the user manipulate the colors in a gradient.
 * A <code>GradientSlider</code> can contain any number of thumbs.  The
 * slider itself represents a range of values from zero to one, so the thumbs
 * must always be within this range.  Each thumb maps to a specific <code>Color</code>.
 * <P>There are some specific properties you can set to customize the look-and-feel
 * of this slider in the default {@link org.gephi.ui.components.gradientslider.GradientSliderUI} class.
 * <P>The UI for each slider is loaded from the UIManager property: "GradientSliderUI".
 * By default this is "org.gephi.ui.components.gradientslider.GradientSliderUI".
 */
//Author Jeremy Wood
public class GradientSlider extends MultiThumbSlider {

    private static final long serialVersionUID = 1L;


    static {
        if (UIManager.getString("GradientSliderUI") == null) {
            UIManager.put("GradientSliderUI", "org.gephi.ui.components.gradientslider.GradientSliderUI");
        }
    }

    /** Create a horizontal <code>GradientSlider</code> that
     * represents a gradient from white to black.
     */
    public GradientSlider() {
        this(HORIZONTAL);
    }

    /** Create a <code>GradientSlider</code> that represents a
     * gradient form white to black.
     * @param orientation HORIZONTAL or VERTICAL
     */
    public GradientSlider(int orientation) {
        this(orientation, new float[]{0f, 1f}, new Color[]{Color.white, Color.black});
    }

    /** Create a new <code>GradientSlider</code>.
     *
     * @param orientation HORIZONTAL or VERTICAL
     * @param thumbPositions the initial positions of each thumb
     * @param values the initial colors at each position
     * @throws IllegalArgumentException if the number of elements in
     * <code>thumbPositions</code> does not equal the number of elements
     * in <code>values</code>.
     *
     */
    public GradientSlider(int orientation, float[] thumbPositions, Color[] values) {
        super(orientation, thumbPositions, values);
    }

    /** Returns the Color at the specified position.
     * @return color
     */
    @Override
    public Object getValue(float pos) {
        for (int a = 0; a < thumbPositions.length - 1; a++) {
            if (thumbPositions[a] == pos) {
                return values[a];
            }
            if (thumbPositions[a] < pos && pos < thumbPositions[a + 1]) {
                float v = (pos - thumbPositions[a]) / (thumbPositions[a + 1] - thumbPositions[a]);
                return tween((Color) values[a], (Color) values[a + 1], v);
            }
        }
        if (pos < thumbPositions[0]) {
            return (Color) values[0];
        }
        if (pos > thumbPositions[thumbPositions.length - 1]) {
            return (Color) values[values.length - 1];
        }
        return null;
    }

    /** This is identical to <code>getValues()</code>,
     * except the return value is an array of <code>Colors</code>.
     * @return color array
     */
    public Color[] getColors() {
        Color[] c = new Color[values.length];
        for (int a = 0; a < c.length; a++) {
            c[a] = (Color) values[a];
        }
        return c;
    }

    private static Color tween(Color c1, Color c2, float p) {
        return new Color(
                (int) (c1.getRed() * (1 - p) + c2.getRed() * (p)),
                (int) (c1.getGreen() * (1 - p) + c2.getGreen() * (p)),
                (int) (c1.getBlue() * (1 - p) + c2.getBlue() * (p)),
                (int) (c1.getAlpha() * (1 - p) + c2.getAlpha() * (p)));
    }

    /** This invokes a <code>ColorPicker</code> dialog to edit
     * the thumb at the selected index.
     *
     * @return return true if successful
     */
    @Override
    public boolean doDoubleClick(int x, int y) {
        int i = getSelectedThumb();
        if (i != -1) {
            showColorPicker();
            //showJColorChooser();
            SwingUtilities.invokeLater(new SelectThumbRunnable(i));
            return true;
        } else {
            return false;
        }
    }

    class SelectThumbRunnable implements Runnable {

        int index;

        public SelectThumbRunnable(int i) {
            index = i;
        }

        @Override
        public void run() {
            setSelectedThumb(index);
        }
    }
    /** The popup for contextual menus. */
    JPopupMenu popup;

    private JPopupMenu createPopup() {
        return new ColorPickerPopup();
    }

    abstract class AbstractPopup extends JPopupMenu {

        private static final long serialVersionUID = 1L;
        int lastSelectedThumb;
        PopupMenuListener popupMenuListener = new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                setValueIsAdjusting(false);
                SwingUtilities.invokeLater(new SelectThumbRunnable(lastSelectedThumb));
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                setValueIsAdjusting(false);
                SwingUtilities.invokeLater(new SelectThumbRunnable(lastSelectedThumb));
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                setValueIsAdjusting(true);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        getFocusableComponent().requestFocus();
                    }
                });
            }
        };

        public AbstractPopup() {
            addPopupMenuListener(popupMenuListener);
        }

        public abstract Component getFocusableComponent();

        @Override
        public void show(Component c, int x, int y) {
            Color[] colors = getColors();
            lastSelectedThumb = getSelectedThumb();
            if (lastSelectedThumb != -1) {
                setColor(colors[lastSelectedThumb]);
                super.show(c, x, y);
            }
        }

        public abstract void setColor(Color c);
    }

    class ColorPickerPopup extends AbstractPopup {

        private static final long serialVersionUID = 1L;
        ColorPicker mini;
        KeyListener commitListener = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ColorPickerPopup.this.setVisible(false);
                }
            }
        };

        public ColorPickerPopup() {
            super();
            boolean includeOpacity = MultiThumbSliderUI.getProperty(GradientSlider.this, "GradientSlider.includeOpacity", "true").equals("true");

            mini = new ColorPicker(false, includeOpacity);
            mini.setMode(0);
            mini.setPreferredSize(new Dimension(220, 200));
            PropertyChangeListener p = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    ColorPicker p = (ColorPicker) evt.getSource();
                    Color[] colors = getColors();
                    colors[lastSelectedThumb] = p.getColor();
                    setValues(getThumbPositions(), colors);
                }
            };
            mini.addPropertyChangeListener(ColorPicker.SELECTED_COLOR_PROPERTY, p);
            mini.addPropertyChangeListener(ColorPicker.OPACITY_PROPERTY, p);
            for (int a = 0; a < mini.getComponentCount(); a++) {
                Component c = mini.getComponent(a);
                c.addKeyListener(commitListener);
            }
            add(mini);
        }

        @Override
        public Component getFocusableComponent() {
            return mini.getColorPanel();
        }

        @Override
        public void setColor(Color c) {
            mini.setRGB(c.getRed(), c.getGreen(), c.getBlue());
        }
    }

    /** This shows a mini ColorPicker panel to let the user
     * change the selected color.
     * @return true if successful
     */
    @Override
    public boolean doPopup(int x, int y) {
        if (popup == null) {
            popup = createPopup();
        }
        popup.show(this, x, y);
        return true;
    }

    private Frame getFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof Frame) {
            return ((Frame) w);
        }
        return null;
    }

    private boolean showColorPicker() {
        Color[] colors = getColors();
        int i = getSelectedThumb();

        Frame frame = getFrame();

        boolean includeOpacity = MultiThumbSliderUI.getProperty(this, "GradientSlider.includeOpacity", "true").equals("true");
        colors[i] = ColorPicker.showDialog(frame, colors[i], includeOpacity);
        if (colors[i] != null) {
            setValues(getThumbPositions(), colors);
        }
        return true;
    }

    /** TODO: If developers don't want to bundle the ColorPicker with their programs,
     * they can use this method instead of <code>showColorPicker()</code>.
     */
    private void showJColorChooser() {
        Color[] colors = getColors();
        int i = getSelectedThumb();
        if (i >= 0 && i < colors.length) {
            colors[i] = JColorChooser.showDialog(this, "Choose a Color", colors[i]);
            if (colors[i] != null) {
                setValues(getThumbPositions(), colors);
            }
        }
    }

    @Override
    public void updateUI() {
        String name = UIManager.getString("GradientSliderUI");
        try {
            Class c = Class.forName(name);
            Constructor[] constructors = c.getConstructors();
            for (int a = 0; a < constructors.length; a++) {
                Class[] types = constructors[a].getParameterTypes();
                if (types.length == 1 && types[0].equals(GradientSlider.class)) {
                    ComponentUI ui = (ComponentUI) constructors[a].newInstance(new Object[]{this});
                    setUI(ui);
                    return;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class \"" + name + "\" could not be found.");
        } catch (Throwable t) {
            RuntimeException e = new RuntimeException("The class \"" + name + "\" could not be constructed.");
            e.initCause(t);
            throw e;
        }
    }
}
