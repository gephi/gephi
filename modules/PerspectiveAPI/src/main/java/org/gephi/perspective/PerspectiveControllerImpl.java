/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.perspective;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.gephi.perspective.api.PerspectiveController;
import org.gephi.perspective.spi.Perspective;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = PerspectiveController.class)
public class PerspectiveControllerImpl implements PerspectiveController {

    private static final Logger LOGGER = Logger.getLogger(PerspectiveControllerImpl.class.getName());
    private static final String SELECTED_PERSPECTIVE_PREFERENCE = "PerspectiveControllerImpl_selectedPerspective";

    // Matches frame-state="N" attribute (any integer value) inside the .wswmgr XML.
    // Used (macOS only) to strip persisted MAXIMIZED state from per-role window layout files,
    // which otherwise causes the window to spuriously re-maximize when switching perspectives.
    private static final Pattern FRAME_STATE_ATTR_PATTERN =
        Pattern.compile("(frame-state=\")(\\d+)(\")");

    private final Perspective[] perspectives;
    //Data
    private String selectedPerspective;

    public PerspectiveControllerImpl() {
        //Load perspectives
        perspectives = Lookup.getDefault().lookupAll(Perspective.class).toArray(new Perspective[0]);

        //Find if there is a default
        String firstPerspective = perspectives.length > 0 ? perspectives[0].getName() : null;
        String defaultPerspectiveName = System.getProperty("org.gephi.perspective.default");
        if (defaultPerspectiveName != null) {
            for (Perspective p : perspectives) {
                if (p.getName().equals(defaultPerspectiveName)) {
                    selectedPerspective = p.getName();
                    break;
                }
            }
        }
        if (selectedPerspective == null) {
            selectedPerspective = NbPreferences.root().get(SELECTED_PERSPECTIVE_PREFERENCE, firstPerspective);
        }

        //Store selected in prefs
        NbPreferences.root().put(SELECTED_PERSPECTIVE_PREFERENCE, selectedPerspective);

        Perspective selectedPerspectiveInstance = getSelectedPerspective();

        openAndCloseMembers(selectedPerspectiveInstance);

        WindowManager.getDefault().addWindowSystemListener(new WindowSystemListener() {
            private Dimension lastDimension = null;
            private Integer lastState = null;
            private Point lastLocation = null;

            @Override
            public void beforeLoad(WindowSystemEvent event) {
            }

            @Override
            public void afterLoad(WindowSystemEvent event) {
                Frame mainWindow = WindowManager.getDefault().getMainWindow();
                if (mainWindow == null) {
                    return;
                }
                if (lastDimension != null) {
                    mainWindow.setSize(lastDimension);
                }
                if (lastLocation != null) {
                    mainWindow.setLocation(lastLocation);
                }
                if (lastState != null) {
                    // setExtendedState (post-1.4) is the correct API for MAXIMIZED_BOTH;
                    // the legacy setState only handles the ICONIFIED bit and is a no-op
                    // for maximized states, which previously left the frame stuck.
                    mainWindow.setExtendedState(lastState);
                }
            }

            @Override
            public void beforeSave(WindowSystemEvent event) {
                Frame mainWindow = WindowManager.getDefault().getMainWindow();
                if (mainWindow != null) {
                    lastDimension = mainWindow.getSize();
                    lastLocation = mainWindow.getLocation();
                    lastState = mainWindow.getExtendedState();
                }
            }

            @Override
            public void afterSave(WindowSystemEvent event) {
                // macOS-only: NetBeans persists frame-state="6" (MAXIMIZED_BOTH) into per-role
                // .wswmgr files when the window is maximized. On a later perspective switch,
                // loading that role re-applies the maximized state from disk and triggers a
                // macOS native zoom that we cannot reliably override. We sidestep the issue by
                // stripping the persisted maximized state from the role files right after save.
                // In-session preservation of maximization across perspective switches still
                // works through lastState/lastDimension/lastLocation captured above.
                // On Windows/Linux the persisted state is left untouched so cross-session
                // "open maximized" behavior is preserved.
                if (Utilities.isMac()) {
                    sanitizePersistedFrameStateOnMac();
                }
            }
        });
    }

    /**
     * Rewrites every {@code WindowManager.wswmgr} file under the user's config directory so
     * that any {@code frame-state="N"} attribute is forced to {@code frame-state="0"}
     * (NORMAL). All IO failures are caught and logged so that a read-only filesystem,
     * a missing file, or a transient lock never breaks a perspective switch.
     */
    private static void sanitizePersistedFrameStateOnMac() {
        String userdir = System.getProperty("netbeans.user");
        if (userdir == null || userdir.isEmpty()) {
            return;
        }
        File configDir = new File(userdir, "config");
        File[] roleDirs = configDir.listFiles((d, name) -> name.startsWith("Windows2Local"));
        if (roleDirs == null) {
            return;
        }
        for (File roleDir : roleDirs) {
            File wmFile = new File(roleDir, "WindowManager.wswmgr");
            if (!wmFile.isFile() || !wmFile.canRead()) {
                continue;
            }
            try {
                String content = new String(Files.readAllBytes(wmFile.toPath()), StandardCharsets.UTF_8);
                Matcher m = FRAME_STATE_ATTR_PATTERN.matcher(content);
                StringBuilder sb = new StringBuilder(content.length());
                boolean changed = false;
                // Explicitly build the replacement instead of relying on $1/$3 backreferences,
                // because the literal "0" we want to insert would otherwise collide with Java's
                // $1 / $10 group syntax in replaceAll().
                while (m.find()) {
                    if (!"0".equals(m.group(2))) {
                        changed = true;
                    }
                    m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "0" + m.group(3)));
                }
                m.appendTail(sb);
                if (!changed) {
                    continue;
                }
                if (!wmFile.canWrite()) {
                    LOGGER.log(Level.FINE,
                        "[Perspective] cannot sanitize {0}: file not writable", wmFile.getAbsolutePath());
                    continue;
                }
                Files.write(wmFile.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException | RuntimeException ex) {
                // Defensive: never let a sanitization failure break the perspective switch.
                // RuntimeException covers SecurityException (read-only FS, sandboxing) and
                // any unexpected regex / IO runtime errors.
                LOGGER.log(Level.WARNING,
                    "[Perspective] failed to sanitize " + wmFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public Perspective[] getPerspectives() {
        return perspectives;
    }

    @Override
    public Perspective getSelectedPerspective() {
        for (Perspective p : perspectives) {
            if (p.getName().equals(selectedPerspective)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void selectPerspective(Perspective perspective) {
        if (perspective.getName().equals(selectedPerspective)) {
            return;
        }
        openAndCloseMembers(perspective);
        selectedPerspective = perspective.getName();
        NbPreferences.root().put(SELECTED_PERSPECTIVE_PREFERENCE, selectedPerspective);
    }

    private void openAndCloseMembers(final Perspective perspective) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                WindowManager.getDefault().setRole(perspective.getName());
            }
        });
    }
}
