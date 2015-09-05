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
package org.gephi.ui.utils;

// Copied from org.netbeans.lib.profiler.ui
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.table.JTableHeader;

//Copied from org.netbeans.lib.profiler.ui
public final class UIUtils {

    public static final float ALTERNATE_ROW_DARKER_FACTOR = 0.96f;
    private static Color unfocusedSelBg;
    private static Color unfocusedSelFg;

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    /**
     * Determines if current Look and Feel is AquaLookAndFeel.
     *
     * @return true if aqua look and feel
     */
    public static boolean isAquaLookAndFeel() {
        // is current Look and Feel some kind of AquaLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("Aqua"); //NOI18N
    }

    public static Color getDarker(Color c) {
        if (c.equals(Color.WHITE)) {
            return new Color(244, 244, 244);
        }

        return getSafeColor((int) (c.getRed() * ALTERNATE_ROW_DARKER_FACTOR), (int) (c.getGreen() * ALTERNATE_ROW_DARKER_FACTOR),
                (int) (c.getBlue() * ALTERNATE_ROW_DARKER_FACTOR));
    }

    public static Color getForegroundColorForBackground(Color background) {
        return (background.getRed() < 100 || background.getGreen() < 100 || background.getRed() < 100) ? Color.white : Color.black;
    }

    public static Color getDarkerLine(Color c, float alternateRowDarkerFactor) {
        return getSafeColor((int) (c.getRed() * alternateRowDarkerFactor), (int) (c.getGreen() * alternateRowDarkerFactor),
                (int) (c.getBlue() * alternateRowDarkerFactor));
    }

    public static int getDefaultRowHeight() {
        return new JLabel("X").getPreferredSize().height + 2; //NOI18N
    }

    /**
     * Determines if current Look and Feel is GTKLookAndFeel.
     * @return true if gtk look and feel
     */
    public static boolean isGTKLookAndFeel() {
        // is current Look and Feel some kind of GTKLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("GTK"); //NOI18N
    }

    /**
     * Determines if current Look and Feel is Nimbus.
     * @return true if nimbus look and feel
     */
    public static boolean isNimbusLookAndFeel() {
        // is current Look and Feel Nimbus?
        return UIManager.getLookAndFeel().getID().equals("Nimbus"); //NOI18N
    }

    /**
     * Determines if current Look and Feel is GTK using Nimbus theme.
     * @return true if nimbus gtk theme
     */
    public static boolean isNimbusGTKTheme() {
        // is current Look and Feel GTK using Nimbus theme?
        return isGTKLookAndFeel() && "nimbus".equals(Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Net/ThemeName")); //NOI18N
    }

    /**
     * Determines if current Look and Feel is Nimbus or GTK with Nimbus theme.
     * @return true if nimbus
     */
    public static boolean isNimbus() {
        // is current Look and Feel Nimbus or GTK with Nimbus theme?
        return isNimbusLookAndFeel() || isNimbusGTKTheme();
    }

    /**
     * Determines if current Look and Feel is MetalLookAndFeel.
     * @return true if metal look and feel
     */
    public static boolean isMetalLookAndFeel() {
        // is current Look and Feel some kind of MetalLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("Metal"); //NOI18N
    }

    // Returns next enabled tab of JTabbedPane
    public static int getNextSubTabIndex(JTabbedPane tabs, int tabIndex) {
        int nextTabIndex = tabIndex;

        for (int i = 0; i < tabs.getComponentCount(); i++) {
            nextTabIndex++;

            if (nextTabIndex == tabs.getComponentCount()) {
                nextTabIndex = 0;
            }

            if (tabs.isEnabledAt(nextTabIndex)) {
                break;
            }
        }

        return nextTabIndex;
    }

    public static Window getParentWindow(Component comp) {
        while ((comp != null) && !(comp instanceof Window)) {
            comp = comp.getParent();
        }

        return (Window) comp;
    }

    // Returns previous enabled tab of JTabbedPane
    public static int getPreviousSubTabIndex(JTabbedPane tabs, int tabIndex) {
        int previousTabIndex = tabIndex;

        for (int i = 0; i < tabs.getComponentCount(); i++) {
            previousTabIndex--;

            if (previousTabIndex < 0) {
                previousTabIndex = tabs.getComponentCount() - 1;
            }

            if (tabs.isEnabledAt(previousTabIndex)) {
                break;
            }
        }

        return previousTabIndex;
    }

    public static Color getSafeColor(int red, int green, int blue) {
        red = Math.max(red, 0);
        red = Math.min(red, 255);
        green = Math.max(green, 0);
        green = Math.min(green, 255);
        blue = Math.max(blue, 0);
        blue = Math.min(blue, 255);

        return new Color(red, green, blue);
    }

    // Copied from org.openide.awt.HtmlLabelUI
    /**
     * Get the system-wide unfocused selection background color.
     * @return unfocused selection background
     */
    public static Color getUnfocusedSelectionBackground() {
        if (unfocusedSelBg == null) {
            //allow theme/ui custom definition
            unfocusedSelBg = UIManager.getColor("nb.explorer.unfocusedSelBg"); //NOI18N

            if (unfocusedSelBg == null) {
                //try to get standard shadow color
                unfocusedSelBg = UIManager.getColor("controlShadow"); //NOI18N

                if (unfocusedSelBg == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    unfocusedSelBg = Color.lightGray;
                }

                //Lighten it a bit because disabled text will use controlShadow/
                //gray
                if (!Color.WHITE.equals(unfocusedSelBg.brighter())) {
                    unfocusedSelBg = unfocusedSelBg.brighter();
                }
            }
        }

        return unfocusedSelBg;
    }

    // Copied from org.openide.awt.HtmlLabelUI
    /**
     * Get the system-wide unfocused selection foreground color.
     * @return unfocused selection foreground
     */
    public static Color getUnfocusedSelectionForeground() {
        if (unfocusedSelFg == null) {
            //allow theme/ui custom definition
            unfocusedSelFg = UIManager.getColor("nb.explorer.unfocusedSelFg"); //NOI18N

            if (unfocusedSelFg == null) {
                //try to get standard shadow color
                unfocusedSelFg = UIManager.getColor("textText"); //NOI18N

                if (unfocusedSelFg == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    unfocusedSelFg = Color.BLACK;
                }
            }
        }

        return unfocusedSelFg;
    }
    private static Color profilerResultsBackground;

    private static Color getGTKProfilerResultsBackground() {
        int[] pixels = new int[1];
        pixels[0] = -1;

        // Prepare textarea to grab the color from
        JTextArea textArea = new JTextArea();
        textArea.setSize(new Dimension(10, 10));
        textArea.doLayout();

        // Print the textarea to an image
        Image image = new BufferedImage(textArea.getSize().width, textArea.getSize().height, BufferedImage.TYPE_INT_RGB);
        textArea.printAll(image.getGraphics());

        // Grab appropriate pixels to get the color
        PixelGrabber pixelGrabber = new PixelGrabber(image, 5, 5, 1, 1, pixels, 0, 1);
        try {
            pixelGrabber.grabPixels();
            if (pixels[0] == -1) {
                return Color.WHITE; // System background not customized
            }
        } catch (InterruptedException e) {
            return getNonGTKProfilerResultsBackground();
        }

        return pixels[0] != -1 ? new Color(pixels[0]) : getNonGTKProfilerResultsBackground();
    }

    private static Color getNonGTKProfilerResultsBackground() {
        return UIManager.getColor("Table.background"); // NOI18N
    }

    public static Color getProfilerResultsBackground() {
        if (profilerResultsBackground == null) {
            if (isGTKLookAndFeel() || isNimbusLookAndFeel()) {
                profilerResultsBackground = getGTKProfilerResultsBackground();
            } else {
                profilerResultsBackground = getNonGTKProfilerResultsBackground();
            }
            if (profilerResultsBackground == null) {
                profilerResultsBackground = Color.WHITE;
            }
        }

        return profilerResultsBackground;
    }

    /**
     * Determines if current Look and Feel is Windows Classic LookAndFeel.
     * @return true if windows classic look and feel
     */
    public static boolean isWindowsClassicLookAndFeel() {
        if (!isWindowsLookAndFeel()) {
            return false;
        }

        return (!isWindowsXPLookAndFeel() && !isWindowsVistaLookAndFeel());
    }

    /**
     * Determines if current Look and Feel is WindowsLookAndFeel.
     * @return true if windows look and feel
     */
    public static boolean isWindowsLookAndFeel() {
        // is current Look and Feel some kind of WindowsLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("Windows"); //NOI18N
    }

    /**
     * Determines if current Look and Feel is Windows XP LookAndFeel.
     * @return true if windows xp look and feel
     */
    public static boolean isWindowsXPLookAndFeel() {
        if (!isWindowsLookAndFeel()) {
            return false;
        }

        // is XP theme active in the underlying OS?
        boolean xpThemeActiveOS = Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive")); //NOI18N
        // is XP theme disabled by the application?

        boolean xpThemeDisabled = (System.getProperty("swing.noxp") != null); // NOI18N

        boolean vistaOs = System.getProperty("os.version").startsWith("6.0");

        return ((xpThemeActiveOS) && (!xpThemeDisabled) && !vistaOs);
    }

    /**
     * Determines if current Look and Feel is Windows XP LookAndFeel.
     * @return true if windows vista look and feel
     */
    public static boolean isWindowsVistaLookAndFeel() {
        if (!isWindowsLookAndFeel()) {
            return false;
        }

        // is XP theme active in the underlying OS?
        boolean xpThemeActiveOS = Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive")); //NOI18N
        // is XP theme disabled by the application?

        boolean xpThemeDisabled = (System.getProperty("swing.noxp") != null); // NOI18N

        boolean vistaOs = System.getProperty("os.version").startsWith("6.0");

        return ((xpThemeActiveOS) && (!xpThemeDisabled) && vistaOs);
    }

    // Classic Windows LaF doesn't draw dotted focus rectangle inside JButton if parent is JToolBar,
    // XP Windows LaF doesn't draw dotted focus rectangle inside JButton at all
    // This method installs customized Windows LaF that draws dotted focus rectangle inside JButton always
    // On JDK 1.5 the XP Windows LaF enforces special border to all buttons, overriding any custom border
    // set by setBorder(). Class responsible for this is WindowsButtonListener. See Issue 71546.
    // Also fixes buttons size in JToolbar.
    /**
     * Ensures that focus will be really painted if button is focused and fixes
     * using custom border for JDK 1.5 and XP LaF
     * @param button button
     */
    public static void fixButtonUI(AbstractButton button) {
        // JButton
        if (button.getUI() instanceof com.sun.java.swing.plaf.windows.WindowsButtonUI) {
            button.setUI(new com.sun.java.swing.plaf.windows.WindowsButtonUI() {

                @Override
                protected BasicButtonListener createButtonListener(AbstractButton b) {
                    return new BasicButtonListener(b); // Fix for  Issue 71546
                }

                @Override
                protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect,
                        Rectangle iconRect) {
                    int width = b.getWidth();
                    int height = b.getHeight();
                    g.setColor(getFocusColor());
                    javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
                            width - dashedRectGapWidth,
                            height - dashedRectGapHeight);
                }
            });
        } // JToggleButton
        else if (button.getUI() instanceof com.sun.java.swing.plaf.windows.WindowsToggleButtonUI) {
            button.setUI(new com.sun.java.swing.plaf.windows.WindowsToggleButtonUI() {

                @Override
                protected BasicButtonListener createButtonListener(AbstractButton b) {
                    return new BasicButtonListener(b); // Fix for  Issue 71546
                }

                @Override
                protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect,
                        Rectangle iconRect) {
                    int width = b.getWidth();
                    int height = b.getHeight();
                    g.setColor(getFocusColor());
                    javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
                            width - dashedRectGapWidth,
                            height - dashedRectGapHeight);
                }
            });
        }
    }

    private static BufferedImage createTableScreenshot(Component component) {
        Component source;
        Dimension sourceSize;
        JTable table;

        if (component instanceof JTable) {
            table = (JTable) component;

            if ((table.getTableHeader() == null) || !table.getTableHeader().isVisible()) {
                return createGeneralComponentScreenshot(component);
            }

            source = table;
            sourceSize = table.getSize();
        } else if (component instanceof JViewport && ((JViewport) component).getView() instanceof JTable) {
            JViewport viewport = (JViewport) component;
            table = (JTable) viewport.getView();

            if ((table.getTableHeader() == null) || !table.getTableHeader().isVisible()) {
                return createGeneralComponentScreenshot(component);
            }

            if (table.getSize().height > viewport.getSize().height) {
                source = viewport;
                sourceSize = viewport.getSize();
            } else {
                source = table;
                sourceSize = table.getSize();
            }
        } else {
            throw new IllegalArgumentException("Component can only be JTable or JViewport holding JTable"); // NOI18N
        }

        final JTableHeader tableHeader = table.getTableHeader();
        Dimension tableHeaderSize = tableHeader.getSize();

        BufferedImage tableScreenshot = new BufferedImage(sourceSize.width, tableHeaderSize.height + sourceSize.height,
                BufferedImage.TYPE_INT_RGB);
        final Graphics tableScreenshotGraphics = tableScreenshot.getGraphics();

        // Component.printAll has to run in AWT Thread to print component contents correctly
        if (SwingUtilities.isEventDispatchThread()) {
            tableHeader.printAll(tableScreenshotGraphics);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        tableHeader.printAll(tableScreenshotGraphics);
                    }
                });
            } catch (Exception e) {
            }
        }

        tableScreenshotGraphics.translate(0, tableHeaderSize.height);

        final Component printSrc = source;

        // Component.printAll has to run in AWT Thread to print component contents correctly
        if (SwingUtilities.isEventDispatchThread()) {
            printSrc.printAll(tableScreenshotGraphics);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        printSrc.printAll(tableScreenshotGraphics);
                    }
                });
            } catch (Exception e) {
            }
        }

        return tableScreenshot;
    }

    private static BufferedImage createGeneralComponentScreenshot(Component component) {
        Component source;
        Dimension sourceSize;

        if (component instanceof JViewport) {
            JViewport viewport = (JViewport) component;
            Component contents = viewport.getView();

            if (contents.getSize().height > viewport.getSize().height) {
                source = component;
                sourceSize = component.getSize();
            } else {
                source = contents;
                sourceSize = contents.getSize();
            }
        } else {
            source = component;
            sourceSize = component.getSize();
        }

        BufferedImage componentScreenshot = new BufferedImage(sourceSize.width, sourceSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics componentScreenshotGraphics = componentScreenshot.getGraphics();
        source.printAll(componentScreenshotGraphics);

        return componentScreenshot;
    }

    public static void runInEventDispatchThread(final Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public static void runInEventDispatchThreadAndWait(final Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static BufferedImage createComponentScreenshot(final Component component) {
        final BufferedImage[] result = new BufferedImage[1];

        final Runnable screenshotPerformer = new Runnable() {

            @Override
            public void run() {
                if (component instanceof JTable || (component instanceof JViewport && ((JViewport) component).getView() instanceof JTable)) {
                    result[0] = createTableScreenshot(component);
                } else {
                    result[0] = createGeneralComponentScreenshot(component);
                }
            }
        };

        try {
            if (SwingUtilities.isEventDispatchThread()) {
                screenshotPerformer.run();
            } else {
                SwingUtilities.invokeAndWait(screenshotPerformer);
            }
        } catch (Exception e) {
            return null;
        }

        return result[0];
    }
}
