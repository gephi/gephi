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
package org.gephi.ui.utils;

// Copied from org.netbeans.lib.profiler.ui
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

//Copied from org.netbeans.lib.profiler.ui
public final class UIUtils {

    public static final float ALTERNATE_ROW_DARKER_FACTOR = 0.96f;
    private static Color unfocusedSelBg;
    private static Color unfocusedSelFg;

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    /** Determines if current L&F is AquaLookAndFeel */
    public static boolean isAquaLookAndFeel() {
        // is current L&F some kind of AquaLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("Aqua"); //NOI18N
    }

    public static Color getDarker(Color c) {
        if (c.equals(Color.WHITE)) {
            return new Color(244, 244, 244);
        }

        return getSafeColor((int) (c.getRed() * ALTERNATE_ROW_DARKER_FACTOR), (int) (c.getGreen() * ALTERNATE_ROW_DARKER_FACTOR),
                (int) (c.getBlue() * ALTERNATE_ROW_DARKER_FACTOR));
    }

    public static Color getDarkerLine(Color c, float alternateRowDarkerFactor) {
        return getSafeColor((int) (c.getRed() * alternateRowDarkerFactor), (int) (c.getGreen() * alternateRowDarkerFactor),
                (int) (c.getBlue() * alternateRowDarkerFactor));
    }

    public static int getDefaultRowHeight() {
        return new JLabel("X").getPreferredSize().height + 2; //NOI18N
    }

    /** Determines if current L&F is GTKLookAndFeel */
    public static boolean isGTKLookAndFeel() {
        // is current L&F some kind of GTKLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("GTK"); //NOI18N
    }

    /** Determines if current L&F is Nimbus */
    public static boolean isNimbusLookAndFeel() {
        // is current L&F Nimbus?
        return UIManager.getLookAndFeel().getID().equals("Nimbus"); //NOI18N
    }

    /** Determines if current L&F is GTK using Nimbus theme */
    public static boolean isNimbusGTKTheme() {
        // is current L&F GTK using Nimbus theme?
        return isGTKLookAndFeel() && "nimbus".equals(Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Net/ThemeName")); //NOI18N
    }

    /** Determines if current L&F is Nimbus or GTK with Nimbus theme*/
    public static boolean isNimbus() {
        // is current L&F Nimbus or GTK with Nimbus theme?
        return isNimbusLookAndFeel() || isNimbusGTKTheme();
    }

    /** Determines if current L&F is MetalLookAndFeel */
    public static boolean isMetalLookAndFeel() {
        // is current L&F some kind of MetalLookAndFeel?
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
    /** Get the system-wide unfocused selection background color */
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
    /** Get the system-wide unfocused selection foreground color */
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

    /** Determines if current L&F is Windows Classic LookAndFeel */
    public static boolean isWindowsClassicLookAndFeel() {
        if (!isWindowsLookAndFeel()) {
            return false;
        }

        return (!isWindowsXPLookAndFeel() && !isWindowsVistaLookAndFeel());
    }

    /** Determines if current L&F is WindowsLookAndFeel */
    public static boolean isWindowsLookAndFeel() {
        // is current L&F some kind of WindowsLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("Windows"); //NOI18N
    }

    /** Determines if current L&F is Windows XP LookAndFeel */
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

    private static BufferedImage createComponentScreenshot(final Component component) {
        final BufferedImage[] result = new BufferedImage[1];

        final Runnable screenshotPerformer = new Runnable() {

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
