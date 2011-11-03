/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.mycompany.installer.utils.applications;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;

/**
 *
 * @author Dmitry Lipin
 */
public class NetBeansRCPUtils {

    
    /**
     * Get resolved application user directory
     * @param appLocation Application home directory
     * @throws IOException if can`t get application default userdir
     */
    public static File getApplicationUserDirFile(File appLocation) throws IOException {
        String dir = getApplicationUserDir(appLocation);
        String userHome = System.getProperty("user.home");
        if(SystemUtils.isWindows()) {
            WindowsNativeUtils wnu = (WindowsNativeUtils) SystemUtils.getNativeUtils();
            WindowsRegistry reg = wnu.getWindowsRegistry();
            String key = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders";
            try {
            if(reg.keyExists(reg.HKCU, key) &&
                    reg.valueExists(reg.HKCU, key, "AppData")) {
                userHome = reg.getStringValue(reg.HKCU, key, "AppData", false);

            }
            } catch (NativeException e) {
                LogManager.log(e);
            }
        }
        dir = dir.replace(USER_HOME_TOKEN, userHome);
        dir = dir.replace(APPNAME_TOKEN, getApplicationName(appLocation));
        return new File(dir);
    }
    
    /**
     * Get application user directory as it is written in application`s configuration file
     * @param appLocation Application home directory
     * @throws IOException if can`t get  default userdir
     */
    public static String getApplicationUserDir(File appLocation) throws IOException {
        File []confFiles = new File(appLocation, "etc").listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".conf");
            }
        });
        File conf = null;
        if(confFiles.length == 1) {
            conf = confFiles[0];
        } else if(confFiles.length >2) {
            for(File f : confFiles) {
                String prefix = f.getName().substring(0, f.getName().indexOf(".conf"));                
                if((SystemUtils.isUnix() && new File(appLocation, "bin/" + prefix).exists()) ||
                   (SystemUtils.isWindows() && new File(appLocation, "bin/" + prefix + ".exe").exists())) {
                    conf = f;
                    break;
                }                
            }
        }
        if(conf == null) {
            return null;
        }

        String contents = FileUtils.readFile(conf);
        Matcher matcher = Pattern.compile(
                NEW_LINE_PATTERN + SPACES_PATTERN +
                (SystemUtils.isMacOS() ? DEFAULT_USERDIR_MAC : DEFAULT_USERDIR) +
                "\"(.*?)\"").matcher(contents);
        if(matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException(StringUtils.format(
                    ERROR_CANNOT_GET_USERDIR_STRING,conf));
        }
    }

    /**
     * Get application name - i.e. <name> in bin/<name>.exe and etc/<name>.conf
     * @param appLocation Application home directory
     */
    public static String getApplicationName(File appLocation)  {
        File []confFiles = new File(appLocation, "etc").listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".conf");
            }
        });

        if(confFiles.length == 1) {
            String name = confFiles[0].getName();
            return name.substring(0, name.indexOf(".conf"));
        } else if(confFiles.length >2) {
            for(File f : confFiles) {
                String name = f.getName();
                String prefix = name.substring(0, name.indexOf(".conf"));
                if((SystemUtils.isUnix() && new File(appLocation, "bin/" + prefix).exists()) ||
                   (SystemUtils.isWindows() && new File(appLocation, "bin/" + prefix + ".exe").exists())) {
                    return prefix;
                }
            }
        }
        return null;
    }
    

    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private NetBeansRCPUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    
    public static final String NEW_LINE_PATTERN =
            "[\r\n|\n|\r]"; // NOI18N
    public static final String SPACES_PATTERN =
            "\\ *"; // NOI18N
    
    public static final String DEFAULT_USERDIR =
            "default_userdir="; // NOI18N
    public static final String DEFAULT_USERDIR_MAC =
            "default_mac_userdir="; // NOI18N
    
    public static final String USER_HOME_TOKEN =
            "${HOME}"; // NOI18N
    public static final String APPNAME_TOKEN =
            "${APPNAME}"; // NOI18N
    
    public static final String ERROR_CANNOT_GET_USERDIR_STRING =
            ResourceUtils.getString(NetBeansRCPUtils.class,
            "NU.error.cannot.get.userdir");//NOI18N
    
    
}
