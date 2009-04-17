/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.importer.api;

import org.w3c.dom.Document;

/**
 *
 * @author Mathieu Bastian
 */
public interface XMLImporter extends Importer {

    public void importData(Document document) throws ImportException;
}
