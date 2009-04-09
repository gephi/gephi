/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.jogl;

import java.io.File;
import java.text.MessageFormat;
import javax.swing.SwingUtilities;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;

/**
 * Manages the JOGL natives loading. Thanks to Michael Bien, Lilian Chamontin and Kenneth Russell.
 * 
 * @author Mathieu Bastan
 */
public class JOGLNativesInstaller extends ModuleInstall {

    private NativeLibInfo nativeLibInfo;    //Compatible nativeLibInfo with OS/Arch

    public void restored() {
        if (findCompatibleOsAndArch()) {

            String nativeArch = nativeLibInfo.getSubDirectoryPath();
            File joglDistFolder = InstalledFileLocator.getDefault().locate("modules/lib/" + nativeArch, null, false);
            if (joglDistFolder != null) {
                loadNatives(joglDistFolder);
            } else {
                System.err.println("Init failed: Impossible to locate natives for " + nativeArch + ".");
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
            System.err.println("Init failed : Unsupported os / arch ( " + osName + " / " + osArch + " )");
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
        // back to the EDT
        SwingUtilities.invokeLater(new Runnable() {

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

                if (!nativeLibInfo.isMacOS()) { // borrowed from NativeLibLoader
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
                            System.err.println("Unable to load JAWT");
                            throw ex;
                        }
                    }
                }

                // Load AWT-specific native code
                loadLibrary(nativeLibDir, "jogl_awt");
            }
        });
    }

    private void loadLibrary(File installDir, String libName) {
        String nativeLibName = nativeLibInfo.getNativeLibName(libName);
        try {
            System.load(new File(installDir, nativeLibName).getPath());
        } catch (UnsatisfiedLinkError ex) {
            // should be safe to continue as long as the native is loaded by any loader
            if (ex.getMessage().indexOf("already loaded") == -1) {
                System.err.println("Unable to load " + nativeLibName);
                throw ex;
            }
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
                if ((this.osArch == null) ||
                        (osArch.toLowerCase().equals(this.osArch))) {
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
