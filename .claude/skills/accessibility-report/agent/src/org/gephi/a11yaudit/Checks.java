package org.gephi.a11yaudit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * Keyboard operation of the controls a focus walk can reach but not use: the graph view (leaving it, shortcuts in
 * it), the Data Laboratory command buttons' popup menus, adding a filter without dragging and the Appearance colour
 * swatch. On Windows the graph view is a native window that Java events don't reach, so those keys are pressed with
 * java.awt.Robot (real key events), only while Gephi's graph view has the focus.
 */
final class Checks {

    private static final int MENU_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    private Checks() {
    }

    private static boolean extendsClass(Object o, String name) {
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            if (c.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static Component find(Container root, String classSuffix) {
        return Ui.edt(() -> {
            for (Component c : Ui.descendants(root)) {
                if (c.isShowing() && c.getClass().getName().endsWith(classSuffix)) {
                    return c;
                }
            }
            return null;
        });
    }

    // Graph view

    /**
     * Presses Tab, Shift+Tab, Ctrl+Tab and two shortcuts with the graph view focused and records where focus and
     * the window system end up.
     */
    static Map<String, Object> graphView(Window main, Component graphPanel, boolean robot) throws Exception {
        Map<String, Object> m = new LinkedHashMap<>();
        Component canvas = find((Container) graphPanel, "NewtCanvasAWT");
        if (canvas == null) {
            m.put("error", "no graph view canvas");
            return m;
        }
        Object newtChild = Gephi.call(canvas, "getNEWTChild");
        m.put("robot", robot);
        String menu = KeyEvent.getModifiersExText(MENU_KEY);
        Object[][] keys = {
            {"Tab", KeyEvent.VK_TAB, 0},
            {"Shift+Tab", KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK},
            {"Ctrl+Tab", KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK},
            {menu + "+2", KeyEvent.VK_2, MENU_KEY},
            {menu + "+Shift+L", KeyEvent.VK_L, MENU_KEY | InputEvent.SHIFT_DOWN_MASK},
        };
        String overview = selectedPerspective();
        rememberWindows();
        List<Map<String, Object>> results = new ArrayList<>();
        for (Object[] key : keys) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("key", key[0]);
            Gephi.selectPerspective(overview);
            Gephi.requestActive(graphPanel);
            Ui.settle(800);
            Ui.edtRun(canvas::requestFocus);
            Ui.settle(800);
            boolean before = inCanvas(canvas, newtChild);
            r.put("focusInGraphViewBefore", before);
            if (!before) {
                // Never press real keys when the graph view doesn't have the focus: they would go to another app
                r.put("skipped", true);
                results.add(r);
                continue;
            }
            String panelBefore = activatedPanel();
            press((Integer) key[1], (Integer) key[2], robot, canvas);
            Ui.settle(1500);
            Component owner = Ui.focusOwner();
            r.put("stillInGraphView", inCanvas(canvas, newtChild));
            r.put("focusOwner", owner == null ? null : owner.getClass().getName());
            r.put("focusOwnerName", owner == null ? null : Ui.edt(() -> owner.getAccessibleContext() == null ? null
                : owner.getAccessibleContext().getAccessibleName()));
            r.put("perspectiveChanged", !overview.equals(selectedPerspective()));
            String panelAfter = activatedPanel();
            r.put("activatedPanel", panelAfter);
            r.put("panelChanged", panelAfter != null && !panelAfter.equals(panelBefore));
            closeNewWindows();
            results.add(r);
        }
        Gephi.selectPerspective(overview);
        Ui.settle(1500);
        m.put("keys", results);
        return m;
    }

    private static boolean inCanvas(Component canvas, Object newtChild) {
        if (Ui.focusOwner() == canvas) {
            return true;
        }
        try {
            return Boolean.TRUE.equals(Gephi.call(newtChild, "hasFocus"));
        } catch (Exception ex) {
            return false;
        }
    }

    private static void press(int keyCode, int modifiers, boolean robot, Component canvas) throws Exception {
        if (!robot) {
            Ui.key(keyCode, modifiers, keyCode == KeyEvent.VK_TAB ? '\t' : KeyEvent.CHAR_UNDEFINED);
            for (int mod : new int[] {KeyEvent.VK_CONTROL, KeyEvent.VK_META, KeyEvent.VK_SHIFT}) {
                Ui.key(mod, 0, KeyEvent.CHAR_UNDEFINED);
            }
            return;
        }
        Robot r = new Robot();
        r.setAutoDelay(60);
        List<Integer> mods = new ArrayList<>();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            mods.add(KeyEvent.VK_CONTROL);
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
            mods.add(KeyEvent.VK_META);
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            mods.add(KeyEvent.VK_SHIFT);
        }
        for (int mod : mods) {
            r.keyPress(mod);
        }
        r.keyPress(keyCode);
        r.keyRelease(keyCode);
        for (int i = mods.size() - 1; i >= 0; i--) {
            r.keyRelease(mods.get(i));
        }
    }

    private static String selectedPerspective() throws Exception {
        Object pc = Gephi.lookup("org.gephi.perspective.api.PerspectiveController");
        return (String) Gephi.call(Gephi.call(pc, "getSelectedPerspective"), "getName");
    }

    private static String activatedPanel() throws Exception {
        Object wm = Gephi.callStatic("org.openide.windows.WindowManager", "getDefault");
        Object tc = Gephi.call(Gephi.call(wm, "getRegistry"), "getActivated");
        return tc == null ? null : Gephi.topComponentName((Component) tc);
    }

    private static final Set<Window> KNOWN = new HashSet<>();

    static void rememberWindows() {
        KNOWN.clear();
        KNOWN.addAll(Ui.showingWindows());
    }

    /**
     * Closes what a key opened (e.g. the NetBeans window switcher) with Escape, or disposes it.
     */
    private static List<String> closeNewWindows() {
        List<String> closed = new ArrayList<>();
        for (Window w : Ui.showingWindows()) {
            if (!KNOWN.contains(w) && !(w instanceof java.awt.Frame)) {
                closed.add(w.getClass().getName());
                Ui.key(KeyEvent.VK_ESCAPE, 0, KeyEvent.CHAR_UNDEFINED);
                Ui.settle(300);
                if (Ui.edt(w::isShowing)) {
                    Ui.edtRun(w::dispose);
                }
            }
        }
        return closed;
    }

    // Data Laboratory command buttons

    /**
     * For each Flamingo command button that has a popup menu: focus it, press Space (then Enter, then Down), and
     * check that the menu opens, takes the focus and can be moved through with the arrow keys.
     */
    static List<Map<String, Object>> commandPopups(Container panel) throws Exception {
        List<Component> buttons = Ui.edt(() -> {
            List<Component> list = new ArrayList<>();
            for (Component c : Ui.descendants(panel)) {
                if (c.isShowing() && c.isEnabled()
                    && extendsClass(c, "org.pushingpixels.flamingo.api.common.JCommandButton")) {
                    list.add(c);
                }
            }
            return list;
        });
        List<Map<String, Object>> results = new ArrayList<>();
        for (Component b : buttons) {
            String kind = String.valueOf(Gephi.call(b, "getCommandButtonKind"));
            if (!kind.contains("POPUP")) {
                continue;
            }
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("button", Ui.plain(Ui.visibleText(b)));
            r.put("name", Ui.edt(() -> b.getAccessibleContext().getAccessibleName()));
            r.put("kind", kind);
            Ui.edtRun(b::requestFocusInWindow);
            Ui.settle(300);
            r.put("focusable", Ui.focusOwner() == b);
            String openedWith = null;
            if (Ui.focusOwner() == b) {
                for (Object[] key : new Object[][] {{"Space", KeyEvent.VK_SPACE, ' '},
                    {"Enter", KeyEvent.VK_ENTER, '\n'}, {"Down", KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED}}) {
                    Ui.key((Integer) key[1], 0, (Character) key[2]);
                    Ui.settle(700);
                    if (popupShown()) {
                        openedWith = (String) key[0];
                        break;
                    }
                }
            }
            r.put("opensWith", openedWith);
            if (openedWith == null) {
                // Control: open it the way a click does, to show the check can see the menu when it is open
                Ui.edtRun(() -> Gephi.call(b, "doPopupClick"));
                Ui.settle(700);
                r.put("menuDetectedWhenClicked", popupShown());
                hidePopups();
            }
            if (openedWith != null) {
                Component focusBefore = Ui.focusOwner();
                r.put("focusInMenu", focusBefore != null && focusBefore != b);
                Ui.key(KeyEvent.VK_DOWN, 0, KeyEvent.CHAR_UNDEFINED);
                Ui.settle(400);
                Component focusAfter = Ui.focusOwner();
                r.put("arrowMovesFocus", focusAfter != null && focusAfter != focusBefore && focusAfter != b);
                r.put("menuItems", popupItems());
                Ui.key(KeyEvent.VK_ESCAPE, 0, KeyEvent.CHAR_UNDEFINED);
                Ui.settle(500);
                r.put("escapeCloses", !popupShown());
                hidePopups();
            }
            results.add(r);
        }
        return results;
    }

    private static Object popupManager() throws Exception {
        return Gephi.callStatic("org.pushingpixels.flamingo.api.common.popup.PopupPanelManager", "defaultManager");
    }

    /**
     * Whether a Flamingo popup panel or a Swing popup menu is open.
     */
    private static boolean popupShown() {
        try {
            if (!((List<?>) Gephi.call(popupManager(), "getShownPath")).isEmpty()) {
                return true;
            }
        } catch (Exception ex) {
            // No Flamingo in this release
        }
        return Ui.edt(() -> {
            if (javax.swing.MenuSelectionManager.defaultManager().getSelectedPath().length > 0) {
                return true;
            }
            for (Window w : Window.getWindows()) {
                for (Component c : Ui.descendants(w)) {
                    if (c instanceof javax.swing.JPopupMenu && c.isShowing()) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /**
     * Names of the items in the open popup, as assistive technology would get them.
     */
    private static List<String> popupItems() {
        try {
            List<?> path = (List<?>) Gephi.call(popupManager(), "getShownPath");
            Component panel = (Component) Gephi.call(path.get(path.size() - 1), "getPopupPanel");
            return Ui.edt(() -> {
                List<String> items = new ArrayList<>();
                for (Component c : Ui.descendants((Container) panel)) {
                    AccessibleContext ac = c.getAccessibleContext();
                    AccessibleRole role = ac == null ? null : ac.getAccessibleRole();
                    if (c.isShowing() && role != null && (role == AccessibleRole.PUSH_BUTTON
                        || role == AccessibleRole.MENU_ITEM || role == AccessibleRole.CHECK_BOX)) {
                        items.add(role.toDisplayString(Locale.ENGLISH) + ": " + ac.getAccessibleName());
                    }
                }
                return items;
            });
        } catch (Exception ex) {
            return List.of("unreadable: " + ex);
        }
    }

    private static void hidePopups() {
        try {
            Ui.edtRun(() -> {
                javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
                Gephi.call(popupManager(), "hidePopups", (Object) null);
            });
            Ui.settle(300);
        } catch (Exception ex) {
            AuditAgent.log("hide popups failed: " + ex);
        }
    }

    // Filters

    /**
     * Selects Degree Range in the filter library and presses Enter: the filter should be added to the queries,
     * the keyboard alternative to dragging it (WCAG 2.1.1, 2.5.7).
     */
    static Map<String, Object> filterEnter(Container filtersPanel, String filterName) throws Exception {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("filter", filterName);
        JTree tree = Ui.edt(() -> {
            for (Component c : Ui.descendants(filtersPanel)) {
                if (c instanceof JTree && c.isShowing()) {
                    return (JTree) c;
                }
            }
            return null;
        });
        if (tree == null) {
            r.put("error", "no filter library tree");
            return r;
        }
        // The library's categories load their children lazily: expand, wait, look again
        Integer row = null;
        List<String> seen = new ArrayList<>();
        for (int pass = 0; pass < 5 && row == null; pass++) {
            row = Ui.edt(() -> {
                seen.clear();
                for (int i = 0; i < tree.getRowCount() && i < 500; i++) {
                    TreePath path = tree.getPathForRow(i);
                    Component rendered = tree.getCellRenderer().getTreeCellRendererComponent(tree,
                        path.getLastPathComponent(), false, tree.isExpanded(i), false, i, false);
                    String text = Ui.plain(Ui.visibleText(rendered));
                    seen.add(text);
                    if (text != null && text.equalsIgnoreCase(filterName)) {
                        return i;
                    }
                }
                for (int i = tree.getRowCount() - 1; i >= 0; i--) {
                    tree.expandRow(i);
                }
                return null;
            });
            if (row == null) {
                Ui.settle(1000);
            }
        }
        if (row == null) {
            r.put("error", filterName + " not found in the library");
            r.put("rowsSeen", new ArrayList<>(seen.subList(0, Math.min(40, seen.size()))));
            return r;
        }
        final int found = row;
        Object model = Gephi.call(Gephi.lookup("org.gephi.filters.api.FilterController"), "getModel");
        int before = ((Object[]) Gephi.call(model, "getQueries")).length;
        rememberWindows();
        Ui.edtRun(() -> {
            tree.setSelectionRow(found);
            tree.scrollRowToVisible(found);
            tree.requestFocusInWindow();
        });
        Ui.settle(400);
        r.put("treeFocused", Ui.focusOwner() == tree);
        Ui.key(KeyEvent.VK_ENTER, 0, '\n');
        Ui.settle(1500);
        int after = ((Object[]) Gephi.call(model, "getQueries")).length;
        r.put("queriesBefore", before);
        r.put("queriesAfter", after);
        r.put("addedWithEnter", after > before);
        r.put("windowsOpened", closeNewWindows());
        return r;
    }

    // Appearance colour swatch

    /**
     * Focuses the colour swatch of the Appearance panel and presses Space, then Enter: does a colour picker open?
     */
    static Map<String, Object> colorSwatch(Container appearancePanel) {
        Map<String, Object> r = new LinkedHashMap<>();
        Component swatch = find(appearancePanel, "ColorChooser");
        if (swatch == null) {
            r.put("error", "no colour swatch");
            return r;
        }
        r.put("class", swatch.getClass().getName());
        Ui.edtRun(swatch::requestFocusInWindow);
        Ui.settle(300);
        r.put("focusable", Ui.focusOwner() == swatch);
        String opened = null;
        if (Ui.focusOwner() == swatch) {
            for (Object[] key : new Object[][] {{"Space", KeyEvent.VK_SPACE, ' '}, {"Enter", KeyEvent.VK_ENTER, '\n'}}) {
                rememberWindows();
                Ui.key((Integer) key[1], 0, (Character) key[2]);
                Ui.settle(800);
                List<String> windows = closeNewWindows();
                if (!windows.isEmpty() || popupShown()) {
                    opened = key[0] + " opens " + (windows.isEmpty() ? "a popup" : windows.get(0));
                    hidePopups();
                    break;
                }
            }
        }
        r.put("opensPicker", opened);
        return r;
    }
}
