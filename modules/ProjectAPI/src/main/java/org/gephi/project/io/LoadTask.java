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
package org.gephi.project.io;

import com.sun.org.apache.xerces.internal.util.XMLChar;
import java.io.File;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.ProjectInformationImpl;
import org.gephi.project.api.Project;
import org.gephi.project.impl.ProjectControllerImpl;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class LoadTask implements LongTask, Runnable {

    private File file;
    private GephiReader gephiReader;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public LoadTask(File file) {
        this.file = file;
    }

    public void run() {
        try {
            Progress.start(progressTicket);
            Progress.setDisplayName(progressTicket, NbBundle.getMessage(LoadTask.class, "LoadTask.name"));
            FileObject fileObject = FileUtil.toFileObject(file);
            if (FileUtil.isArchiveFile(fileObject)) {
                //Unzip
                fileObject = FileUtil.getArchiveRoot(fileObject).getChildren()[0];
            }

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
                inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
            }
            inputFactory.setXMLReporter(new XMLReporter() {

                @Override
                public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
                    System.out.println("Error:" + errorType + ", message : " + message);
                }
            });
            InputStreamReader isReader = new InputStreamReader(fileObject.getInputStream(), "UTF-8");
            Xml10FilterReader filterReader = new Xml10FilterReader(isReader);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(filterReader);

            if (!cancel) {
                //Project instance
                Project project = new ProjectImpl();
                project.getLookup().lookup(ProjectInformationImpl.class).setFile(file);

                //GephiReader
                gephiReader = new GephiReader();
                project = gephiReader.readAll(reader, project);

                //Add project
                if (!cancel) {
                    ProjectControllerImpl pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
                    pc.openProject(project);
                }
            }
            Progress.finish(progressTicket);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex instanceof GephiFormatException) {
                throw (GephiFormatException) ex;
            }
            throw new GephiFormatException(GephiReader.class, ex);
        }
    }

    public boolean cancel() {
        cancel = true;
        if (gephiReader != null) {
            gephiReader.cancel();
        }
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    /**
     * {@link FilterReader} to skip invalid xml version 1.0 characters. Valid
     * Unicode chars for xml version 1.0 according to http://www.w3.org/TR/xml are
     * #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD], [#x10000-#x10FFFF] . In
     * other words - any Unicode character, excluding the surrogate blocks, FFFE,
     * and FFFF.
     * <p>
     * More details on the <a href="http://info.tsachev.org/2009/05/skipping-invalid-xml-character-with.html">blog</a>
     */
    public class Xml10FilterReader extends FilterReader {

        /**
         * Creates filter reader which skips invalid xml characters.
         * 
         * @param in original reader
         */
        public Xml10FilterReader(Reader in) {
            super(in);
        }

        /**
         * Every overload of {@link Reader#read()} method delegates to this one so
         * it is enough to override only this one. <br />
         * To skip invalid characters this method shifts only valid chars to left
         * and returns decreased value of the original read method. So after last
         * valid character there will be some unused chars in the buffer.
         * 
         * @return Number of read valid characters or <code>-1</code> if end of the
         *         underling reader was reached.
         */
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int read = super.read(cbuf, off, len);
            /*
             * If read chars are -1 then we have reach the end of the reader.
             */
            if (read == -1) {
                return -1;
            }
            /*
             * pos will show the index where chars should be moved if there are gaps
             * from invalid characters.
             */
            int pos = off - 1;

            for (int readPos = off; readPos < off + read; readPos++) {
                if (XMLChar.isValid(cbuf[readPos])) {
                    pos++;
                } else {
                    continue;
                }
                /*
                 * If there is gap(s) move current char to its position.
                 */
                if (pos < readPos) {
                    cbuf[pos] = cbuf[readPos];
                }
            }
            /*
             * Number of read valid characters.
             */
            return pos - off + 1;
        }
    }
}
