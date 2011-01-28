/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.io.generator.spi;

import javax.swing.JPanel;

/**
 * Defines settings panel for a particular generator. Responsible for loading and
 * saving settings to the <code>Generator</code> instance.
 * <p>
 * Note that panels are compatible with <code>ValidationAPI</code>. If the
 * <code>JPanel</code> returned from {@link #getPanel()} is a <code>ValidationPanel</code>
 * instance, the dialog OK button will be linked to the <code>ValidationGroup</code>.
 * @author Mathieu Bastian
 */
public interface GeneratorUI {

    /**
     * Returns the panel settings.
     * @return          the panel settings
     */
    public JPanel getPanel();

    /**
     * Push the generator instance to get settings values.
     * @param generator the generator instance that is to be configured
     */
    public void setup(Generator generator);

    /**
     * Notify UI that generator settings panel has been closed and that
     * settings values can be written into current generator instance.
     */
    public void unsetup();
}
