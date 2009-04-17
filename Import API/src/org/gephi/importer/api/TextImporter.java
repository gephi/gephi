/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.importer.api;

import java.io.BufferedReader;

/**
 *
 * @author Mathieu Bastian
 */
public interface TextImporter extends Importer {

    public void importData(BufferedReader reader, ImportContainer containter) throws ImportException;
}
