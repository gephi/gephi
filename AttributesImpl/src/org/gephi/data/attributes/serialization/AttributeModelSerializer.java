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
package org.gephi.data.attributes.serialization;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.data.attributes.AbstractAttributeModel;
import org.gephi.data.attributes.AttributeColumnImpl;
import org.gephi.data.attributes.AttributeTableImpl;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;

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

    public void writeModel(XMLStreamWriter writer, AbstractAttributeModel model) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_MODEL);

        if (model != null) {
            for (AttributeTableImpl table : model.getTables()) {
                writeTable(writer, table, model);
            }
        }

        writer.writeEndElement();
    }

    public void readModel(XMLStreamReader reader, AbstractAttributeModel model) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_TABLE.equalsIgnoreCase(name)) {
                        AttributeTableImpl table;
                        if (Boolean.parseBoolean(reader.getAttributeValue(null, "nodetable"))) {
                            table = model.getNodeTable();
                        } else if (Boolean.parseBoolean(reader.getAttributeValue(null, "edgetable"))) {
                            table = model.getEdgeTable();
                        } else {
                            table = new AttributeTableImpl(model, "");
                        }
                        readTable(reader, table);
                        if (table != model.getNodeTable() && table != model.getEdgeTable()) {
                            model.addTable(table);
                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_MODEL.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public void writeTable(XMLStreamWriter writer, AttributeTableImpl table, AbstractAttributeModel model) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_TABLE);

        writer.writeAttribute("name", table.getName());
        writer.writeAttribute("version", String.valueOf(table.getVersion()));
        writer.writeAttribute("nodetable", String.valueOf(table == model.getNodeTable()));
        writer.writeAttribute("edgetable", String.valueOf(table == model.getEdgeTable()));

        for (AttributeColumnImpl columnImpl : table.getColumns()) {
            writeColumn(writer, columnImpl);
        }
        writer.writeEndElement();
    }

    public void readTable(XMLStreamReader reader, AttributeTableImpl table) throws XMLStreamException {
        table.setName(reader.getAttributeValue(null, "name"));
        int version = Integer.parseInt(reader.getAttributeValue(null, "version"));

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_COLUMN.equalsIgnoreCase(name)) {
                        readColumn(reader, table);
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_TABLE.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }

        table.setVersion(version);
    }

    public void writeColumn(XMLStreamWriter writer, AttributeColumnImpl column) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_COLUMN);

        writer.writeStartElement(ELEMENT_COLUMN_INDEX);
        writer.writeCharacters(String.valueOf(column.getIndex()));
        writer.writeEndElement();

        writer.writeStartElement(ELEMENT_COLUMN_ID);
        writer.writeCharacters(String.valueOf(column.getId()));
        writer.writeEndElement();

        writer.writeStartElement(ELEMENT_COLUMN_TITLE);
        writer.writeCharacters(String.valueOf(column.getTitle()));
        writer.writeEndElement();

        writer.writeStartElement(ELEMENT_COLUMN_TYPE);
        writer.writeCharacters(column.getType().getTypeString());
        writer.writeEndElement();

        writer.writeStartElement(ELEMENT_COLUMN_ORIGIN);
        writer.writeCharacters(column.getOrigin().name());
        writer.writeEndElement();

        writer.writeStartElement(ELEMENT_COLUMN_DEFAULT);
        if (column.getDefaultValue() != null) {
            writer.writeCharacters(column.getDefaultValue().toString());
        }
        writer.writeEndElement();

        writer.writeEndElement();
    }

    public void readColumn(XMLStreamReader reader, AttributeTableImpl table) throws XMLStreamException {

        int index = 0;
        String id = "";
        String title = "";
        AttributeType type = AttributeType.STRING;
        AttributeOrigin origin = AttributeOrigin.DATA;
        String defaultValue = "";

        boolean end = false;
        String name = null;
        while (reader.hasNext() && !end) {
            int t = reader.next();

            switch (t) {
                case XMLStreamReader.START_ELEMENT:
                    name = reader.getLocalName();
                    break;
                case XMLStreamReader.CHARACTERS:
                    if (!reader.isWhiteSpace()) {
                        if (ELEMENT_COLUMN_INDEX.equalsIgnoreCase(name)) {
                            index = Integer.parseInt(reader.getText());
                        } else if (ELEMENT_COLUMN_ID.equalsIgnoreCase(name)) {
                            id += reader.getText();
                        } else if (ELEMENT_COLUMN_TITLE.equalsIgnoreCase(name)) {
                            title += reader.getText();
                        } else if (ELEMENT_COLUMN_TYPE.equalsIgnoreCase(name)) {
                            type = AttributeType.valueOf(reader.getText());
                        } else if (ELEMENT_COLUMN_ORIGIN.equalsIgnoreCase(name)) {
                            origin = AttributeOrigin.valueOf(reader.getText());
                        } else if (ELEMENT_COLUMN_DEFAULT.equalsIgnoreCase(name)) {
                            if (!reader.getText().isEmpty()) {
                                defaultValue += reader.getText();
                            }
                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_COLUMN.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
        Object defaultVal = !defaultValue.isEmpty() ? type.parse(defaultValue) : null;
        if (!table.hasColumn(title)) {
            table.addColumn(id, title, type, origin, defaultVal);
        } else {
            table.replaceColumn(table.getColumn(title), id, title, type, origin, defaultVal);
        }
    }
}
