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
package org.gephi.io.importer.spi;

import org.gephi.io.importer.api.FileType;
import org.openide.filesystems.FileObject;

/**
 * Importer builder specific for {@link FileImporter}.
 *
 * @author Mathieu Bastian
 */
public interface FileImporterBuilder extends ImporterBuilder {

    /**
     * Builds a new file importer instance, ready to be used.
     *
     * @return a new file importer
     */
    @Override
    public FileImporter buildImporter();

    /**
     * Get default file types this importer can deal with.
     *
     * @return an array of file types this importer can read
     */
    public FileType[] getFileTypes();

    /**
     * Returns <code>true</code> if this importer can import
     * <code>fileObject</code>. Called from controllers to identify dynamically
     * which importers can be used for a particular file format.
     * <p>
     * Use <code>FileObject.getExt()</code> to retrieve file extension. Matching
     * can be done not only with metadata but also with file content. The
     * <code>fileObject</code> can be read in that way.
     *
     * @param fileObject the file in input
     * @return <code>true</code> if the importer is compatible with
     * <code>fileObject</code> or <code>false</code> otherwise
     */
    public boolean isMatchingImporter(FileObject fileObject);
}
