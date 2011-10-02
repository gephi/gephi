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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.impl.WorkspaceProviderImpl;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectInformation;
import org.gephi.project.api.ProjectMetaData;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu
 */
public class GephiWriter implements Cancellable {

    private int tasks = 0;
    private Map<String, WorkspacePersistenceProvider> providers;

    public GephiWriter() {
        providers = new LinkedHashMap<String, WorkspacePersistenceProvider>();
        for (WorkspacePersistenceProvider w : Lookup.getDefault().lookupAll(WorkspacePersistenceProvider.class)) {
            try {
                String id = w.getIdentifier();
                if (id != null && !id.isEmpty()) {
                    providers.put(w.getIdentifier(), w);
                }
            } catch (Exception e) {
            }
        }
    }

    public void writeAll(Project project, XMLStreamWriter writer) throws Exception {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("gephiFile");
        writer.writeAttribute("version", "0.7");
        writer.writeComment("File saved from Gephi 0.8");

        writeCore(writer);
        writeProject(writer, project);

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    public void writeCore(XMLStreamWriter writer) throws Exception {
        //Core
        writer.writeStartElement("core");
        writer.writeAttribute("tasks", String.valueOf(tasks));
        writer.writeStartElement("lastModifiedDate");

        //LastModifiedDate
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        writer.writeCharacters(sdf.format(cal.getTime()));
        writer.writeComment("yyyy-MM-dd HH:mm:ss");

        //Append
        writer.writeEndElement();
        writer.writeEndElement();
    }

    public void writeProject(XMLStreamWriter writer, Project project) throws Exception {
        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
        ProjectMetaData metaData = project.getLookup().lookup(ProjectMetaData.class);
        WorkspaceProviderImpl workspaces = project.getLookup().lookup(WorkspaceProviderImpl.class);

        writer.writeStartElement("project");
        writer.writeAttribute("name", info.getName());

        //MetaData
        writer.writeStartElement("metadata");

        writer.writeStartElement("title");
        writer.writeCharacters(metaData.getTitle());
        writer.writeEndElement();

        writer.writeStartElement("keywords");
        writer.writeCharacters(metaData.getKeywords());
        writer.writeEndElement();

        writer.writeStartElement("description");
        writer.writeCharacters(metaData.getDescription());
        writer.writeEndElement();

        writer.writeStartElement("author");
        writer.writeCharacters(metaData.getAuthor());
        writer.writeEndElement();

        writer.writeEndElement();

        //Workspaces
        writer.writeStartElement("workspaces");
        for (Workspace ws : workspaces.getWorkspaces()) {
            writeWorkspace(writer, ws);
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    public void writeWorkspace(XMLStreamWriter writer, Workspace workspace) throws Exception {
        WorkspaceInformation info = workspace.getLookup().lookup(WorkspaceInformation.class);

        writer.writeStartElement("workspace");
        writer.writeAttribute("name", info.getName());
        if (info.isOpen()) {
            writer.writeAttribute("status", "open");
        } else if (info.isClosed()) {
            writer.writeAttribute("status", "closed");
        } else {
            writer.writeAttribute("status", "invalid");
        }

        writeWorkspaceChildren(writer, workspace);

        writer.writeEndElement();
    }

    public void writeWorkspaceChildren(XMLStreamWriter writer, Workspace workspace) throws Exception {
        for (WorkspacePersistenceProvider pp : providers.values()) {
            try {
                writer.writeComment("Persistence from " + pp.getClass().getName());
                pp.writeXML(writer, workspace);
            } catch (UnsupportedOperationException e) {
            }
        }
    }

    public boolean cancel() {
        return true;
    }
}
