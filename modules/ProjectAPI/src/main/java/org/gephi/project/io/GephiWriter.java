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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectInformation;
import org.gephi.project.api.ProjectMetaData;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.openide.util.NbBundle;

public class GephiWriter {

    static final String VERSION = "0.9";

    public static void writeProject(XMLStreamWriter writer, Project project) throws Exception {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("projectFile");

        // Header
        writer.writeAttribute("version", VERSION);
        writer.writeStartElement("lastModifiedDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        writer.writeCharacters(sdf.format(cal.getTime()));
        writer.writeComment("yyyy-MM-dd HH:mm:ss");
        writer.writeEndElement();
        writer.writeComment("File saved with " + getVersion());

        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
        ProjectMetaData metaData = project.getLookup().lookup(ProjectMetaData.class);

        //Start Project
        writer.writeStartElement("project");
        writer.writeAttribute("name", info.getName());
        writer.writeAttribute("ids", String.valueOf(((ProjectImpl) project).getWorkspaceIds()));

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
        //End Metadata

        writer.writeEndElement();
        //End Project

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    public static void writeWorkspace(XMLStreamWriter writer, Workspace workspace) throws Exception {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("workspace");

        WorkspaceInformation info = workspace.getLookup().lookup(WorkspaceInformation.class);

        writer.writeAttribute("name", info.getName());
        writer.writeAttribute("id", String.valueOf(workspace.getId()));
        if (info.isOpen()) {
            writer.writeAttribute("status", "open");
        } else if (info.isClosed()) {
            writer.writeAttribute("status", "closed");
        } else {
            writer.writeAttribute("status", "invalid");
        }

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    public static void writeWorkspaceChildren(XMLStreamWriter writer, Workspace workspace, WorkspaceXMLPersistenceProvider persistenceProvider) throws Exception {
        String identifier = persistenceProvider.getIdentifier();
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement(identifier);
        writer.writeComment("Persistence from '" + identifier + "' (" + persistenceProvider.getClass().getName() + ")");
        try {
            persistenceProvider.writeXML(writer, workspace);
        } catch (UnsupportedOperationException e) {
        }
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static String getVersion() {
        try {
            return MessageFormat.format(
                    NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), // NOI18N
                    new Object[]{System.getProperty("netbeans.buildnumber")} // NOI18N
            );
        } catch (Exception e) {
            return "?";
        }
    }
}
