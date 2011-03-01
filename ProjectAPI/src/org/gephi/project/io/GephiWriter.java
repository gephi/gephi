/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
