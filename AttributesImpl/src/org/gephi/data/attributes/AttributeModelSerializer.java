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
package org.gephi.data.attributes;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeModelSerializer {

    private static final String ELEMENT_MODEL = "attributemodel";
    private static final String ELEMENT_TABLE = "table";
    private static final String ELEMENT_COLUMN = "column";
    private static final String ELEMENT_COLUMN_INDEX = "index";
    private static final String ELEMENT_COLUMN_ID = "id";
    private static final String ELEMENT_COLUMN_TITLE = "title";
    private static final String ELEMENT_COLUMN_TYPE = "type";
    private static final String ELEMENT_COLUMN_ORIGIN = "origin";
    private static final String ELEMENT_COLUMN_DEFAULT = "default";

    public Document createDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
            return document;
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public Element writeModel(Document document, AbstractAttributeModel model) {
        Element modelE = document.createElement(ELEMENT_MODEL);

        if (model != null) {
            for (AttributeTableImpl table : model.getTables()) {
                Element tableE = writeTable(document, table);
                tableE.setAttribute("nodetable", String.valueOf(table == model.getNodeTable()));
                tableE.setAttribute("edgetable", String.valueOf(table == model.getEdgeTable()));
                modelE.appendChild(tableE);
            }
        }

        return modelE;
    }

    public void readModel(Element modelE, AbstractAttributeModel model) {
        NodeList modelListE = modelE.getChildNodes();
        for (int i = 0; i < modelListE.getLength(); i++) {
            if (modelListE.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element itemE = (Element) modelListE.item(i);
                if (itemE.getTagName().equals(ELEMENT_TABLE)) {
                    AttributeTableImpl table;
                    if (Boolean.parseBoolean(itemE.getAttribute("nodetable"))) {
                        table = model.getNodeTable();
                    } else if (Boolean.parseBoolean(itemE.getAttribute("edgetable"))) {
                        table = model.getEdgeTable();
                    } else {
                        table = new AttributeTableImpl(model, "");
                    }
                    readTable(itemE, table);
                    if (table != model.getNodeTable() && table != model.getEdgeTable()) {
                        model.addTable(table);
                    }
                }
            }
        }
    }

    public Element writeTable(Document document, AttributeTableImpl table) {
        Element tableE = document.createElement(ELEMENT_TABLE);

        tableE.setAttribute("name", table.getName());
        tableE.setAttribute("version", String.valueOf(table.getVersion()));

        for (AttributeColumnImpl columnImpl : table.getColumns()) {
            Element columnE = writeColumn(document, columnImpl);
            tableE.appendChild(columnE);
        }
        return tableE;
    }

    public void readTable(Element tableE, AttributeTableImpl table) {
        table.setName(tableE.getAttribute("name"));

        NodeList tableListE = tableE.getChildNodes();
        for (int i = 0; i < tableListE.getLength(); i++) {
            if (tableListE.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element itemE = (Element) tableListE.item(i);
                if (itemE.getTagName().equals(ELEMENT_COLUMN)) {
                    readColumn(itemE, table);
                }
            }
        }

        table.setVersion(Integer.parseInt(tableE.getAttribute("version")));
    }

    public Element writeColumn(Document document, AttributeColumnImpl column) {
        Element columnE = document.createElement(ELEMENT_COLUMN);

        Element indexE = document.createElement(ELEMENT_COLUMN_INDEX);
        indexE.setTextContent(String.valueOf(column.getIndex()));
        columnE.appendChild(indexE);

        Element idE = document.createElement(ELEMENT_COLUMN_ID);
        idE.setTextContent(String.valueOf(column.getId()));
        columnE.appendChild(idE);

        Element titleE = document.createElement(ELEMENT_COLUMN_TITLE);
        titleE.setTextContent(String.valueOf(column.getTitle()));
        columnE.appendChild(titleE);

        Element typeE = document.createElement(ELEMENT_COLUMN_TYPE);
        typeE.setTextContent(column.getType().getTypeString());
        columnE.appendChild(typeE);

        Element origineE = document.createElement(ELEMENT_COLUMN_ORIGIN);
        origineE.setTextContent(column.getOrigin().name());
        columnE.appendChild(origineE);

        Element defaultE = document.createElement(ELEMENT_COLUMN_DEFAULT);
        if (column.getDefaultValue() != null) {
            defaultE.setTextContent(column.getDefaultValue().toString());
        }
        columnE.appendChild(defaultE);

        return columnE;
    }

    public void readColumn(Element columnE, AttributeTableImpl table) {

        int index = 0;
        String id = "";
        String title = "";
        AttributeType type = AttributeType.STRING;
        AttributeOrigin origin = AttributeOrigin.DATA;
        Object defaultValue = null;

        NodeList columnListE = columnE.getChildNodes();
        for (int i = 0; i < columnListE.getLength(); i++) {
            if (columnListE.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element itemE = (Element) columnListE.item(i);
                if (itemE.getTagName().equals(ELEMENT_COLUMN_INDEX)) {
                    index = Integer.parseInt(itemE.getTextContent());
                } else if (itemE.getTagName().equals(ELEMENT_COLUMN_ID)) {
                    id = itemE.getTextContent();
                } else if (itemE.getTagName().equals(ELEMENT_COLUMN_TITLE)) {
                    title = itemE.getTextContent();
                } else if (itemE.getTagName().equals(ELEMENT_COLUMN_TYPE)) {
                    type = AttributeType.valueOf(itemE.getTextContent());
                } else if (itemE.getTagName().equals(ELEMENT_COLUMN_ORIGIN)) {
                    origin = AttributeOrigin.valueOf(itemE.getTextContent());
                } else if (itemE.getTagName().equals(ELEMENT_COLUMN_DEFAULT)) {
                    if (!itemE.getTextContent().isEmpty()) {
                        defaultValue = type.parse(itemE.getTextContent());
                    }
                }
            }
        }
        table.addColumn(id, title, type, origin, defaultValue);
    }
}
