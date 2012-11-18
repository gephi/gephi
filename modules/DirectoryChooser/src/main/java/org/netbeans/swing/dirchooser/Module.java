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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 *
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FileChooserUI;
import org.openide.modules.ModuleInstall;

/**
 * Registers the directory chooser in NetBeans.
 *
 * @author Soot Phengsy
 */
public class Module extends ModuleInstall {
    
    private static final String KEY = "FileChooserUI"; // NOI18N
    private static Class<? extends FileChooserUI> originalImpl;
    private static PropertyChangeListener pcl;
    
    private static final String QUICK_CHOOSER_NAME = 
            "org.netbeans.modules.quickfilechooser.ChooserComponentUI";
    
    private static final String FORCE_STANDARD_CHOOSER = "standard-file-chooser"; // NOI18N

    @Override public void restored() {
        install();
    }

    @Override public void uninstalled() {
        uninstall();
    }
        
    public static void install() {
        // don't install directory chooser if standard chooser is desired
        if (isStandardChooserForced()) {
            return;
        }
        final UIDefaults uid = UIManager.getDefaults();
        originalImpl = (Class<? extends FileChooserUI>) uid.getUIClass(KEY);
        Class impl = DelegatingChooserUI.class;
        final String val = impl.getName();
        // don't install dirchooser if quickfilechooser is present
        if (!isQuickFileChooser(uid.get(KEY))) {
            uid.put(KEY, val);
            // To make it work in NetBeans too:
            uid.put(val, impl);
        }
        // #61147: prevent NB from switching to a different UI later (under GTK):
        uid.addPropertyChangeListener(pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                Object className = uid.get(KEY);
                if ((name.equals(KEY) || name.equals("UIDefaults")) && !val.equals(className)
                        && !isQuickFileChooser(className)) {
                    uid.put(KEY, val);
                }
            }
        });
    }
    
    public static void uninstall() {
        if (isInstalled()) {
            assert pcl != null;
            UIDefaults uid = UIManager.getDefaults();
            uid.removePropertyChangeListener(pcl);
            pcl = null;
            String val = originalImpl.getName();
            uid.put(KEY, val);
            uid.put(val, originalImpl);
            originalImpl = null;
        }
    }
    
    public static boolean isInstalled() {
        return originalImpl != null;
    }
    
    static Class<? extends FileChooserUI> getOrigChooser () {
        return originalImpl;
    }
    
    private static boolean isQuickFileChooser (Object className) {
        return QUICK_CHOOSER_NAME.equals(className);
    }
    
    private static boolean isStandardChooserForced () {
        return Boolean.getBoolean(FORCE_STANDARD_CHOOSER);
    }
    
}
