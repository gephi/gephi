package org.gephi.a11yaudit;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;

/**
 * Menu bar contents with mnemonics and shortcuts. NetBeans fills most menus lazily when they are first selected, so
 * each menu is selected and deselected before it is read.
 */
final class Menus {

    private static final int MAX_DEPTH = 3;

    private Menus() {
    }

    static List<Map<String, Object>> collect(Frame frame) {
        List<Map<String, Object>> menus = new ArrayList<>();
        for (Component c : topLevel(frame)) {
            menus.add(describe((JMenu) c, 0));
        }
        return menus;
    }

    private static List<Component> topLevel(Frame frame) {
        JMenuBar bar = frame instanceof JFrame ? Ui.edt(((JFrame) frame)::getJMenuBar) : null;
        if (bar == null) {
            return List.of();
        }
        return Ui.edt(() -> {
            List<Component> list = new ArrayList<>();
            for (int i = 0; i < bar.getMenuCount(); i++) {
                if (bar.getMenu(i) != null) {
                    list.add(bar.getMenu(i));
                }
            }
            return list;
        });
    }

    private static Map<String, Object> describe(JMenu menu, int depth) {
        List<Component> children = children(menu);
        Map<String, Object> m = Ui.edt(() -> item(menu));
        List<Map<String, Object>> items = new ArrayList<>();
        for (Component c : children) {
            if (c instanceof JMenu && depth < MAX_DEPTH) {
                items.add(describe((JMenu) c, depth + 1));
            } else if (c instanceof JMenuItem) {
                items.add(Ui.edt(() -> item((JMenuItem) c)));
            }
        }
        m.put("items", items);
        return m;
    }

    private static Map<String, Object> item(JMenuItem item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("text", Ui.plain(item.getText()));
        m.put("name", item.getAccessibleContext().getAccessibleName());
        int mnemonic = item.getMnemonic();
        if (mnemonic != 0) {
            m.put("mnemonic", KeyEvent.getKeyText(mnemonic));
        }
        if (!(item instanceof JMenu)) {
            KeyStroke ks = item.getAccelerator();
            if (ks != null) {
                m.put("accelerator", Keys.describe(ks));
            }
        }
        m.put("enabled", item.isEnabled());
        return m;
    }

    private static List<Component> children(JMenu menu) {
        Ui.edtRun(() -> {
            menu.setSelected(true);
            menu.getPopupMenu();
            menu.setSelected(false);
            MenuSelectionManager.defaultManager().clearSelectedPath();
        });
        Ui.settle(100);
        return Ui.edt(() -> List.of(menu.getMenuComponents()));
    }

    /**
     * Finds a menu item by the plain texts of its path, e.g. "File", "Export", "Graph file".
     */
    static JMenuItem find(Frame frame, String... path) {
        List<Component> level = topLevel(frame);
        JMenuItem found = null;
        for (String step : path) {
            found = null;
            for (Component c : level) {
                if (c instanceof JMenuItem
                    && step.equalsIgnoreCase(Ui.edt(() -> Ui.plain(((JMenuItem) c).getText())))) {
                    found = (JMenuItem) c;
                    break;
                }
            }
            if (found == null) {
                return null;
            }
            if (found instanceof JMenu) {
                level = children((JMenu) found);
            }
        }
        return found;
    }
}
