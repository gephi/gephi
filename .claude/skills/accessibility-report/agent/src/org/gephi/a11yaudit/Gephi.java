package org.gephi.a11yaudit;

import java.awt.Component;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reaches Gephi and NetBeans Platform APIs by reflection, through the NetBeans system class loader, so the agent
 * has no compile-time dependency on any Gephi version.
 */
final class Gephi {

    private static ClassLoader loader;
    private static final Map<Class<?>, Boolean> TOP_COMPONENT_CLASSES = new IdentityHashMap<>();

    private Gephi() {
    }

    static void init() {
        loader = Ui.edt(() -> Thread.currentThread().getContextClassLoader());
    }

    static Class<?> type(String name) throws ClassNotFoundException {
        return Class.forName(name, true, loader);
    }

    static Object lookup(String className) throws Exception {
        Class<?> lookupClass = type("org.openide.util.Lookup");
        Object lookup = lookupClass.getMethod("getDefault").invoke(null);
        return lookupClass.getMethod("lookup", Class.class).invoke(lookup, type(className));
    }

    static Object callStatic(String className, String method, Object... args) throws Exception {
        return invoke(type(className), null, method, args);
    }

    static Object call(Object target, String method, Object... args) throws Exception {
        return invoke(target.getClass(), target, method, args);
    }

    /**
     * Invokes the public method declared on a public class or interface, as implementation classes are often
     * package private.
     */
    private static Object invoke(Class<?> type, Object target, String name, Object... args) throws Exception {
        for (Method m : publicMethods(type)) {
            if (m.getName().equals(name) && m.getParameterCount() == args.length && accepts(m, args)) {
                return m.invoke(target, args);
            }
        }
        throw new NoSuchMethodException(type.getName() + "." + name + " with " + args.length + " arguments");
    }

    private static List<Method> publicMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            addPublic(c, methods);
            for (Class<?> i : c.getInterfaces()) {
                addInterface(i, methods);
            }
        }
        return methods;
    }

    private static void addInterface(Class<?> i, List<Method> methods) {
        addPublic(i, methods);
        for (Class<?> parent : i.getInterfaces()) {
            addInterface(parent, methods);
        }
    }

    private static void addPublic(Class<?> c, List<Method> methods) {
        if (!Modifier.isPublic(c.getModifiers())) {
            return;
        }
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
                methods.add(m);
            }
        }
    }

    private static boolean accepts(Method m, Object[] args) {
        Class<?>[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (args[i] != null && !box(params[i]).isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> box(Class<?> c) {
        if (!c.isPrimitive()) {
            return c;
        }
        if (c == int.class) {
            return Integer.class;
        }
        if (c == boolean.class) {
            return Boolean.class;
        }
        if (c == long.class) {
            return Long.class;
        }
        if (c == double.class) {
            return Double.class;
        }
        if (c == float.class) {
            return Float.class;
        }
        return c;
    }

    /**
     * Opens a graph file the way File > Open does once the file is chosen: with the import report dialog.
     */
    static void importFile(File file) throws Exception {
        Object normalized = callStatic("org.openide.filesystems.FileUtil", "normalizeFile", file);
        Object fileObject = callStatic("org.openide.filesystems.FileUtil", "toFileObject", normalized);
        Object ui = lookup("org.gephi.desktop.importer.api.ImportControllerUI");
        call(ui, "importFile", fileObject);
    }

    static boolean hasProject() throws Exception {
        Object pc = lookup("org.gephi.project.api.ProjectController");
        return (Boolean) call(pc, "hasCurrentProject");
    }

    /**
     * Perspectives in their banner order, as name and display name pairs.
     */
    static List<String[]> perspectives() throws Exception {
        Object pc = lookup("org.gephi.perspective.api.PerspectiveController");
        List<String[]> list = new ArrayList<>();
        for (Object p : (Object[]) call(pc, "getPerspectives")) {
            list.add(new String[] {(String) call(p, "getName"), (String) call(p, "getDisplayName")});
        }
        return list;
    }

    static void selectPerspective(String name) throws Exception {
        Object pc = lookup("org.gephi.perspective.api.PerspectiveController");
        for (Object p : (Object[]) call(pc, "getPerspectives")) {
            if (name.equals(call(p, "getName"))) {
                Ui.edtRun(() -> call(pc, "selectPerspective", p));
                return;
            }
        }
        throw new IllegalArgumentException("No perspective " + name);
    }

    /**
     * Top components (NetBeans windows such as Layout or Filters) opened in the current perspective, including
     * tabs that are not selected and so not showing.
     */
    static List<Component> openedTopComponents() throws Exception {
        Object wm = callStatic("org.openide.windows.WindowManager", "getDefault");
        Object registry = call(wm, "getRegistry");
        Collection<?> opened = (Collection<?>) call(registry, "getOpened");
        List<Component> list = new ArrayList<>();
        for (Object tc : opened) {
            list.add((Component) tc);
        }
        return list;
    }

    static List<Component> showingTopComponents() throws Exception {
        List<Component> list = new ArrayList<>();
        for (Component tc : openedTopComponents()) {
            if (Ui.edt(tc::isShowing)) {
                list.add(tc);
            }
        }
        return list;
    }

    /**
     * An action registered in the layer, e.g. Options and About, which macOS shows in the application menu rather
     * than in the menu bar.
     */
    static javax.swing.Action action(String category, String id) throws Exception {
        return (javax.swing.Action) callStatic("org.openide.awt.Actions", "forID", category, id);
    }

    /**
     * Global shortcuts registered in the Shortcuts folder and keymap profiles of the system filesystem, as folder,
     * file name, key stroke and action display name.
     */
    static List<Object[]> shortcuts() throws Exception {
        List<Object[]> list = new ArrayList<>();
        for (String folder : new String[] {"Shortcuts", "Keymaps/NetBeans"}) {
            Object dir = callStatic("org.openide.filesystems.FileUtil", "getConfigFile", folder);
            if (dir == null) {
                continue;
            }
            for (Object file : (Object[]) call(dir, "getChildren")) {
                String name = (String) call(file, "getName");
                Object key = callStatic("org.openide.util.Utilities", "stringToKey", name);
                Object original = call(file, "getAttribute", "originalFile");
                String action = original == null ? (String) call(file, "getNameExt") : original.toString();
                if (original != null) {
                    Object target = callStatic("org.openide.filesystems.FileUtil", "getConfigFile", original);
                    Object display = target == null ? null : call(target, "getAttribute", "displayName");
                    if (display != null) {
                        action = Ui.plain(display.toString());
                    }
                }
                list.add(new Object[] {folder, name, key, action});
            }
        }
        return list;
    }

    static void requestActive(Component topComponent) {
        Ui.edtRun(() -> call(topComponent, "requestActive"));
    }

    static boolean isTopComponent(Component c) {
        return TOP_COMPONENT_CLASSES.computeIfAbsent(c.getClass(), type -> {
            for (Class<?> t = type; t != null; t = t.getSuperclass()) {
                if (t.getName().equals("org.openide.windows.TopComponent")) {
                    return true;
                }
            }
            return false;
        });
    }

    static Component topComponentOf(Component c) {
        for (Component p = c; p != null; p = p.getParent()) {
            if (isTopComponent(p)) {
                return p;
            }
        }
        return null;
    }

    static String topComponentName(Component tc) {
        if (tc == null) {
            return null;
        }
        try {
            Object name = call(tc, "getDisplayName");
            if (name == null) {
                name = call(tc, "getName");
            }
            return name == null ? tc.getClass().getSimpleName() : Ui.plain(name.toString());
        } catch (Exception ex) {
            return tc.getClass().getSimpleName();
        }
    }

    /**
     * The global NetBeans keymap, which holds the shortcuts of menu items and actions.
     */
    static javax.swing.text.Keymap globalKeymap() throws Exception {
        return (javax.swing.text.Keymap) lookup("javax.swing.text.Keymap");
    }
}
