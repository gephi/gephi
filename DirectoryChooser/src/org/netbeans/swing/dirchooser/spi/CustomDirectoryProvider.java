/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/

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
