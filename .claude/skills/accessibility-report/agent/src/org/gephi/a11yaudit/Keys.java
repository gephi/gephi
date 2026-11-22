package org.gephi.a11yaudit;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

/**
 * Keyboard shortcuts of the global NetBeans keymap, and the character key shortcuts WCAG 2.1.4 is about.
 */
final class Keys {

    private static final int MODIFIERS = InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK
        | InputEvent.META_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK;

    private Keys() {
    }

    /**
     * A single letter, number, punctuation or symbol key, with no modifier other than Shift.
     */
    static boolean isCharacterKey(KeyStroke ks) {
        if ((ks.getModifiers() & MODIFIERS) != 0) {
            return false;
        }
        if (ks.getKeyEventType() == KeyEvent.KEY_TYPED) {
            char c = ks.getKeyChar();
            return c > ' ' && c != KeyEvent.CHAR_UNDEFINED && c != 127;
        }
        int code = ks.getKeyCode();
        return code >= KeyEvent.VK_A && code <= KeyEvent.VK_Z || code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9
            || code >= KeyEvent.VK_NUMPAD0 && code <= KeyEvent.VK_NUMPAD9 || code == KeyEvent.VK_ADD
            || code == KeyEvent.VK_SUBTRACT || code == KeyEvent.VK_MULTIPLY || code == KeyEvent.VK_DIVIDE
            || code == KeyEvent.VK_PLUS || code == KeyEvent.VK_MINUS || code == KeyEvent.VK_EQUALS
            || code == KeyEvent.VK_COMMA || code == KeyEvent.VK_PERIOD || code == KeyEvent.VK_SLASH
            || code == KeyEvent.VK_SEMICOLON || code == KeyEvent.VK_QUOTE || code == KeyEvent.VK_OPEN_BRACKET
            || code == KeyEvent.VK_CLOSE_BRACKET || code == KeyEvent.VK_BACK_SLASH;
    }

    static String describe(KeyStroke ks) {
        String mods = InputEvent.getModifiersExText(ks.getModifiers());
        String key = ks.getKeyEventType() == KeyEvent.KEY_TYPED ? "'" + ks.getKeyChar() + "'"
            : KeyEvent.getKeyText(ks.getKeyCode());
        return mods.isEmpty() ? key : mods + "+" + key;
    }

    /**
     * Global shortcuts, from the layer's Shortcuts folder; the NetBeans keymap doesn't list its key strokes.
     */
    static Map<String, Object> globalKeymap() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> bindings = new ArrayList<>();
        List<String> characterKeys = new ArrayList<>();
        for (Object[] s : Gephi.shortcuts()) {
            KeyStroke ks = (KeyStroke) s[2];
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("folder", s[0]);
            b.put("file", s[1]);
            b.put("key", ks == null ? null : describe(ks));
            b.put("action", s[3]);
            bindings.add(b);
            if (ks != null && isCharacterKey(ks)) {
                characterKeys.add(describe(ks) + " " + s[3]);
            }
        }
        result.put("bindings", bindings);
        result.put("characterKeys", characterKeys);
        Keymap keymap = Gephi.globalKeymap();
        result.put("keymapClass", keymap == null ? null : keymap.getClass().getName());
        return result;
    }
}
