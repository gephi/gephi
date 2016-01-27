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
package org.gephi.graph;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.types.IntervalBooleanMap;
import org.gephi.graph.api.types.IntervalByteMap;
import org.gephi.graph.api.types.IntervalCharMap;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalFloatMap;
import org.gephi.graph.api.types.IntervalIntegerMap;
import org.gephi.graph.api.types.IntervalLongMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.IntervalShortMap;
import org.gephi.graph.api.types.IntervalStringMap;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 130)
public class LegacyAttributePersistenceProvider implements WorkspaceXMLPersistenceProvider {

    private static final String ELEMENT_MODEL = "attributemodel";
    private static final String ELEMENT_TABLE = "table";
    private static final String ELEMENT_COLUMN = "column";
    private static final String ELEMENT_COLUMN_INDEX = "index";
    private static final String ELEMENT_COLUMN_ID = "id";
    private static final String ELEMENT_COLUMN_TITLE = "title";
    private static final String ELEMENT_COLUMN_TYPE = "type";
    private static final String ELEMENT_COLUMN_DEFAULT = "default";

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        GraphModel model = LegacyMapHelper.getGraphModel(workspace);
        LegacyMapHelper helper = LegacyMapHelper.get(workspace);
        try {
            readModel(reader, model, helper);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return "attributemodel";
    }

    public void readModel(XMLStreamReader reader, GraphModel graphModel, LegacyMapHelper mapHelper) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_TABLE.equalsIgnoreCase(name)) {
                        Table table = null;
                        if (Boolean.parseBoolean(reader.getAttributeValue(null, "nodetable"))) {
                            table = graphModel.getNodeTable();
                        } else if (Boolean.parseBoolean(reader.getAttributeValue(null, "edgetable"))) {
                            table = graphModel.getEdgeTable();
                        }

                        if (table != null) {
                            readTable(reader, table, mapHelper);
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

    public void readTable(XMLStreamReader reader, Table table, LegacyMapHelper mapHelper) throws XMLStreamException {

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_COLUMN.equalsIgnoreCase(name)) {
                        readColumn(reader, table, mapHelper);
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_TABLE.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public void readColumn(XMLStreamReader reader, Table table, LegacyMapHelper mapHelper) throws XMLStreamException {

        int index = 0;
        String id = "";
        String title = "";
        Class type = null;
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
                            String typeText = reader.getText();
                            if (typeText.equalsIgnoreCase("byte")) {
                                type = Byte.class;
                            } else if (typeText.equalsIgnoreCase("short")) {
                                type = Short.class;
                            } else if (typeText.equalsIgnoreCase("int")) {
                                type = Integer.class;
                            } else if (typeText.equalsIgnoreCase("long")) {
                                type = Long.class;
                            } else if (typeText.equalsIgnoreCase("float")) {
                                type = Float.class;
                            } else if (typeText.equalsIgnoreCase("double")) {
                                type = Double.class;
                            } else if (typeText.equalsIgnoreCase("boolean")) {
                                type = Boolean.class;
                            } else if (typeText.equalsIgnoreCase("char")) {
                                type = Character.class;
                            } else if (typeText.equalsIgnoreCase("string")) {
                                type = String.class;
                            } else if (typeText.equalsIgnoreCase("biginteger")) {
                                type = BigInteger.class;
                            } else if (typeText.equalsIgnoreCase("bigdecimal")) {
                                type = BigDecimal.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_byte")) {
                                type = IntervalByteMap.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_short")) {
                                type = IntervalShortMap.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_int")) {
                                type = IntervalIntegerMap.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_long")) {
                                type = IntervalLongMap.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_float")) {
                                type = IntervalFloatMap.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_double")) {
                                type = IntervalDoubleMap.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_boolean")) {
                                type = IntervalBooleanMap.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_char")) {
                                type = IntervalCharMap.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_string")) {
                                type = IntervalStringMap.class;
                            } else if (typeText.equalsIgnoreCase("dynamic_biginteger")) {
                                //Not supported yet
                            } else if (typeText.equalsIgnoreCase("dynamic_bigdecimal")) {
                                //Not supported yet
                            } else if (typeText.equalsIgnoreCase("list_byte")) {
                                type = byte[].class;
                            } else if (typeText.equalsIgnoreCase("list_short")) {
                                type = short[].class;
                            } else if (typeText.equalsIgnoreCase("list_integer")) {
                                type = int[].class;
                            } else if (typeText.equalsIgnoreCase("list_long")) {
                                type = long[].class;
                            } else if (typeText.equalsIgnoreCase("list_float")) {
                                type = float[].class;
                            } else if (typeText.equalsIgnoreCase("list_double")) {
                                type = double[].class;
                            } else if (typeText.equalsIgnoreCase("list_boolean")) {
                                type = boolean[].class;
                            } else if (typeText.equalsIgnoreCase("list_character")) {
                                type = char[].class;
                            } else if (typeText.equalsIgnoreCase("list_string")) {
                                type = String[].class;
                            } else if (typeText.equalsIgnoreCase("list_biginteger")) {
                                type = BigInteger[].class;
                            } else if (typeText.equalsIgnoreCase("list_bigdecimal")) {
                                type = BigDecimal[].class;
                            } else if (typeText.equalsIgnoreCase("time_interval")) {
                                type = IntervalSet.class;
                            }

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

        if (type != null && type.equals(IntervalSet.class)) {
            id = "timeset";
        }

        if (type != null) {
            if (!table.hasColumn(id)) {
                Object defaultVal = null;
                try {
                    defaultVal = !defaultValue.isEmpty() ? AttributeUtils.parse(defaultValue, type) : null;
                } catch (Exception e) {
                    //Ignore
                }
                table.addColumn(id, title, type, Origin.DATA, defaultVal, true);
            }
            if (table.getElementClass().equals(Node.class)) {
                mapHelper.nodeIndexToIds.put(index, id);
            } else {
                mapHelper.edgeIndexToIds.put(index, id);
            }
        }
    }
}
