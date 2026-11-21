package org.gephi.a11yaudit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;

/**
 * Java agent that audits the accessibility of a running Gephi: it waits for the main window, opens a sample graph,
 * visits each perspective and a set of dialogs, and writes what it measured to measurements.json. It only uses the
 * JDK and reaches Gephi by reflection, so the same jar audits any Gephi release.
 *
 * <p>Configured with system properties: a11yaudit.out (output directory), a11yaudit.variant (label of the run),
 * a11yaudit.graph (sample graph), a11yaudit.broken (a graph file with errors), a11yaudit.walks (keyboard walks,
 * default true), a11yaudit.timeoutSec (default 900).</p>
 */
public final class AuditAgent {

    static final int VERSION = 2;
    private static final List<String> LOG = Collections.synchronizedList(new ArrayList<>());
    private static final List<Map<String, Object>> GEPHI_LOG = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, Object> RESULTS = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final List<Map<String, Object>> SCENES = Collections.synchronizedList(new ArrayList<>());
    private static final long START = System.currentTimeMillis();
    private static File out;
    private static boolean walks;
    private static boolean robot;
    private static Frame main;
    private static volatile boolean written;
    private static int interruptedWalks;

    private AuditAgent() {
    }

    public static void premain(String args, Instrumentation inst) {
        out = new File(System.getProperty("a11yaudit.out", "a11yaudit"));
        walks = Boolean.parseBoolean(System.getProperty("a11yaudit.walks", "true"));
        robot = Boolean.parseBoolean(System.getProperty("a11yaudit.robot", "false"));
        out.mkdirs();
        Thread thread = new Thread(AuditAgent::run, "Gephi Accessibility Audit");
        thread.setDaemon(true);
        thread.start();
        long timeout = Long.getLong("a11yaudit.timeoutSec", 900) * 1000;
        Thread watchdog = new Thread(() -> {
            Ui.sleep(timeout);
            log("timeout after " + timeout + " ms, writing partial results");
            finish(2);
        }, "Gephi Accessibility Audit Watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    static File outputDir() {
        return out;
    }

    static void log(String message) {
        LOG.add(String.format(Locale.ROOT, "%7.1fs %s", (System.currentTimeMillis() - START) / 1000.0, message));
    }

    private interface Step {
        void run() throws Exception;
    }

    private static void step(String name, Step step) {
        long t0 = System.currentTimeMillis();
        try {
            step.run();
            log("ok " + name + " (" + (System.currentTimeMillis() - t0) + " ms)");
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            log("FAILED " + name + ": " + sw);
        }
    }

    private static void run() {
        try {
            main = waitForMainFrame(240_000);
            if (main == null) {
                log("main window never showed");
                finish(1);
                return;
            }
            Ui.settle(6000);
            Gephi.init();
            captureLogs();
            traceFocus();
            RESULTS.put("agentVersion", VERSION);
            RESULTS.put("variant", System.getProperty("a11yaudit.variant", "default"));
            RESULTS.put("date", java.time.LocalDate.now().toString());
            step("window size", AuditAgent::sizeMainWindow);
            step("runtime", () -> RESULTS.put("runtime", Env.runtime(main)));
            step("theme", () -> RESULTS.put("theme", Env.theme()));
            step("tooltips", () -> RESULTS.put("tooltips", Env.tooltips()));
            step("focus available", () -> RESULTS.put("focusedWindowAtStart",
                Ui.focusedWindow() == null ? null : Ui.edt(() -> Ui.focusedWindow().getClass().getName())));
            step("menus", () -> RESULTS.put("menus", Menus.collect(main)));
            step("keymap", () -> RESULTS.put("keymap", Keys.globalKeymap()));
            step("perspectives", () -> RESULTS.put("perspectives", perspectiveNames()));
            step("scene startup", () -> perspectiveScene("startup", null));
            String graph = System.getProperty("a11yaudit.graph");
            if (graph != null) {
                step("import", () -> importScene("import-report", new File(graph), false));
            }
            for (String[] p : Gephi.perspectives()) {
                String perspective = p[0].toLowerCase(Locale.ROOT);
                if (perspective.contains("preview")) {
                    step("preview refresh", () -> {
                        Gephi.selectPerspective(p[0]);
                        Ui.settle(3000);
                        Component settings = topComponent("Preview Settings");
                        AbstractButton refresh = settings == null ? null
                            : Ui.findButton((Container) settings, "Refresh");
                        if (refresh != null) {
                            Ui.later(refresh::doClick);
                            Ui.settle(4000);
                        }
                    });
                }
                step("perspective " + p[0], () -> perspectiveScene(sceneName(p[1]), p[0]));
                if (perspective.contains("overview") && walks) {
                    step("keyboard: graph view", () -> keyboardCheck("graphView",
                        Checks.graphView(main, topComponent("Graph"), robot)));
                    step("keyboard: colour swatch", () -> keyboardCheck("colorSwatch",
                        Checks.colorSwatch((Container) topComponent("Appearance"))));
                    step("keyboard: filter", () -> keyboardCheck("filterEnter",
                        Checks.filterEnter((Container) topComponent("Filters"), "Degree Range")));
                }
                if (perspective.contains("overview")) {
                    step("layout run", AuditAgent::layoutRun);
                    step("statistic", AuditAgent::statisticRun);
                }
                if ((perspective.contains("datalab") || perspective.contains("laboratory")) && walks) {
                    step("keyboard: command popups", () -> {
                        ensureFocus();
                        keyboardCheck("commandPopups", Checks.commandPopups((Container) topComponent("Data Table")));
                    });
                }
                if (perspective.contains("datalab") || perspective.contains("laboratory")) {
                    buttonDialog("add-column", "Data Table", "Add column");
                    buttonDialog("search-replace", "Data Table", "Search/Replace");
                }
            }
            step("dialogs", AuditAgent::menuDialogs);
            String broken = System.getProperty("a11yaudit.broken");
            if (broken != null) {
                step("import with issues", () -> importScene("import-report-issues", new File(broken), true));
            }
            finish(0);
        } catch (Throwable t) {
            log("audit failed: " + t);
            finish(1);
        }
    }

    private static Frame waitForMainFrame(long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            for (Frame f : Frame.getFrames()) {
                if (f.isShowing() && f.getTitle() != null && f.getTitle().startsWith("Gephi")) {
                    return f;
                }
            }
            Ui.sleep(500);
        }
        return null;
    }

    /**
     * A fixed window size, so measurements that depend on layout (clipped text, target sizes) compare across runs.
     */
    private static void sizeMainWindow() {
        Ui.edtRun(() -> {
            Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            Dimension size = new Dimension(Math.min(1440, screen.width), Math.min(900, screen.height));
            main.setExtendedState(Frame.NORMAL);
            main.setBounds(screen.x, screen.y, size.width, size.height);
            RESULTS.put("windowSize", new int[] {size.width, size.height});
        });
        Ui.settle(1500);
        RESULTS.put("foreground", ensureFocus());
    }

    /**
     * Brings Gephi in front so it receives keyboard focus; macOS doesn't activate an app started from a terminal.
     */
    private static boolean ensureFocus() {
        for (int attempt = 0; attempt < 3 && Ui.focusedWindow() == null; attempt++) {
            Ui.edtRun(() -> {
                if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.APP_REQUEST_FOREGROUND)) {
                    java.awt.Desktop.getDesktop().requestForeground(true);
                }
                main.toFront();
                main.requestFocus();
            });
            Ui.settle(1500);
        }
        boolean focused = Ui.focusedWindow() != null;
        if (!focused) {
            log("Gephi has no keyboard focus: keyboard walks will fail");
        }
        return focused;
    }

    private static void captureLogs() {
        Logger.getLogger("").addHandler(new Handler() {
            @Override
            public void publish(LogRecord r) {
                if (r.getLevel().intValue() >= Level.WARNING.intValue() && GEPHI_LOG.size() < 300) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("level", r.getLevel().getName());
                    m.put("logger", r.getLoggerName());
                    m.put("message", r.getMessage());
                    if (r.getThrown() != null) {
                        m.put("thrown", r.getThrown().toString());
                    }
                    GEPHI_LOG.add(m);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
    }

    /**
     * Logs window activation changes, to tell when Gephi lost keyboard focus to another application.
     */
    private static void traceFocus() {
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            int id = e.getID();
            if (id == java.awt.event.WindowEvent.WINDOW_GAINED_FOCUS
                || id == java.awt.event.WindowEvent.WINDOW_LOST_FOCUS) {
                java.awt.event.WindowEvent we = (java.awt.event.WindowEvent) e;
                Window opposite = we.getOppositeWindow();
                log((id == java.awt.event.WindowEvent.WINDOW_GAINED_FOCUS ? "focus gained " : "focus lost ")
                    + we.getWindow().getClass().getSimpleName()
                    + (opposite == null ? " (other application)" : " to/from " + opposite.getClass().getSimpleName()));
            }
        }, java.awt.AWTEvent.WINDOW_FOCUS_EVENT_MASK);
    }

    @SuppressWarnings("unchecked")
    private static void keyboardCheck(String name, Object result) {
        Map<String, Object> checks = (Map<String, Object>) RESULTS.computeIfAbsent("keyboardChecks",
            k -> Collections.synchronizedMap(new LinkedHashMap<>()));
        checks.put(name, result);
    }

    private static List<Map<String, Object>> perspectiveNames() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] p : Gephi.perspectives()) {
            list.add(Map.of("name", p[0], "displayName", p[1]));
        }
        return list;
    }

    private static String sceneName(String displayName) {
        return Ui.plain(displayName).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }

    private static Map<String, Object> scene(String name, String kind, List<Window> windows, Tree tree) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("name", name);
        s.put("kind", kind);
        List<Map<String, Object>> infos = new ArrayList<>();
        for (int i = 0; i < windows.size(); i++) {
            Window w = windows.get(i);
            Map<String, Object> info = Ui.edt(() -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("class", w.getClass().getName());
                m.put("title", w instanceof Frame ? ((Frame) w).getTitle()
                    : w instanceof Dialog ? ((Dialog) w).getTitle() : null);
                m.put("modal", w instanceof Dialog && ((Dialog) w).isModal());
                m.put("size", new int[] {w.getWidth(), w.getHeight()});
                return m;
            });
            info.put("screenshot", Ui.screenshot(w, out, name + (i == 0 ? "" : "-" + i)));
            infos.add(info);
        }
        s.put("windows", infos);
        s.put("nodes", tree.nodes);
        SCENES.add(s);
        return s;
    }

    private static void perspectiveScene(String name, String perspective) throws Exception {
        if (perspective != null) {
            Gephi.selectPerspective(perspective);
            Ui.settle(4000);
        }
        List<Window> windows = List.of(main);
        Tree tree = Tree.capture(windows);
        Map<String, Object> s = scene(name, perspective == null ? "startup" : "perspective", windows, tree);
        s.put("perspective", perspective);
        s.put("focusOwner", Ui.focusOwner() == null ? null : Focus.describe(Ui.focusOwner(), tree));
        if (perspective == null) {
            return;
        }
        // Panels in tabs that aren't selected, like Statistics behind Filters, are shown one at a time
        List<Component> panels = Gephi.openedTopComponents();
        List<String> shownLater = new ArrayList<>();
        for (Component tc : panels) {
            if (!Ui.edt(tc::isShowing)) {
                Gephi.requestActive(tc);
                Ui.settle(800);
                if (Ui.edt(tc::isShowing)) {
                    tree.add(main, 0);
                    shownLater.add(Gephi.topComponentName(tc));
                }
            }
        }
        s.put("panels", panelNames(panels));
        s.put("panelsShownLater", shownLater);
        if (!walks) {
            return;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Component tc : panels) {
            String label = Gephi.topComponentName(tc);
            Map<String, Object> walk = withFocus(label, () -> {
                Gephi.requestActive(tc);
                Ui.settle(600);
                if (Ui.edt(() -> javax.swing.SwingUtilities.getWindowAncestor(tc)) != main) {
                    return null;
                }
                Component owner = Ui.focusOwner();
                Component start = null;
                if (owner == null || !Ui.edt(() -> javax.swing.SwingUtilities.isDescendingFrom(owner, tc))) {
                    List<Component> focusables = tree.focusablesIn((Container) tc);
                    start = focusables.isEmpty() ? null : focusables.get(0);
                }
                Map<String, Object> w = Focus.walk(label, (Container) tc, start, tree, true, 150);
                w.put("focusAfterActivation", owner == null ? null : Focus.describe(owner, tree));
                return w;
            });
            if (walk != null) {
                list.add(walk);
            }
        }
        s.put("walks", list);
    }

    private interface Walk {
        Map<String, Object> run() throws Exception;
    }

    /**
     * Runs a keyboard walk with Gephi in front, again up to twice if another application took the focus meanwhile
     * (the walk then ends without focus, which says nothing about Gephi).
     */
    private static Map<String, Object> withFocus(String label, Walk walk) throws Exception {
        if (interruptedWalks >= 3) {
            // Someone is using the computer: stop taking the focus from them, the walks would be invalid anyway
            Map<String, Object> skipped = new LinkedHashMap<>();
            skipped.put("scope", label);
            skipped.put("outcome", "noFocus");
            skipped.put("interrupted", true);
            skipped.put("skipped", true);
            return skipped;
        }
        Map<String, Object> result = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            ensureFocus();
            result = walk.run();
            if (result == null) {
                return null;
            }
            Object outcome = result.get("outcome");
            boolean interrupted = "lost".equals(outcome) || "noFocus".equals(outcome) && Ui.focusedWindow() == null;
            result.put("attempts", attempt);
            if (!interrupted) {
                interruptedWalks = 0;
                return result;
            }
            result.put("interrupted", true);
            log("walk " + label + " lost focus to another application, attempt " + attempt);
            Ui.sleep(2000);
        }
        interruptedWalks++;
        if (interruptedWalks == 3) {
            log("three walks in a row lost focus: skipping the remaining keyboard walks");
        }
        return result;
    }

    private static List<String> panelNames(List<Component> panels) {
        List<String> names = new ArrayList<>();
        for (Component tc : panels) {
            names.add(Gephi.topComponentName(tc));
        }
        return names;
    }

    private static void importScene(String name, File file, boolean cancel) throws Exception {
        Set<Window> before = new HashSet<>(Ui.showingWindows());
        Ui.later(() -> Gephi.importFile(file));
        Window dialog = Ui.waitForNewWindow(before, 30_000);
        if (dialog == null) {
            throw new IllegalStateException("no import report dialog");
        }
        Ui.settle(1500);
        dialogScene(name, dialog, cancel ? new String[] {"Cancel"} : new String[] {"OK"}, false);
        Ui.settle(3000);
    }

    private static void dialogScene(String name, Window dialog, String[] closeButtons, boolean escapeFirst)
        throws Exception {
        Tree tree = Tree.capture(List.of(dialog));
        Map<String, Object> s = scene(name, "dialog", List.of(dialog), tree);
        Component owner = Ui.focusOwner();
        s.put("focusOwner", owner == null ? null : Focus.describe(owner, tree));
        if (walks) {
            Component initial = owner;
            Map<String, Object> walk = withFocus(name, () -> {
                Ui.edtRun(dialog::toFront);
                Ui.settle(300);
                Component start = initial != null && Ui.edt(() -> javax.swing.SwingUtilities
                    .isDescendingFrom(initial, dialog)) ? initial : null;
                return Focus.walk(name, (Container) dialog, start, tree, true, 100);
            });
            s.put("walks", walk == null ? List.of() : List.of(walk));
        }
        s.put("closedWith", close(dialog, closeButtons, escapeFirst));
    }

    /**
     * Closes a dialog with Escape or one of its buttons, recording which worked (Escape closing dialogs is part of
     * keyboard operation).
     */
    private static String close(Window dialog, String[] buttons, boolean escapeFirst) {
        if (escapeFirst) {
            Ui.edtRun(dialog::toFront);
            Ui.settle(200);
            Ui.key(KeyEvent.VK_ESCAPE, 0, KeyEvent.CHAR_UNDEFINED);
            if (Ui.waitClosed(dialog, 3000)) {
                return "escape";
            }
        }
        AbstractButton button = Ui.findButton(dialog, buttons);
        if (button != null) {
            String text = Ui.edt(button::getText);
            Ui.later(button::doClick);
            if (Ui.waitClosed(dialog, 10_000)) {
                return "button " + Ui.plain(text);
            }
        }
        Ui.edtRun(dialog::dispose);
        return "disposed";
    }

    private static void menuDialog(String name, String... path) {
        step("dialog " + name, () -> {
            JMenuItem item = Menus.find(main, path);
            if (item == null) {
                throw new IllegalStateException("menu item not found: " + String.join(" > ", path));
            }
            openDialog(name, item::doClick);
        });
    }

    /**
     * Opens a dialog from a layer action, for the ones macOS moves to the application menu (Options, About).
     */
    private static void actionDialog(String name, String category, String id) {
        step("dialog " + name, () -> {
            javax.swing.Action action = Gephi.action(category, id);
            if (action == null) {
                throw new IllegalStateException("no action " + category + "/" + id);
            }
            openDialog(name, () -> action.actionPerformed(new java.awt.event.ActionEvent(main, 0, null)));
        });
    }

    private static void buttonDialog(String name, String panel, String button) {
        step("dialog " + name, () -> {
            Component tc = topComponent(panel);
            Component b = tc == null ? null : Ui.findClickable((Container) tc, button);
            if (b == null) {
                throw new IllegalStateException("no button " + button + " in " + panel);
            }
            openDialog(name, () -> Ui.click(b));
        });
    }

    private static void openDialog(String name, Ui.Task open) throws Exception {
        Set<Window> before = new HashSet<>(Ui.showingWindows());
        Ui.later(open);
        Window dialog = Ui.waitForNewWindow(before, 15_000);
        if (dialog == null) {
            throw new IllegalStateException("no window opened for " + name);
        }
        Ui.settle(2000);
        dialogScene(name, dialog, new String[] {"Cancel", "Close", "OK"}, true);
        Ui.settle(1000);
    }

    private static void menuDialogs() throws Exception {
        Gephi.selectPerspective(Gephi.perspectives().get(0)[0]);
        Ui.settle(3000);
        menuDialog("export-graph-file", "File", "Export", "Graph file");
        menuDialog("export-image", "File", "Export", "SVG/PDF/PNG file");
        menuDialog("welcome", "Window", "Welcome");
        menuDialog("plugins", "Tools", "Plugins");
        actionDialog("options", "Window", "org.netbeans.modules.options.OptionsWindowAction");
        actionDialog("about", "Help", "org.netbeans.core.actions.AboutAction");
    }

    /**
     * A panel of the current perspective by name, selecting its tab if needed.
     */
    private static Component topComponent(String name) throws Exception {
        for (Component tc : Gephi.openedTopComponents()) {
            if (name.equalsIgnoreCase(Gephi.topComponentName(tc))) {
                if (!Ui.edt(tc::isShowing)) {
                    Gephi.requestActive(tc);
                    Ui.settle(800);
                }
                return tc;
            }
        }
        return null;
    }

    /**
     * Runs ForceAtlas 2 for a few seconds from the Layout panel, recording the accessibility events Gephi fires
     * meanwhile (WCAG 4.1.3: are progress and results exposed without moving focus).
     */
    private static void layoutRun() throws Exception {
        Component tc = topComponent("Layout");
        if (tc == null) {
            throw new IllegalStateException("no Layout panel");
        }
        JComboBox<?> combo = Ui.edt(() -> {
            for (Component c : Ui.descendants((Container) tc)) {
                if (c instanceof JComboBox && c.isShowing()) {
                    return (JComboBox<?>) c;
                }
            }
            return null;
        });
        if (combo == null) {
            throw new IllegalStateException("no layout chooser");
        }
        Ui.edtRun(() -> {
            for (int i = 0; i < combo.getItemCount(); i++) {
                Object item = combo.getItemAt(i);
                if (item != null && String.valueOf(item).contains("ForceAtlas 2")) {
                    combo.setSelectedIndex(i);
                    return;
                }
                if (item != null) {
                    try {
                        if (String.valueOf(Gephi.call(item, "getName")).contains("ForceAtlas 2")) {
                            combo.setSelectedIndex(i);
                            return;
                        }
                    } catch (Exception ex) {
                        // Not a builder with a name
                    }
                }
            }
            throw new IllegalStateException("ForceAtlas 2 not in the layout chooser");
        });
        Ui.settle(1000);
        AbstractButton run = Ui.findButton((Container) tc, "Run");
        if (run == null) {
            throw new IllegalStateException("no Run button");
        }
        Events events = Events.attach(Tree.capture(List.of(main)));
        Component focusBefore = Ui.focusOwner();
        Ui.later(run::doClick);
        Ui.sleep(4000);
        AbstractButton stop = Ui.findButton((Container) tc, "Stop");
        if (stop != null) {
            Ui.later(stop::doClick);
        }
        Ui.settle(2000);
        Map<String, Object> m = events.detach();
        m.put("focusMoved", Ui.focusOwner() != focusBefore);
        m.put("stopButtonFound", stop != null);
        RESULTS.put("layoutRun", m);
    }

    /**
     * Runs Average Degree from the Statistics panel and audits its report.
     */
    private static void statisticRun() throws Exception {
        Component tc = topComponent("Statistics");
        if (tc == null) {
            throw new IllegalStateException("no Statistics panel");
        }
        AbstractButton run = Ui.edt(() -> {
            for (Component c : Ui.descendants((Container) tc)) {
                if ("Average Degree".equalsIgnoreCase(Ui.plain(Ui.visibleText(c)))) {
                    Container p = c.getParent();
                    for (int i = 0; i < 3 && p != null; i++, p = p.getParent()) {
                        for (Component b : Ui.descendants(p)) {
                            if (b instanceof AbstractButton && "Run".equalsIgnoreCase(Ui.plain(((AbstractButton) b)
                                .getText())) && b.isShowing()) {
                                return (AbstractButton) b;
                            }
                        }
                    }
                }
            }
            return null;
        });
        if (run == null) {
            throw new IllegalStateException("no Run button for Average Degree");
        }
        Events events = Events.attach(Tree.capture(List.of(main)));
        Set<Window> before = new HashSet<>(Ui.showingWindows());
        Ui.later(run::doClick);
        Window report = Ui.waitForNewWindow(before, 20_000);
        Map<String, Object> m = events.detach();
        m.put("reportOpened", report != null);
        RESULTS.put("statisticRun", m);
        if (report != null) {
            Ui.settle(1500);
            dialogScene("statistics-report", report, new String[] {"Close", "OK"}, true);
        }
    }

    /**
     * Records accessibility property changes on every component of a tree while an action runs.
     */
    private static final class Events implements PropertyChangeListener {

        private final List<AccessibleContext> contexts = new ArrayList<>();
        private final List<Map<String, Object>> events = Collections.synchronizedList(new ArrayList<>());

        static Events attach(Tree tree) {
            Events e = new Events();
            Ui.edtRun(() -> {
                for (Component c : tree.components) {
                    AccessibleContext ac = c.getAccessibleContext();
                    if (ac != null) {
                        ac.addPropertyChangeListener(e);
                        e.contexts.add(ac);
                    }
                }
            });
            return e;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (events.size() >= 400 || !(evt.getSource() instanceof AccessibleContext)) {
                return;
            }
            AccessibleContext ac = (AccessibleContext) evt.getSource();
            AccessibleRole role = ac.getAccessibleRole();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("property", evt.getPropertyName());
            m.put("role", role == null ? null : role.toDisplayString(Locale.ENGLISH));
            m.put("name", ac.getAccessibleName());
            String value = evt.getNewValue() == null ? null : String.valueOf(evt.getNewValue());
            m.put("newValue", value == null || value.length() < 120 ? value : value.substring(0, 120));
            events.add(m);
        }

        Map<String, Object> detach() {
            Ui.edtRun(() -> {
                for (AccessibleContext ac : contexts) {
                    ac.removePropertyChangeListener(this);
                }
            });
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("events", new ArrayList<>(events));
            return m;
        }
    }

    private static synchronized void finish(int status) {
        if (written) {
            return;
        }
        written = true;
        RESULTS.put("scenes", new ArrayList<>(SCENES));
        RESULTS.put("gephiLog", new ArrayList<>(GEPHI_LOG));
        RESULTS.put("log", new ArrayList<>(LOG));
        RESULTS.put("status", status);
        try {
            Files.writeString(new File(out, "measurements.json").toPath(), Json.write(RESULTS),
                StandardCharsets.UTF_8);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Runtime.getRuntime().halt(status);
    }
}
