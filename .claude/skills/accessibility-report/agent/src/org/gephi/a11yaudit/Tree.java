package org.gephi.a11yaudit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRelation;
import javax.accessibility.AccessibleRelationSet;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleTable;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

/**
 * Snapshot of the accessibility tree of some windows, as assistive technology sees it through the Java Accessibility
 * API, with what can be measured on each node: name, role, states, focusability, size, text contrast and clipping.
 */
final class Tree {

    static final Set<AccessibleRole> INTERACTIVE = Set.of(AccessibleRole.PUSH_BUTTON, AccessibleRole.TOGGLE_BUTTON,
        AccessibleRole.CHECK_BOX, AccessibleRole.RADIO_BUTTON, AccessibleRole.COMBO_BOX, AccessibleRole.TEXT,
        AccessibleRole.PASSWORD_TEXT, AccessibleRole.SLIDER, AccessibleRole.SPIN_BOX, AccessibleRole.TABLE,
        AccessibleRole.TREE, AccessibleRole.LIST, AccessibleRole.HYPERLINK, AccessibleRole.PAGE_TAB,
        AccessibleRole.CANVAS);
    private static final Set<AccessibleRole> SUMMARIZED = Set.of(AccessibleRole.TABLE, AccessibleRole.TREE,
        AccessibleRole.LIST);
    private static final int MAX_NODES = 20000;

    final List<Map<String, Object>> nodes = new ArrayList<>();
    final Map<Component, Integer> componentIds = new IdentityHashMap<>();
    final List<Component> components = new ArrayList<>();
    private final Map<Object, Integer> ids = new IdentityHashMap<>();

    static Tree capture(List<Window> windows) {
        Tree tree = new Tree();
        for (int i = 0; i < windows.size(); i++) {
            tree.add(windows.get(i), i);
        }
        return tree;
    }

    /**
     * Walks a window again, adding only the nodes not seen before, e.g. a panel shown since the last walk.
     */
    void add(Window window, int index) {
        Ui.edtRun(() -> visit(window, -1, 0, index, null, new IdentityHashMap<>()));
    }

    Integer id(Component c) {
        return componentIds.get(c);
    }

    private void visit(Accessible accessible, int parent, int depth, int window, AccessibleRole parentRole,
                       Map<Object, Boolean> pass) {
        if (accessible == null || pass.put(accessible, Boolean.TRUE) != null || nodes.size() >= MAX_NODES) {
            return;
        }
        AccessibleContext ac = accessible.getAccessibleContext();
        if (ac == null) {
            return;
        }
        Component comp = accessible instanceof Component ? (Component) accessible : null;
        AccessibleStateSet states = ac.getAccessibleStateSet();
        if (depth > 0 && (comp != null ? !comp.isShowing()
            : states != null && !states.contains(AccessibleState.SHOWING))) {
            return;
        }
        AccessibleRole role = ac.getAccessibleRole();
        Integer known = ids.get(accessible);
        if (known != null) {
            if (role == null || !SUMMARIZED.contains(role) && role != AccessibleRole.CANVAS) {
                children(ac, known, depth, window, role, pass);
            }
            return;
        }
        int id = nodes.size();
        ids.put(accessible, id);
        Map<String, Object> n = new LinkedHashMap<>();
        nodes.add(n);
        n.put("id", id);
        n.put("parent", parent);
        n.put("window", window);
        n.put("role", role == null ? null : role.toDisplayString(Locale.ENGLISH));
        n.put("name", ac.getAccessibleName());
        String desc = ac.getAccessibleDescription();
        if (desc != null && !desc.isEmpty()) {
            n.put("desc", desc);
        }
        n.put("states", states(states));
        n.put("class", comp != null ? comp.getClass().getName() : ac.getClass().getName());
        boolean part = isPart(comp, parentRole);
        boolean interactive = role != null && INTERACTIVE.contains(role) && !part
            && (role != AccessibleRole.TEXT || isFocusableText(comp, states));
        n.put("interactive", interactive);
        if (part) {
            n.put("part", true);
        }
        if (comp != null) {
            componentIds.put(comp, id);
            components.add(comp);
            describeComponent(comp, ac, n);
        }
        relations(ac, n);
        if (ac.getAccessibleValue() != null) {
            Number v = ac.getAccessibleValue().getCurrentAccessibleValue();
            n.put("value", v == null ? null : v.toString());
        }
        if (role != null && SUMMARIZED.contains(role)) {
            summarize(comp, ac, n);
            return;
        }
        if (role == AccessibleRole.CANVAS) {
            return;
        }
        children(ac, id, depth, window, role, pass);
    }

    private void children(AccessibleContext ac, int id, int depth, int window, AccessibleRole role,
                          Map<Object, Boolean> pass) {
        int count = ac.getAccessibleChildrenCount();
        for (int i = 0; i < count && i < 2000; i++) {
            visit(ac.getAccessibleChild(i), id, depth + 1, window, role, pass);
        }
    }

    private static List<String> states(AccessibleStateSet set) {
        List<String> list = new ArrayList<>();
        if (set != null) {
            for (AccessibleState s : set.toArray()) {
                if (s != AccessibleState.SHOWING && s != AccessibleState.VISIBLE && s != AccessibleState.OPAQUE) {
                    list.add(s.toDisplayString(Locale.ENGLISH));
                }
            }
        }
        return list;
    }

    private static boolean isFocusableText(Component comp, AccessibleStateSet states) {
        if (states != null && states.contains(AccessibleState.EDITABLE)) {
            return true;
        }
        return comp instanceof JTextComponent && comp.isFocusable();
    }

    /**
     * Pieces of a compound control (scroll and combo box arrows, a combo box's editor) that assistive technology
     * presents as part of their parent.
     */
    private static boolean isPart(Component comp, AccessibleRole parentRole) {
        if (parentRole == AccessibleRole.COMBO_BOX || parentRole == AccessibleRole.SCROLL_BAR
            || parentRole == AccessibleRole.SPIN_BOX) {
            return true;
        }
        if (comp == null) {
            return false;
        }
        String cls = comp.getClass().getName();
        return cls.contains("ArrowButton") || cls.contains("ScrollBar");
    }

    private void describeComponent(Component c, AccessibleContext ac, Map<String, Object> n) {
        if (c.isShowing()) {
            Point p = c.getLocationOnScreen();
            n.put("bounds", new int[] {p.x, p.y, c.getWidth(), c.getHeight()});
        }
        n.put("focusable", c.isFocusable());
        n.put("enabled", c.isEnabled());
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            if (!jc.isRequestFocusEnabled()) {
                n.put("requestFocusEnabled", false);
            }
            String tip = jc.getToolTipText();
            if (tip != null) {
                n.put("tooltip", Ui.plain(tip));
            }
            List<String> keys = windowKeys(jc);
            if (!keys.isEmpty()) {
                n.put("windowKeys", keys);
            }
        }
        if (c instanceof AbstractButton) {
            int mnemonic = ((AbstractButton) c).getMnemonic();
            if (mnemonic != 0) {
                n.put("mnemonic", String.valueOf((char) mnemonic));
            }
            if (((AbstractButton) c).isSelected()) {
                n.put("selected", true);
            }
        }
        String cls = c.getClass().getName();
        if (cls.startsWith("org.netbeans.swing.tabcontrol") || cls.startsWith("org.netbeans.core.windows")) {
            n.put("windowSystem", true);
        }
        Component tc = Gephi.topComponentOf(c);
        if (tc != null) {
            n.put("panel", Gephi.topComponentName(tc));
        }
        String text = Ui.visibleText(c);
        if (text != null && !text.isBlank()) {
            n.put("text", Ui.plain(text));
        }
        Font font = c.getFont();
        if (font != null) {
            n.put("fontSize", font.getSize2D());
            if (font.isBold()) {
                n.put("bold", true);
            }
        }
        if ((text != null && !text.isBlank()) || c instanceof JTextComponent || c instanceof JTable
            || c instanceof JTree || c instanceof JList) {
            Color fg = c.getForeground();
            Color bg = background(c);
            if (fg != null && bg != null) {
                n.put("fg", hex(fg));
                n.put("bg", hex(bg));
                n.put("contrast", round(Contrast.ratio(fg, bg)));
            }
        }
        if (c instanceof AbstractButton && (text == null || text.isBlank())
            && ((AbstractButton) c).getIcon() != null) {
            Double ic = iconContrast((AbstractButton) c);
            if (ic != null) {
                n.put("iconContrast", ic);
            }
        }
        if (text != null && !text.isBlank()) {
            Boolean clipped = clipped(c, text);
            if (clipped != null && clipped) {
                n.put("clipped", true);
            }
        }
    }

    private static void relations(AccessibleContext ac, Map<String, Object> n) {
        AccessibleRelationSet set = ac.getAccessibleRelationSet();
        if (set == null) {
            return;
        }
        AccessibleRelation labeledBy = set.get(AccessibleRelation.LABELED_BY);
        if (labeledBy != null) {
            List<String> labels = new ArrayList<>();
            for (Object target : labeledBy.getTarget()) {
                if (target instanceof Accessible) {
                    labels.add(((Accessible) target).getAccessibleContext().getAccessibleName());
                }
            }
            n.put("labeledBy", labels);
        }
    }

    private void summarize(Component comp, AccessibleContext ac, Map<String, Object> n) {
        if (comp instanceof JTable) {
            JTable table = (JTable) comp;
            n.put("rows", table.getRowCount());
            n.put("columns", table.getColumnCount());
            List<String> headers = new ArrayList<>();
            AccessibleTable at = ac.getAccessibleTable();
            AccessibleTable header = at == null ? null : at.getAccessibleColumnHeader();
            for (int c = 0; c < table.getColumnCount() && c < 50; c++) {
                String h = null;
                if (header != null && header.getAccessibleRowCount() > 0) {
                    Accessible cell = header.getAccessibleAt(0, c);
                    h = cell == null ? null : cell.getAccessibleContext().getAccessibleName();
                }
                headers.add(h);
            }
            n.put("headers", headers);
            if (at != null && table.getRowCount() > 0 && table.getColumnCount() > 0) {
                Accessible cell = at.getAccessibleAt(0, 0);
                n.put("firstCell", cell == null ? null : cell.getAccessibleContext().getAccessibleName());
                List<List<String>> sample = new ArrayList<>();
                for (int r = 0; r < Math.min(6, table.getRowCount()); r++) {
                    List<String> row = new ArrayList<>();
                    for (int c = 0; c < Math.min(4, table.getColumnCount()); c++) {
                        Accessible a = at.getAccessibleAt(r, c);
                        String name = a == null ? null : a.getAccessibleContext().getAccessibleName();
                        row.add(name == null || name.length() < 160 ? name : name.substring(0, 160));
                    }
                    sample.add(row);
                }
                n.put("sampleCells", sample);
            }
            n.put("clippedCells", clippedCells(table));
            n.put("selectionContrast", round(Contrast.ratio(table.getSelectionForeground(),
                table.getSelectionBackground())));
        } else {
            int count = ac.getAccessibleChildrenCount();
            n.put("rows", count);
            List<String> items = new ArrayList<>();
            for (int i = 0; i < count && i < 8; i++) {
                Accessible child = ac.getAccessibleChild(i);
                items.add(child == null ? null : child.getAccessibleContext().getAccessibleName());
            }
            n.put("items", items);
        }
    }

    /**
     * Cells of the visible rows whose content is wider than their column, so it is cut off (e.g. property names).
     */
    private static List<String> clippedCells(JTable table) {
        List<String> clipped = new ArrayList<>();
        Rectangle visible = table.getVisibleRect();
        int first = table.rowAtPoint(visible.getLocation());
        int last = table.rowAtPoint(new Point(visible.x, visible.y + visible.height - 1));
        if (first < 0) {
            return clipped;
        }
        if (last < 0) {
            last = table.getRowCount() - 1;
        }
        for (int row = first; row <= last && row < first + 60; row++) {
            for (int col = 0; col < table.getColumnCount(); col++) {
                try {
                    TableCellRenderer renderer = table.getCellRenderer(row, col);
                    Component r = table.prepareRenderer(renderer, row, col);
                    int width = table.getColumnModel().getColumn(col).getWidth();
                    String text = Ui.visibleText(r);
                    if (r.getPreferredSize().width > width + 1 && text != null && !text.isBlank()) {
                        clipped.add(Ui.plain(text));
                    }
                } catch (RuntimeException ex) {
                    // Renderers of plugins may not support being asked out of a paint
                }
            }
        }
        return clipped;
    }

    /**
     * Whether a label or button can't show all its text in its current size, as Swing then cuts it with "...".
     */
    private static Boolean clipped(Component c, String text) {
        if (text.startsWith("<html") || c.getWidth() <= 0 || !(c instanceof JLabel || c instanceof AbstractButton)) {
            return null;
        }
        Insets in = ((JComponent) c).getInsets();
        Rectangle view = new Rectangle(in.left, in.top, c.getWidth() - in.left - in.right,
            c.getHeight() - in.top - in.bottom);
        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();
        FontMetrics fm = c.getFontMetrics(c.getFont());
        String laid;
        if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            Icon icon = l.isEnabled() ? l.getIcon() : l.getDisabledIcon();
            laid = SwingUtilities.layoutCompoundLabel(l, fm, text, icon, l.getVerticalAlignment(),
                l.getHorizontalAlignment(), l.getVerticalTextPosition(), l.getHorizontalTextPosition(), view, iconR,
                textR, l.getIconTextGap());
        } else if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            Icon icon = b.getIcon();
            if (icon == null) {
                String cls = b.getClass().getSimpleName();
                icon = UIManager.getIcon(cls.contains("Radio") ? "RadioButton.icon"
                    : cls.contains("CheckBox") ? "CheckBox.icon" : "");
            }
            laid = SwingUtilities.layoutCompoundLabel(b, fm, text, icon, b.getVerticalAlignment(),
                b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), view, iconR,
                textR, b.getIconTextGap());
        } else {
            return null;
        }
        return !laid.equals(text);
    }

    /**
     * Contrast of an icon-only button's icon against the button's background (WCAG 1.4.11): the icon is painted
     * alone and its most contrasting opaque pixel, the stroke colour, is compared with the background.
     */
    private static Double iconContrast(AbstractButton b) {
        Icon icon = b.isEnabled() ? b.getIcon() : b.getDisabledIcon();
        if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0 || !b.isEnabled()) {
            return null;
        }
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(icon.getIconWidth(),
            icon.getIconHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        try {
            icon.paintIcon(b, g, 0, 0);
        } catch (RuntimeException ex) {
            return null;
        } finally {
            g.dispose();
        }
        Color bg = background(b);
        double best = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                if ((argb >>> 24) > 200) {
                    best = Math.max(best, Contrast.ratio(argb | 0xff000000, bg.getRGB()));
                }
            }
        }
        return best == 0 ? null : round(best);
    }

    /**
     * Background behind a component's text: its own if it paints one, else the first opaque ancestor's.
     */
    static Color background(Component c) {
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            boolean inToolbar = SwingUtilities.getAncestorOfClass(JToolBar.class, b) != null;
            if (b.isContentAreaFilled() && !inToolbar && b.getBackground() != null) {
                return b.getBackground();
            }
        }
        for (Component p = c; p != null; p = p.getParent()) {
            if (p.isOpaque() || p instanceof Window) {
                return p.getBackground();
            }
        }
        return Color.WHITE;
    }

    /**
     * Character keys bound to the whole window rather than to the focused component (WCAG 2.1.4).
     */
    private static List<String> windowKeys(JComponent jc) {
        List<String> list = new ArrayList<>();
        InputMap map = jc.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke[] keys = map == null ? null : map.keys();
        if (keys != null) {
            for (KeyStroke ks : keys) {
                if (Keys.isCharacterKey(ks)) {
                    list.add(Keys.describe(ks));
                }
            }
        }
        return list;
    }

    static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    static double round(double v) {
        return Math.round(v * 100) / 100.0;
    }

    /**
     * Focusable, enabled and showing components of a container, in tree order.
     */
    List<Component> focusablesIn(Container scope) {
        return Ui.edt(() -> {
            List<Component> list = new ArrayList<>();
            for (Component c : components) {
                if (c.isShowing() && c.isFocusable() && c.isEnabled() && SwingUtilities.isDescendingFrom(c, scope)) {
                    list.add(c);
                }
            }
            return list;
        });
    }
}
