package org.gephi.a11yaudit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 * Runtime, theme and tooltip facts.
 */
final class Env {

    /**
     * Colour pairs whose contrast matters: text on its background (WCAG 1.4.3, 4.5:1) and component boundaries and
     * focus indicators against what is next to them (1.4.11, 3:1).
     */
    private static final String[][] PAIRS = {
        {"text", "Label.foreground", "Panel.background"},
        {"text", "TextField.foreground", "TextField.background"},
        {"text", "TextField.placeholderForeground", "TextField.background"},
        {"text", "TextField.inactiveForeground", "TextField.background"},
        {"text", "Label.disabledForeground", "Panel.background"},
        {"text", "Button.foreground", "Button.background"},
        {"text", "Button.default.foreground", "Button.default.background"},
        {"text", "Table.foreground", "Table.background"},
        {"text", "Table.selectionForeground", "Table.selectionBackground"},
        {"text", "Table.selectionInactiveForeground", "Table.selectionInactiveBackground"},
        {"text", "TableHeader.foreground", "TableHeader.background"},
        {"text", "Tree.foreground", "Tree.background"},
        {"text", "Tree.selectionForeground", "Tree.selectionBackground"},
        {"text", "List.selectionForeground", "List.selectionBackground"},
        {"text", "ToolTip.foreground", "ToolTip.background"},
        {"text", "Menu.foreground", "Menu.background"},
        {"text", "MenuItem.selectionForeground", "MenuItem.selectionBackground"},
        {"text", "Component.linkColor", "Panel.background"},
        {"nonText", "Component.focusColor", "Panel.background"},
        {"nonText", "Component.focusedBorderColor", "TextField.background"},
        {"nonText", "Button.focusedBorderColor", "Button.background"},
        {"nonText", "Component.borderColor", "Panel.background"},
        {"nonText", "Component.borderColor", "TextField.background"},
        {"nonText", "Button.borderColor", "Panel.background"},
        {"nonText", "CheckBox.icon.borderColor", "Panel.background"},
        {"nonText", "CheckBox.icon.selectedBackground", "Panel.background"},
        {"nonText", "Slider.trackColor", "Panel.background"},
        {"nonText", "Slider.thumbColor", "Panel.background"},
        {"nonText", "ToggleButton.toolbar.selectedBackground", "ToolBar.background"},
    };

    private Env() {
    }

    static Map<String, Object> runtime(Frame main) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", Ui.edt(main::getTitle));
        for (String key : new String[] {"java.version", "java.vendor", "java.runtime.version", "os.name",
            "os.version", "os.arch", "netbeans.productversion", "user.language", "user.country"}) {
            m.put(key, System.getProperty(key));
        }
        String home = System.getProperty("java.home");
        m.put("javaHome", home);
        m.put("jdkAccessibilityModule", ModuleLayer.boot().findModule("jdk.accessibility").isPresent());
        List<String> bridgeFiles = new ArrayList<>();
        for (String dir : new String[] {"bin", "lib"}) {
            File[] files = new File(home, dir).listFiles();
            if (files != null) {
                for (File f : files) {
                    String name = f.getName().toLowerCase(Locale.ROOT);
                    if (name.contains("accessbridge") || name.startsWith("jabswitch") || name.contains("jaccess")) {
                        bridgeFiles.add(dir + "/" + f.getName());
                    }
                }
            }
        }
        m.put("accessBridgeFiles", bridgeFiles);
        m.put("assistiveTechnologies", System.getProperty("javax.accessibility.assistive_technologies"));
        Path userProps = Path.of(System.getProperty("user.home"), ".accessibility.properties");
        try {
            m.put("userAccessibilityProperties", Files.exists(userProps) ? Files.readString(userProps) : null);
        } catch (Exception ex) {
            m.put("userAccessibilityProperties", "unreadable: " + ex);
        }
        m.put("locale", Locale.getDefault().toLanguageTag());
        m.put("frameLocale", Ui.edt(() -> {
            Locale l = main.getAccessibleContext().getLocale();
            return l == null ? null : l.toLanguageTag();
        }));
        return m;
    }

    static Map<String, Object> theme() {
        return Ui.edt(() -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("lookAndFeel", UIManager.getLookAndFeel().getName());
            m.put("lookAndFeelClass", UIManager.getLookAndFeel().getClass().getName());
            List<String> installed = new ArrayList<>();
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                installed.add(info.getName());
            }
            m.put("installedLookAndFeels", installed);
            Font font = UIManager.getFont("Label.font");
            m.put("labelFontSize", font == null ? null : font.getSize2D());
            m.put("focusWidth", UIManager.get("Component.focusWidth"));
            m.put("innerFocusWidth", UIManager.get("Component.innerFocusWidth"));
            List<Map<String, Object>> pairs = new ArrayList<>();
            for (String[] p : PAIRS) {
                Color fg = UIManager.getColor(p[1]);
                Color bg = UIManager.getColor(p[2]);
                if (fg == null || bg == null) {
                    continue;
                }
                Map<String, Object> pair = new LinkedHashMap<>();
                pair.put("kind", p[0]);
                pair.put("foreground", p[1]);
                pair.put("background", p[2]);
                pair.put("fg", Tree.hex(fg));
                pair.put("bg", Tree.hex(bg));
                pair.put("contrast", Tree.round(Contrast.ratio(fg, bg)));
                pair.put("required", p[0].equals("text") ? 4.5 : 3.0);
                pairs.add(pair);
            }
            m.put("pairs", pairs);
            return m;
        });
    }

    static Map<String, Object> tooltips() {
        return Ui.edt(() -> {
            ToolTipManager t = ToolTipManager.sharedInstance();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("enabled", t.isEnabled());
            m.put("initialDelayMs", t.getInitialDelay());
            m.put("dismissDelayMs", t.getDismissDelay());
            m.put("reshowDelayMs", t.getReshowDelay());
            return m;
        });
    }
}
