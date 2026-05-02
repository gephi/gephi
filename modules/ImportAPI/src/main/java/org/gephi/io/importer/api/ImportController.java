/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/

package org.gephi.io.importer.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.Workspace;
import org.openide.filesystems.FileObject;

/**
 * Manage and control the import execution flow.
 * <p>
 * This controller is a singleton and can therefore be found in Lookup:
 * <pre>ImportController ic = Lookup.getDefault().lookup(ImportController.class);</pre>
 *
 * @author Mathieu Bastian
 */
public interface ImportController {

    /**
     * Imports a file by automatically detecting the appropriate importer based on the file extension.
     * <p>
     * If the file is a supported archive (zip, gz, bz2), it is extracted first and the content file is imported.
     *
     * @param file the file to import
     * @return the container holding the imported data, or {@code null} if no matching importer was found
     * @throws FileNotFoundException if the file does not exist
     */
    Container importFile(File file) throws FileNotFoundException;

    /**
     * Imports a file using the specified importer.
     * <p>
     * If the file is a supported archive (zip, gz, bz2), it is extracted first and the content file is imported.
     *
     * @param file     the file to import
     * @param importer the importer to use
     * @return the container holding the imported data, or {@code null} if the import failed
     * @throws FileNotFoundException if the file does not exist
     */
    Container importFile(File file, FileImporter importer) throws FileNotFoundException;

    /**
     * Imports data from a reader using the specified importer.
     *
     * @param reader   the reader providing the data to import
     * @param importer the importer to use
     * @return the container holding the imported data, or {@code null} if the import failed
     */
    Container importFile(Reader reader, FileImporter importer);

    /**
     * Imports data from an input stream using the specified importer.
     *
     * @param stream   the input stream providing the data to import
     * @param importer the importer to use
     * @return the container holding the imported data, or {@code null} if the import failed
     */
    Container importFile(InputStream stream, FileImporter importer);

    /**
     * Imports data using the specified wizard importer.
     * <p>
     * Wizard importers generate data without requiring a file or database source, for example from user input or
     * generated content.
     *
     * @param importer the wizard importer to execute
     * @return the container holding the imported data, or {@code null} if the import failed
     */
    Container importWizard(WizardImporter importer);

    /**
     * Returns a file importer that matches the given file object, or {@code null} if none is found.
     * <p>
     * If the file object represents a supported archive, it is extracted first and the content file is matched.
     *
     * @param fileObject the file object to match an importer for
     * @return a matching {@link FileImporter}, or {@code null} if none was found
     */
    FileImporter getFileImporter(FileObject fileObject);

    /**
     * Returns a file importer that matches the given file, or {@code null} if none is found.
     *
     * @param file the file to match an importer for
     * @return a matching {@link FileImporter}, or {@code null} if none was found
     */
    FileImporter getFileImporter(File file);

    /**
     * Returns a file importer matching the given importer name or file extension, or {@code null} if none is found.
     * <p>
     * The {@code importerName} can be either a file extension (with or without a leading dot) or the name of a
     * registered importer.
     *
     * @param importerName the importer name or file extension to look up
     * @return a matching {@link FileImporter}, or {@code null} if none was found
     */
    FileImporter getFileImporter(String importerName);

    /**
     * Imports data from a database using the specified database importer.
     *
     * @param database the database connection parameters
     * @param importer the database importer to use
     * @return the container holding the imported data, or {@code null} if the import failed
     */
    Container importDatabase(Database database, DatabaseImporter importer);

    /**
     * Processes a container using the default processor and creates a new workspace.
     * <p>
     * The default processor is retrieved from the global Lookup.
     *
     * @param container the container with imported data to process
     * @return the workspace created by the processor
     * @throws RuntimeException if no default processor is found or the processor fails
     */
    Workspace process(Container container);

    /**
     * Processes a container using the specified processor and workspace.
     * <p>
     * If {@code workspace} is {@code null}, the processor will create a new workspace. Auto-scaling is applied to the
     * container if enabled.
     *
     * @param container the container with imported data to process
     * @param processor the processor to use
     * @param workspace the target workspace, or {@code null} to let the processor create one
     * @return the workspace populated by the processor
     * @throws RuntimeException if the processor does not return exactly one workspace
     */
    Workspace process(Container container, Processor processor, Workspace workspace);

    /**
     * Processes multiple containers using the specified processor and workspace.
     * <p>
     * If {@code workspace} is {@code null}, the processor will create new workspaces. Auto-scaling is applied to each
     * container individually if enabled.
     *
     * @param containers the containers with imported data to process
     * @param processor  the processor to use
     * @param workspace  the target workspace, or {@code null} to let the processor create workspaces
     * @return the workspaces populated by the processor
     * @throws RuntimeException if the processor does not return any workspace
     */
    Workspace[] process(Container[] containers, Processor processor, Workspace workspace);

    /**
     * Returns all file types supported by the registered file importers.
     *
     * @return an array of supported {@link FileType} instances
     */
    FileType[] getFileTypes();

    /**
     * Returns whether the given file is supported by any registered file importer.
     * <p>
     * Archive files (zip, gz, bz2) are always considered supported.
     *
     * @param file the file to check
     * @return {@code true} if the file is supported, {@code false} otherwise
     */
    boolean isFileSupported(File file);

    /**
     * Returns the UI component associated with the given importer, or {@code null} if none is registered.
     *
     * @param importer the importer to look up a UI for
     * @return the matching {@link ImporterUI}, or {@code null} if none was found
     */
    ImporterUI getUI(Importer importer);

    /**
     * Returns the wizard UI component associated with the given importer, or {@code null} if none is registered.
     *
     * @param importer the importer to look up a wizard UI for
     * @return the matching {@link ImporterWizardUI}, or {@code null} if none was found
     */
    ImporterWizardUI getWizardUI(Importer importer);
}
