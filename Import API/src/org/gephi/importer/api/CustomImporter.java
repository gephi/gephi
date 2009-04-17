/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.importer.api;

import java.io.InputStream;

/**
 *
 * @author Mathieu Bastian
 */
public interface CustomImporter {

    public void importData(InputStream stream) throws ImportException;
}
