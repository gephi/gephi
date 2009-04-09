/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.jogl;

import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;


/**
 *
 * @author Mathieu
 */
public class JOGLWrapper {

    private static JOGLWrapper instance;

    public static synchronized JOGLWrapper getInstance() {
        if (instance == null) {
            instance = new JOGLWrapper();
        }
        return instance;
    }

    //--------------------------------------
    private boolean isInitOk = false;
    private NativeLibInfo nativeLibInfo;    //Compatible nativeLibInfo with OS/Arch

    private JOGLWrapper() {
        init();
    }

    private void init()
    {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        if (checkOSAndArch(osName, osArch)) {
            this.isInitOk = true;
        } else {
            System.err.println("Init failed : Unsupported os / arch ( " + osName + " / " + osArch + " )");
        }

        //
        //ClassPath.
        File file = FileUtil.normalizeFile(new File("JOGLWrapper/release/modules/lib"));
        FileObject fo = FileUtil.toFileObject(file);

        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
        
        System.out.println(fo.getPath());
       /*File f = FileUtil.toFile(Repository.getDefault().getDefaultFileSystem().getRoot());
        System.out.println(f.getPath());
        String subDirectory = nativeLibInfo.getSubDirectoryPath();*/
    }

    private boolean checkOSAndArch(String osName, String osArch) {
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
                Logger.getLogger(JOGLWrapper.class.getName()).severe("Unable to load " + nativeLibName);
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

        public String getSubDirectoryPath()
        {
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
    
    // Library names computed once the jar comes down.
    // The signatures of these native libraries are checked before
    // installing them.
    private String[] nativeLibNames;
}
