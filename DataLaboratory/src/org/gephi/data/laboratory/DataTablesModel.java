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
package org.gephi.data.laboratory;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class DataTablesModel {

    private String[] nodeColumnsHidden;
    private String[] edgeColumnsHidden;

    public DataTablesModel() {
        nodeColumnsHidden = new String[0];
        edgeColumnsHidden = new String[0];
    }

    public String[] getEdgeColumnsHidden() {
        return edgeColumnsHidden;
    }

    public void setEdgeColumnsHidden(String[] edgeColumnsHidden) {
        this.edgeColumnsHidden = edgeColumnsHidden;
    }

    public String[] getNodeColumnsHidden() {
        return nodeColumnsHidden;
    }

    public void setNodeColumnsHidden(String[] nodeColumnsHidden) {
        this.nodeColumnsHidden = nodeColumnsHidden;
    }

    public void readXML(Element modelElement) {
        List<String> nodeList = new ArrayList<String>();

        Element nodeHiddenE = (Element) modelElement.getElementsByTagName("nodehidden").item(0);
        NodeList nodeChildren = nodeHiddenE.getChildNodes();
        for (int i = 0; i < nodeChildren.getLength(); i++) {
            Element colE = (Element) nodeChildren.item(i);
            nodeList.add(colE.getTextContent());
        }
        this.nodeColumnsHidden = nodeList.toArray(new String[0]);

        List<String> edgeList = new ArrayList<String>();
        Element edgeHiddenE = (Element) modelElement.getElementsByTagName("edgehidden").item(0);
        NodeList edgeChildren = edgeHiddenE.getChildNodes();
        for (int i = 0; i < edgeChildren.getLength(); i++) {
            Element colE = (Element) edgeChildren.item(i);
            edgeList.add(colE.getTextContent());
        }
        this.edgeColumnsHidden = edgeList.toArray(new String[0]);
    }

    public Element writeXML(Document document) {
        Element modelE = document.createElement("datatablesmodel");

        Element nodeHiddenE = document.createElement("nodehidden");
        for (int i = 0; i < nodeColumnsHidden.length; i++) {
            Element colE = document.createElement("column");
            colE.setTextContent(nodeColumnsHidden[i]);
            nodeHiddenE.appendChild(colE);
        }
        modelE.appendChild(nodeHiddenE);

        Element edgeHiddenE = document.createElement("edgehidden");
        for (int i = 0; i < edgeColumnsHidden.length; i++) {
            Element colE = document.createElement("column");
            colE.setTextContent(edgeColumnsHidden[i]);
            edgeHiddenE.appendChild(colE);
        }
        modelE.appendChild(edgeHiddenE);

        return modelE;
    }
}
