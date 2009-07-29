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
package org.gephi.io.project;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.gephi.project.api.Project;
import org.gephi.workspace.api.Workspace;
import org.openide.util.Cancellable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu
 */
public class GephiWriter implements Cancellable {

    private int tasks = 0;
    private Document doc;

    private Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        document.setXmlVersion("1.0");
        document.setXmlStandalone(true);

        return document;
    }



    public Document writeAll(Project project) throws Exception {
        doc = createDocument();

        Element root = writeCore();
        Element projectE = writeProject(project);

        root.appendChild(projectE);
        doc.appendChild(root);

        return doc;
    }

    public Element writeCore() throws Exception {
        Element root = doc.createElement("gephiFile");
        root.setAttribute("version", "0.7");
        root.appendChild(doc.createComment("File saved from Gephi 0.7"));

        //Core
        Element core = doc.createElement("core");
        core.setAttribute("tasks", String.valueOf(tasks));
        Element lastModifiedDate = doc.createElement("lastModifiedDate");

        //LastModifiedDate
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        lastModifiedDate.setTextContent(sdf.format(cal.getTime()));
        lastModifiedDate.appendChild(doc.createComment("yyyy-MM-dd HH:mm:ss"));

        //Append
        core.appendChild(lastModifiedDate);
        root.appendChild(core);

        return root;
    }

    public Element writeProject(Project project) throws Exception {
        Element projectE = doc.createElement("project");
        projectE.setAttribute("name", project.getName());

        //MetaData
        Element projectMetaDataE = doc.createElement("metadata");
        Element titleE = doc.createElement("title");
        titleE.setTextContent(project.getMetaData().getTitle());
        Element keywordsE = doc.createElement("keywords");
        keywordsE.setTextContent(project.getMetaData().getKeywords());
        Element descriptionE = doc.createElement("description");
        descriptionE.setTextContent(project.getMetaData().getDescription());
        Element authorE = doc.createElement("author");
        authorE.setTextContent(project.getMetaData().getAuthor());
        projectMetaDataE.appendChild(titleE);
        projectMetaDataE.appendChild(authorE);
        projectMetaDataE.appendChild(keywordsE);
        projectMetaDataE.appendChild(descriptionE);
        projectE.appendChild(projectMetaDataE);

        //Workspaces
        Element workspacesE = doc.createElement("workspaces");
        for (Workspace ws : project.getWorkspaces()) {
            workspacesE.appendChild(writeWorkspace(ws));
        }
        projectE.appendChild(workspacesE);

        return projectE;
    }

    public Element writeWorkspace(Workspace workspace) throws Exception {
        Element workspaceE = doc.createElement("workspace");
        workspaceE.setAttribute("name", workspace.getName());
        if (workspace.isOpen()) {
            workspaceE.setAttribute("status", "open");
        } else if (workspace.isClosed()) {
            workspaceE.setAttribute("status", "closed");
        } else {
            workspaceE.setAttribute("status", "invalid");
        }

        return workspaceE;
    }

    public boolean cancel() {
        return true;
    }
}
