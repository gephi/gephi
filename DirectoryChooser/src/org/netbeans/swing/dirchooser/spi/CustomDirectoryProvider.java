package org.netbeans.swing.dirchooser.spi;


import java.io.File;
import javax.swing.Icon;


/**
 * Defines icon and required file content of custom directory. Custom directory is invoked always as
 * the result of <code>jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);</code>. As
 * of implementation detail, {@link CustomDirectoryProvider#isEnabled isEnabled()} must return true
 * in the process of creating the JFileChooser. This means that there should be an static setter method
 * setEnabled(boolean) and it must be set to true before the
 * <code>jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);</code> will be called.
 *
 * @author Martin Škurla
 */
public interface CustomDirectoryProvider {
    /**
     * Determines if custom directory provider is enabled.
     * 
     * @return true if custom directory provider is enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Determines if given directory represents valid custom directory. This can be determined by the
     * content of given directory. Returns false if given argument isn't directory.
     * 
     * @param directory input file
     * 
     * @return true if given directoru represent valid custom directory, false otherwise
     */
    boolean isValidCustomDirectory(File directory);

    /**
     * Returns icon for custom directory. Icon is showd in the JFileChooser dialog.
     * 
     * @return icon for custom directory
     */
    Icon getCustomDirectoryIcon();
}
