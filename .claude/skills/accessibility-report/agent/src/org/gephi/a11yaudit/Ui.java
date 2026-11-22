package org.gephi.a11yaudit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Event Dispatch Thread, window, key and painting helpers. Everything that touches Swing goes through {@link #edt}.
 */
final class Ui {

    interface Task {
        void run() throws Exception;
    }

    private Ui() {
    }

    static <T> T edt(Callable<T> callable) {
        if (EventQueue.isDispatchThread()) {
            try {
                return callable.call();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        try {
            EventQueue.invokeAndWait(() -> {
                try {
                    result.set(callable.call());
                } catch (Throwable t) {
                    error.set(t);
                }
            });
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        if (error.get() != null) {
            throw new IllegalStateException(error.get());
        }
        return result.get();
    }

    static void edtRun(Task task) {
        edt(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Runs later on the EDT without waiting, for actions that open modal dialogs.
     */
    static void later(Task task) {
        EventQueue.invokeLater(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                AuditAgent.log("EDT task failed: " + t);
            }
        });
    }

    static void settle(long ms) {
        sleep(ms);
        edt(() -> null);
        edt(() -> null);
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    static List<Window> showingWindows() {
        return edt(() -> {
            List<Window> windows = new ArrayList<>();
            for (Window w : Window.getWindows()) {
                if (w.isShowing()) {
                    windows.add(w);
                }
            }
            return windows;
        });
    }

    static Window waitForNewWindow(Set<Window> before, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            for (Window w : showingWindows()) {
                if (!before.contains(w) && edt(() -> w.getWidth() > 50 && w.getHeight() > 50)) {
                    return w;
                }
            }
            sleep(200);
        }
        return null;
    }

    static boolean waitClosed(Window w, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (!edt(w::isShowing)) {
                return true;
            }
            sleep(200);
        }
        return false;
    }

    static Component focusOwner() {
        return edt(() -> KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
    }

    static Window focusedWindow() {
        return edt(() -> KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow());
    }

    /**
     * Posts a key press, typed (when the key has a character) and release to the focus owner, as the keyboard would.
     * The events go through the KeyboardFocusManager, so focus traversal keys are handled like real ones.
     */
    static void key(int keyCode, int modifiers, char keyChar) {
        Component owner = focusOwner();
        if (owner == null) {
            return;
        }
        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        long now = System.currentTimeMillis();
        queue.postEvent(new KeyEvent(owner, KeyEvent.KEY_PRESSED, now, modifiers, keyCode, keyChar));
        if (keyChar != KeyEvent.CHAR_UNDEFINED) {
            queue.postEvent(new KeyEvent(owner, KeyEvent.KEY_TYPED, now, modifiers, KeyEvent.VK_UNDEFINED, keyChar));
        }
        queue.postEvent(new KeyEvent(owner, KeyEvent.KEY_RELEASED, now + 5, modifiers, keyCode, keyChar));
    }

    /**
     * Visible text of a component: label and button text, or a public getText() on other components (e.g. Flamingo
     * command buttons). Null for text fields, whose text is content rather than a label.
     */
    static String visibleText(Component c) {
        if (c instanceof JTextComponent) {
            return null;
        }
        if (c instanceof JLabel) {
            return ((JLabel) c).getText();
        }
        if (c instanceof AbstractButton) {
            return ((AbstractButton) c).getText();
        }
        if (c instanceof JComponent) {
            try {
                Method m = c.getClass().getMethod("getText");
                if (m.getReturnType() == String.class && m.getParameterCount() == 0) {
                    return (String) m.invoke(c);
                }
            } catch (ReflectiveOperationException | RuntimeException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Text without HTML tags, mnemonic ampersands and trailing ellipsis, for comparisons.
     */
    static String plain(String s) {
        if (s == null) {
            return null;
        }
        String t = s.replaceAll("(?s)<[^>]*>", " ").replace("&nbsp;", " ").replace("&amp;", "&")
            .replaceAll("&(?=\\S)", "").replace("…", "").replaceAll("\\.\\.\\.$", "")
            .replaceAll("\\s+", " ").trim();
        return t;
    }

    static List<Component> descendants(Container root) {
        List<Component> list = new ArrayList<>();
        collect(root, list);
        return list;
    }

    private static void collect(Component c, List<Component> list) {
        list.add(c);
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                collect(child, list);
            }
        }
    }

    /**
     * Finds a showing, enabled button whose plain text or accessible name equals one of the texts, ignoring case.
     */
    static AbstractButton findButton(Container root, String... texts) {
        return edt(() -> {
            for (String text : texts) {
                for (Component c : descendants(root)) {
                    if (c instanceof AbstractButton && c.isShowing() && c.isEnabled()) {
                        AbstractButton b = (AbstractButton) c;
                        String name = b.getAccessibleContext().getAccessibleName();
                        if (text.equalsIgnoreCase(plain(b.getText())) || text.equalsIgnoreCase(plain(name))) {
                            return b;
                        }
                    }
                }
            }
            return null;
        });
    }

    /**
     * Finds a showing, enabled component with that visible text, including non-Swing buttons such as Flamingo
     * command buttons.
     */
    static Component findClickable(Container root, String text) {
        return edt(() -> {
            for (Component c : descendants(root)) {
                if (c.isShowing() && c.isEnabled() && text.equalsIgnoreCase(plain(visibleText(c)))
                    && !(c instanceof JLabel)) {
                    return c;
                }
            }
            return null;
        });
    }

    /**
     * Clicks a button, through doClick or Flamingo's doActionClick.
     */
    static void click(Component c) throws Exception {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).doClick();
            return;
        }
        for (String name : new String[] {"doActionClick", "doClick"}) {
            try {
                c.getClass().getMethod(name).invoke(c);
                return;
            } catch (NoSuchMethodException ex) {
                // Try the next one
            }
        }
        throw new IllegalStateException("can't click " + c.getClass().getName());
    }

    /**
     * Paints the area of the window around a component into an image, without a screen capture (no permission
     * needed). Heavyweight content like the OpenGL canvas is not included.
     */
    static BufferedImage paintAround(Component c, int margin) {
        return edt(() -> {
            JComponent root = SwingUtilities.getRootPane(c);
            if (root == null || !c.isShowing() || c.getWidth() <= 0 || c.getHeight() <= 0) {
                return null;
            }
            Rectangle r = SwingUtilities.convertRectangle(c.getParent(), c.getBounds(), root);
            r.grow(margin, margin);
            r = r.intersection(new Rectangle(0, 0, root.getWidth(), root.getHeight()));
            if (r.isEmpty()) {
                return null;
            }
            BufferedImage img = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.translate(-r.x, -r.y);
            g.setClip(r);
            root.paint(g);
            g.dispose();
            return img;
        });
    }

    static String screenshot(Window w, File dir, String name) {
        BufferedImage img = edt(() -> {
            Container root = SwingUtilities.getRootPane(w);
            if (root == null) {
                root = w;
            }
            if (root.getWidth() <= 0 || root.getHeight() <= 0) {
                return null;
            }
            BufferedImage image = new BufferedImage(root.getWidth(), root.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            root.paint(g);
            g.dispose();
            return image;
        });
        if (img == null) {
            return null;
        }
        File file = new File(dir, name + ".png");
        try {
            ImageIO.write(img, "png", file);
        } catch (IOException ex) {
            AuditAgent.log("screenshot " + name + " failed: " + ex);
            return null;
        }
        return file.getName();
    }
}
