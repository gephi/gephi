package org.gephi.a11yaudit;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.SwingUtilities;

/**
 * Keyboard walks: real Tab key events through the KeyboardFocusManager, recording where focus goes (WCAG 2.1.1,
 * 2.1.2, 2.4.3) and how much the focused component's look changes (2.4.7, 1.4.11).
 */
final class Focus {

    private static final int MARGIN = 4;
    private static final int STEP_MS = 120;

    private Focus() {
    }

    /**
     * Presses Tab from a start component until focus comes back to a component already visited, leaves the scope,
     * or stops moving, then presses Shift+Tab once from the start.
     */
    static Map<String, Object> walk(String label, Container scope, Component start, Tree tree, boolean images,
                                    int maxSteps) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scope", label);
        if (start != null) {
            Ui.edtRun(start::requestFocusInWindow);
            Ui.settle(300);
        }
        Component first = Ui.focusOwner();
        if (first == null || !inside(first, scope)) {
            result.put("outcome", "noFocus");
            result.put("focusOwner", first == null ? null : describe(first, tree));
            result.put("focusedWindow", Ui.focusedWindow() == null ? null
                : Ui.focusedWindow().getClass().getName());
            return result;
        }
        List<Map<String, Object>> steps = new ArrayList<>();
        Map<Component, Boolean> visited = new IdentityHashMap<>();
        Map<Component, Map<String, Object>> stepOf = new IdentityHashMap<>();
        Map<Component, BufferedImage> focusedImages = new IdentityHashMap<>();
        Component current = first;
        visited.put(current, true);
        steps.add(step(current, tree, stepOf));
        if (images) {
            focusedImages.put(current, Ui.paintAround(current, MARGIN));
        }
        String outcome = "limit";
        java.util.Set<java.awt.Window> windows = new java.util.HashSet<>(Ui.showingWindows());
        List<String> opened = new ArrayList<>();
        for (int i = 0; i < maxSteps; i++) {
            Ui.key(KeyEvent.VK_TAB, 0, '\t');
            Ui.settle(STEP_MS);
            for (java.awt.Window w : Ui.showingWindows()) {
                if (windows.add(w)) {
                    opened.add(w.getClass().getName());
                }
            }
            Component next = Ui.focusOwner();
            if (images && next != current) {
                compare(label, current, focusedImages.remove(current), stepOf.get(current));
            }
            if (next == null) {
                outcome = "lost";
                break;
            }
            if (next == current) {
                Map<String, Object> stuck = describe(current, tree);
                Component policyNext = policyNext(current);
                outcome = policyNext == null || policyNext == current ? "single" : "consumed";
                stuck.put("policyNext", policyNext == null || policyNext == current ? null
                    : describe(policyNext, tree));
                stuck.put("ctrlTab", ctrlTab(current, scope, tree));
                result.put("stuckAt", stuck);
                break;
            }
            if (!inside(next, scope)) {
                outcome = "left";
                result.put("leftTo", describe(next, tree));
                break;
            }
            if (visited.containsKey(next)) {
                outcome = "cycle";
                result.put("cycleBackTo", stepOf.get(next) == null ? null : stepOf.get(next).get("index"));
                break;
            }
            visited.put(next, true);
            Map<String, Object> s = step(next, tree, stepOf);
            s.put("index", steps.size());
            steps.add(s);
            if (images) {
                focusedImages.put(next, Ui.paintAround(next, MARGIN));
            }
            current = next;
        }
        if (images) {
            for (Map.Entry<Component, BufferedImage> e : focusedImages.entrySet()) {
                if (Ui.focusOwner() != e.getKey()) {
                    compare(label, e.getKey(), e.getValue(), stepOf.get(e.getKey()));
                }
            }
        }
        result.put("outcome", outcome);
        result.put("windowsOpenedByTab", opened);
        result.put("steps", steps);
        result.put("shiftTab", shiftTab(first, scope, tree));
        return result;
    }

    /**
     * Where the focus traversal policy says Tab should go from a component. When that is another component but
     * focus stayed, the component consumed Tab itself (tables, text areas).
     */
    private static Component policyNext(Component c) {
        return Ui.edt(() -> {
            Container root = c instanceof Container && ((Container) c).isFocusCycleRoot() ? (Container) c
                : c.getFocusCycleRootAncestor();
            if (root == null || root.getFocusTraversalPolicy() == null) {
                return null;
            }
            return root.getFocusTraversalPolicy().getComponentAfter(root, c);
        });
    }

    /**
     * Presses Ctrl+Tab, the standard way out of components that use Tab themselves. NetBeans may show its window
     * switcher instead, which is closed again.
     */
    private static Map<String, Object> ctrlTab(Component from, Container scope, Tree tree) {
        java.util.Set<java.awt.Window> before = new java.util.HashSet<>(Ui.showingWindows());
        Ui.key(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK, '\t');
        Ui.key(KeyEvent.VK_CONTROL, 0, KeyEvent.CHAR_UNDEFINED);
        Ui.settle(400);
        Map<String, Object> m = new LinkedHashMap<>();
        for (java.awt.Window w : Ui.showingWindows()) {
            if (!before.contains(w)) {
                m.put("opened", w.getClass().getName());
                Ui.key(KeyEvent.VK_ESCAPE, 0, KeyEvent.CHAR_UNDEFINED);
                Ui.settle(300);
            }
        }
        Component after = Ui.focusOwner();
        m.put("moved", after != null && after != from);
        m.put("to", after == null ? null : describe(after, tree));
        m.put("leftScope", after != null && !inside(after, scope));
        return m;
    }

    private static Map<String, Object> shiftTab(Component first, Container scope, Tree tree) {
        Ui.edtRun(first::requestFocusInWindow);
        Ui.settle(200);
        if (Ui.focusOwner() != first) {
            return null;
        }
        Ui.key(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK, '\t');
        Ui.settle(STEP_MS);
        Component prev = Ui.focusOwner();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("moved", prev != null && prev != first);
        m.put("inside", prev != null && inside(prev, scope));
        m.put("to", prev == null ? null : describe(prev, tree));
        return m;
    }

    private static boolean inside(Component c, Container scope) {
        return Ui.edt(() -> SwingUtilities.isDescendingFrom(c, scope));
    }

    private static Map<String, Object> step(Component c, Tree tree, Map<Component, Map<String, Object>> stepOf) {
        Map<String, Object> s = describe(c, tree);
        s.put("index", 0);
        s.put("visibleFraction", visibleFraction(c));
        stepOf.put(c, s);
        return s;
    }

    /**
     * How much of a focused component is inside its scroll panes and window: 0 when focus went to a control
     * scrolled out of view (WCAG 2.4.11).
     */
    private static Double visibleFraction(Component c) {
        return Ui.edt(() -> {
            if (c.getWidth() <= 0 || c.getHeight() <= 0) {
                // An empty table or list has no size; nothing to hide
                return null;
            }
            java.awt.Rectangle visible = new java.awt.Rectangle(0, 0, c.getWidth(), c.getHeight());
            Component child = c;
            for (Component p = c.getParent(); p != null && !(child instanceof java.awt.Window); p = p.getParent()) {
                visible.translate(child.getX(), child.getY());
                visible = visible.intersection(new java.awt.Rectangle(0, 0, p.getWidth(), p.getHeight()));
                if (visible.isEmpty()) {
                    return (Double) 0.0;
                }
                child = p;
            }
            return (Double) Tree.round((double) visible.width * visible.height / (c.getWidth() * c.getHeight()));
        });
    }

    static Map<String, Object> describe(Component c, Tree tree) {
        return Ui.edt(() -> {
            Map<String, Object> m = new LinkedHashMap<>();
            Integer id = tree.id(c);
            if (id != null) {
                m.put("node", id);
            }
            AccessibleContext ac = c.getAccessibleContext();
            AccessibleRole role = ac == null ? null : ac.getAccessibleRole();
            m.put("role", role == null ? null : role.toDisplayString(Locale.ENGLISH));
            m.put("name", ac == null ? null : ac.getAccessibleName());
            m.put("class", c.getClass().getName());
            String panel = Gephi.topComponentName(Gephi.topComponentOf(c));
            if (panel != null) {
                m.put("panel", panel);
            }
            return m;
        });
    }

    /**
     * Compares a component painted with and without focus: the number of pixels that change and how strongly.
     */
    private static void compare(String label, Component c, BufferedImage focused, Map<String, Object> step) {
        if (step == null || focused == null) {
            return;
        }
        BufferedImage unfocused = Ui.paintAround(c, MARGIN);
        if (unfocused == null || unfocused.getWidth() != focused.getWidth()
            || unfocused.getHeight() != focused.getHeight()) {
            step.put("focusIndicator", null);
            return;
        }
        List<Double> contrasts = new ArrayList<>();
        for (int y = 0; y < focused.getHeight(); y++) {
            for (int x = 0; x < focused.getWidth(); x++) {
                int a = focused.getRGB(x, y);
                int b = unfocused.getRGB(x, y);
                if (distance(a, b) > 24) {
                    contrasts.add(Contrast.ratio(a, b));
                }
            }
        }
        Collections.sort(contrasts);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("changedPixels", contrasts.size());
        m.put("perimeter", 2 * (c.getWidth() + c.getHeight()));
        if (!contrasts.isEmpty()) {
            m.put("medianContrast", Tree.round(contrasts.get(contrasts.size() / 2)));
            m.put("p90Contrast", Tree.round(contrasts.get((int) (contrasts.size() * 0.9))));
        }
        step.put("focusIndicator", m);
        if (contrasts.size() < (Integer) m.get("perimeter") / 2) {
            // Little or no visible change: keep both images as evidence for the reviewer
            String base = "focus/" + label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-") + "-"
                + step.get("index");
            m.put("evidence", base);
            try {
                java.io.File dir = new java.io.File(AuditAgent.outputDir(), "focus");
                dir.mkdirs();
                javax.imageio.ImageIO.write(focused, "png", new java.io.File(AuditAgent.outputDir(), base + "-focused.png"));
                javax.imageio.ImageIO.write(unfocused, "png",
                    new java.io.File(AuditAgent.outputDir(), base + "-unfocused.png"));
            } catch (java.io.IOException ex) {
                AuditAgent.log("focus evidence " + base + " failed: " + ex);
            }
        }
    }

    private static int distance(int a, int b) {
        return Math.abs(((a >> 16) & 0xff) - ((b >> 16) & 0xff)) + Math.abs(((a >> 8) & 0xff) - ((b >> 8) & 0xff))
            + Math.abs((a & 0xff) - (b & 0xff));
    }
}
