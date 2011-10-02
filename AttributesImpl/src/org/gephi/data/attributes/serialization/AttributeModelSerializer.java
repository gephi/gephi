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
