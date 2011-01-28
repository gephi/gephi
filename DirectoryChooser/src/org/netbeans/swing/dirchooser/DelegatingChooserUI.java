/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.swing.dirchooser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.metal.MetalFileChooserUI;
import org.openide.util.Utilities;

/** Placeholder ComponentUI that just delegates to other FileChooserUIs
 * based on what selection mode is set in JFileChooser.
 *
 * @author Dafe Simonek
 */
public class DelegatingChooserUI extends ComponentUI {
    
    static final String USE_SHELL_FOLDER = "FileChooser.useShellFolder";
    static final String NB_USE_SHELL_FOLDER = "nb.FileChooser.useShellFolder";
    static final String START_TIME = "start.time";
    
    private static boolean firstTime = true;

    public static ComponentUI createUI(JComponent c) {
        JFileChooser fc = (JFileChooser)c;

        // #109703 - don't use shell folder on JDK versions interval <1.6.0_02, 1.6.0_10>,
        // it's terribly slow on Windows due to JDK bug
        if (Utilities.isWindows()) {
            if (System.getProperty(NB_USE_SHELL_FOLDER) != null) {
                fc.putClientProperty(USE_SHELL_FOLDER, Boolean.getBoolean(NB_USE_SHELL_FOLDER));
            } else {
                String jv = System.getProperty("java.version");
                jv = jv.split("-", 2)[0];
                if ("1.6.0_02".compareToIgnoreCase(jv) <= 0 &&
                        "1.6.0_10".compareToIgnoreCase(jv) >= 0) {
                    if (!Boolean.TRUE.equals(fc.getClientProperty(USE_SHELL_FOLDER))) {
                        fc.putClientProperty(USE_SHELL_FOLDER, Boolean.FALSE);
                    }
                }
            }
        }

        // mark start time, just once during init (code can be run multiple times
        // because of property listenign below)
        if (fc.getClientProperty(START_TIME) == null) {
            fc.putClientProperty(START_TIME, Long.valueOf(System.currentTimeMillis()));
        }
        
        Class<? extends FileChooserUI> chooser = getCurChooser(fc);
        ComponentUI compUI;
        try {
            Method createUIMethod = chooser.getMethod("createUI", JComponent.class);
            compUI = (ComponentUI) createUIMethod.invoke(null, fc);
        } catch (Exception exc) {
            Logger.getLogger(DelegatingChooserUI.class.getName()).log(Level.FINE,
                    "Could not instantiate custom chooser, fallbacking to Metal", exc);
            compUI = MetalFileChooserUI.createUI(c);
        }
        
        // listen to sel mode changes and select correct chooser by invoking
        // filechooser.updateUI() which triggers this createUI again 
        if (firstTime) {
            fc.addPropertyChangeListener(
                    JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY,
                    new PropertyChangeListener () {
                        public void propertyChange(PropertyChangeEvent evt) {
                            JFileChooser fileChooser = (JFileChooser)evt.getSource();
                            fileChooser.updateUI();
                        }
                    }
            );
        }
        
        return compUI;
    }

    /** Returns dirchooser for DIRECTORIES_ONLY, default filechooser for other
     * selection modes.
     */
    private static Class<? extends FileChooserUI> getCurChooser (JFileChooser fc) {
        if (fc.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
            return DirectoryChooserUI.class;
        }
        return Module.getOrigChooser();
    }

}
