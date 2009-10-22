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
package org.gephi.io.importer;

import java.io.BufferedReader;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.logging.Report;

/**
 * Importers interface for importing data from Text files.
 *
 * @author Mathieu Bastian
 */
public interface TextImporter extends FileFormatImporter {

    /**
     * Import data from <code>reader</code> and push it to <code>container</code>. Informations, logs and
     * issues are pushed to <code>report</code> for further analysis and verification.
     * @param reader the reader on text data
     * @param container container loading interface
     * @param report the import report for logging informations and issues
     * @throws java.lang.Exception for catching eventual exceptions
     */
    public void importData(BufferedReader reader, ContainerLoader container, Report report) throws Exception;
}
