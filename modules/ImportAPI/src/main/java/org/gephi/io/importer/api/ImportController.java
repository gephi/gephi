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
 * @author Mathieu Bastian
 */
public interface ImportController {

    public Container importFile(File file) throws FileNotFoundException;

    public Container importFile(File file, FileImporter importer) throws FileNotFoundException;

    public Container importFile(Reader reader, FileImporter importer);

    public Container importFile(InputStream stream, FileImporter importer);
    
    public Container importWizard(WizardImporter importer);

    public FileImporter getFileImporter(FileObject fileObject);
    
    public FileImporter getFileImporter(File file);

    public FileImporter getFileImporter(String importerName);

    public Container importDatabase(Database database, DatabaseImporter importer);

    public void process(Container container);

    public void process(Container container, Processor processor, Workspace workspace);

    public void process(Container[] containers, Processor processor, Workspace workspace);

    public FileType[] getFileTypes();

    public boolean isFileSupported(File file);

    public ImporterUI getUI(Importer importer);

    public ImporterWizardUI getWizardUI(Importer importer);
}
