/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.io.importer.spi;

import javax.swing.JPanel;

/**
 *
 * @author Mathieu Bastian
 */
public interface ImporterUI {

    public void setup(Importer importer);

    public JPanel getPanel();

    public void unsetup();

    public String getDisplayName();

    public boolean isUIForImporter(Importer importer);
}
