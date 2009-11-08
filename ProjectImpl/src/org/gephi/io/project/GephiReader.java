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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.gephi.project.ProjectImpl;
import org.gephi.project.api.Project;
import org.gephi.workspace.WorkspaceImpl;
import org.gephi.workspace.api.Workspace;
import org.openide.util.Cancellable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class GephiReader implements Cancellable {

    private ProjectImpl project;

    public boolean cancel() {
        return true;
    }

    public Project readAll(Element root, Project project) throws Exception {
        //XPath
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        //Calculate the task max
        readCore(xpath, root);

        //Project
        this.project = (ProjectImpl)project;
        XPathExpression exp = xpath.compile("./project");
        Element projectE = (Element) exp.evaluate(root, XPathConstants.NODE);
        readProject(xpath, projectE);
        return project;
    }

    public void readCore(XPath xpath, Element root) throws Exception {
        XPathExpression exp = xpath.compile("./core");
        Element coreE = (Element) exp.evaluate(root, XPathConstants.NODE);
        int max = Integer.parseInt(coreE.getAttribute("tasks"));
        System.out.println(max);
    }

    public void readProject(XPath xpath, Element projectE) throws Exception {
        project.setName(projectE.getAttribute("name"));

        //WorkSpaces
        XPathExpression exp = xpath.compile("./workspaces/workspace");
        NodeList workSpaceList = (NodeList) exp.evaluate(projectE, XPathConstants.NODESET);

        for (int i = 0; i < workSpaceList.getLength(); i++) {
            Element workspaceE = (Element) workSpaceList.item(i);
            Workspace workspace = readWorkSpace(xpath, workspaceE);

            //Current workspace
            if (workspace.isOpen()) {
                project.setCurrentWorkspace(workspace);
            }
        }
    }

    public Workspace readWorkSpace(XPath xpath, Element workspaceE) throws Exception {
        WorkspaceImpl workspace = project.newWorkspace();

        //Name
        workspace.setName(workspaceE.getAttribute("name"));

        //Status
        String workspaceStatus = workspaceE.getAttribute("status");
        if (workspaceStatus.equals("open")) {
            workspace.open();
        } else if (workspaceStatus.equals("closed")) {
            workspace.close();
        } else {
            workspace.invalid();
        }

        return workspace;
    }
}
