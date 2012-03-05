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
package org.gephi.lib.jogl;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Manages the JOGL natives loading. Thanks to Michael Bien, Lilian Chamontin
 * and Kenneth Russell.
 *
 * @author Mathieu Bastan
 */
public class JOGLNativesInstaller extends ModuleInstall {

    private NativeLibInfo nativeLibInfo;    //Compatible nativeLibInfo with OS/Arch
    private boolean exitOnFatalError = true;

    public void restored() {
        if (findCompatibleOsAndArch()) {
            String nativeArch = nativeLibInfo.getSubDirectoryPath();
            File joglDistFolder = InstalledFileLocator.getDefault().locate("modules/lib/" + nativeArch, "org.gephi.lib.jogl", false);
            if (joglDistFolder != null) {
                loadNatives(joglDistFolder);
            } else {
                fatalError(String.format(NbBundle.getMessage(JOGLNativesInstaller.class, "JOGLNativesInstaller_error1"), new Object[]{nativeArch}));
            }
        }
    }

    //=============================================================
    //=============================================================
    private boolean findCompatibleOsAndArch() {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        if (checkOSAndArch(osName, osArch)) {
            return true;
        } else {
            fatalError(String.format(NbBundle.getMessage(JOGLNativesInstaller.class, "JOGLNativesInstaller_error2"), new Object[]{osName, osArch}));
        }
        return false;
    }

    boolean checkOSAndArch(String osName, String osArch) {
        for (int i = 0; i < allNativeLibInfo.length; i++) {
            NativeLibInfo info = allNativeLibInfo[i];
            if (info.matchesOSAndArch(osName, osArch)) {
                nativeLibInfo = info;
                return true;
            }
        }
        return false;
    }

    private void loadNatives(final File nativeLibDir) {
        try {
            // back to the EDT
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    System.out.println("Loading native libraries");
                    // disable JOGL and GlueGen runtime library loading from elsewhere
                    com.sun.opengl.impl.NativeLibLoader.disableLoading();
                    com.sun.gluegen.runtime.NativeLibLoader.disableLoading();
                    // Open GlueGen runtime library optimistically. Note that
                    // currently we do not need this on any platform except X11
                    // ones, because JOGL doesn't use the GlueGen NativeLibrary
                    // class anywhere except the DRIHack class, but if for
                    // example we add JOAL support then we will need this on
                    // every platform.
                    loadLibrary(nativeLibDir, "gluegen-rt");
                    Class driHackClass = null;
                    if (nativeLibInfo.mayNeedDRIHack()) {
                        // Run the DRI hack
                        try {
                            driHackClass = Class.forName("com.sun.opengl.impl.x11.DRIHack");
                            driHackClass.getMethod("begin", new Class[]{}).invoke(null, new Object[]{});
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // Load core JOGL native library
                    loadLibrary(nativeLibDir, "jogl");
                    if (nativeLibInfo.mayNeedDRIHack()) {
                        // End DRI hack
                        try {
                            driHackClass.getMethod("end", new Class[]{}).invoke(null, new Object[]{});
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!nativeLibInfo.isMacOS()) {
                        // borrowed from NativeLibLoader
                        // Must pre-load JAWT on all non-Mac platforms to
                        // ensure references from jogl_awt shared object
                        // will succeed since JAWT shared object isn't in
                        // default library path
                        try {
                            System.loadLibrary("jawt");
                        } catch (UnsatisfiedLinkError ex) {
                            // Accessibility technologies load JAWT themselves; safe to continue
                            // as long as JAWT is loaded by any loader
                            if (ex.getMessage().indexOf("already loaded") == -1) {
                                fatalError(String.format(NbBundle.getMessage(JOGLNativesInstaller.class, "JOGLNativesInstaller_error3"), new Object[]{}));
                            }
                        }
                    } else {
                        //Make sure jawt is loaded on Mac Os X, Issue #542
                        //In Lion the symbolic link to the /Librarires might be missing is some JDK
                        //JAWT is a dependency of jogl_awt so it needs to be accessible
                        //We force to load the library at the default location
                        File defaultJdk = new File("/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK");
                        if (!defaultJdk.exists()) {
                            //Use the current JDK path and remove the /Home
                            String javaHome = System.getProperty("java.home");
                            javaHome = javaHome.substring(0, javaHome.lastIndexOf("Home"));
                            defaultJdk = new File(javaHome);
                        }
                        File libraryPath = new File(defaultJdk, "Libraries");
                        File jawtPath = new File(libraryPath, "libjawt.dylib");
                        if (libraryPath.exists() && jawtPath.exists()) {
                            //Load library file
                            loadLibrary(jawtPath);
                        } else {
                            System.out.println("Issue #452: Can't locate the default Libraries folder to load the"
                                    + "JAWT library. This library is needed as a dependency of jogl_awt and is"
                                    + "normally installed in the JDK. To fix that please make sure to have the"
                                    + "'/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK' path"
                                    + "points to the current Java installation.");
                        }
                    }
                    // Load AWT-specific native code
                    loadLibrary(nativeLibDir, "jogl_awt");
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void loadLibrary(File installDir, String libName) {
        String nativeLibName = nativeLibInfo.getNativeLibName(libName);
        loadLibrary(new File(installDir, nativeLibName));
    }

    private void loadLibrary(File file) {
        try {
            System.load(file.getPath());
        } catch (UnsatisfiedLinkError ex) {
            // should be safe to continue as long as the native is loaded by any loader
            if (ex.getMessage().indexOf("already loaded") == -1) {
                fatalError(String.format(NbBundle.getMessage(JOGLNativesInstaller.class, "JOGLNativesInstaller_error4"), new Object[]{file.getName()}));
            }
        }
    }

    private void fatalError(String error) {
        Exception ex = new Exception(error);
        NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
        DialogDisplayer.getDefault().notify(e);
        if (exitOnFatalError) {
            System.exit(1);
        }
    }

    private static class NativeLibInfo {

        private String osName;
        private String osArch;
        private String osNameAndArchPair;
        private String nativePrefix;
        private String nativeSuffix;

        public NativeLibInfo(String osName, String osArch, String osNameAndArchPair, String nativePrefix, String nativeSuffix) {
            this.osName = osName;
            this.osArch = osArch;
            this.osNameAndArchPair = osNameAndArchPair;
            this.nativePrefix = nativePrefix;
            this.nativeSuffix = nativeSuffix;
        }

        public boolean matchesOSAndArch(String osName, String osArch) {
            if (osName.toLowerCase().startsWith(this.osName)) {
                if ((this.osArch == null)
                        || (osArch.toLowerCase().equals(this.osArch))) {
                    return true;
                }
            }
            return false;
        }

        public boolean matchesNativeLib(String fileName) {
            if (fileName.toLowerCase().endsWith(nativeSuffix)) {
                return true;
            }
            return false;
        }

        public String formatNativeJarName(String nativeJarPattern) {
            return MessageFormat.format(nativeJarPattern, new Object[]{osNameAndArchPair});
        }

        public String getNativeLibName(String baseName) {
            return nativePrefix + baseName + nativeSuffix;
        }

        public boolean isMacOS() {
            return (osName.equals("mac"));
        }

        public boolean mayNeedDRIHack() {
            return (!isMacOS() && !osName.equals("win"));
        }

        public String getSubDirectoryPath() {
            return osNameAndArchPair;
        }
    }
    private static final NativeLibInfo[] allNativeLibInfo = {
        new NativeLibInfo("win", "x86", "windows-i586", "", ".dll"),
        new NativeLibInfo("win", "amd64", "windows-amd64", "", ".dll"),
        new NativeLibInfo("win", "x86_64", "windows-amd64", "", ".dll"),
        new NativeLibInfo("mac", "ppc", "macosx-ppc", "lib", ".jnilib"),
        new NativeLibInfo("mac", "i386", "macosx-universal", "lib", ".jnilib"),
        new NativeLibInfo("mac", "x86_64", "macosx-universal", "lib", ".jnilib"),
        new NativeLibInfo("linux", "i386", "linux-i586", "lib", ".so"),
        new NativeLibInfo("linux", "x86", "linux-i586", "lib", ".so"),
        new NativeLibInfo("linux", "amd64", "linux-amd64", "lib", ".so"),
        new NativeLibInfo("linux", "x86_64", "linux-amd64", "lib", ".so"),
        new NativeLibInfo("sunos", "sparc", "solaris-sparc", "lib", ".so"),
        new NativeLibInfo("sunos", "sparcv9", "solaris-sparcv9", "lib", ".so"),
        new NativeLibInfo("sunos", "x86", "solaris-i586", "lib", ".so"),
        new NativeLibInfo("sunos", "amd64", "solaris-amd64", "lib", ".so"),
        new NativeLibInfo("sunos", "x86_64", "solaris-amd64", "lib", ".so")
    };
}
