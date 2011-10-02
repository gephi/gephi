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
package org.gephi.io.importer.plugin.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.PropertiesAssociations;
import org.gephi.io.importer.api.PropertiesAssociations.EdgeProperties;
import org.gephi.io.importer.api.PropertiesAssociations.NodeProperties;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.DatabaseImporter;

/**
 *
 * @author Mathieu Bastian
 */
public class ImporterEdgeList implements DatabaseImporter {

    private Report report;
    private EdgeListDatabaseImpl database;
    private ContainerLoader container;
    private Connection connection;
    //TempData
    private String timeIntervalStart;
    private String timeIntervalStartOpen;
    private String timeIntervalEnd;
    private String timeIntervalEndOpen;

    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        try {
            importData();
        } catch (Exception e) {
            close();
            throw new RuntimeException(e);
        }
        close();
        return true;
    }

    private void close() {
        //Close connection
        if (connection != null) {
            try {
                connection.close();
                report.log("Database connection terminated");
            } catch (Exception e) { /* ignore close errors */ }
        }
    }

    private void importData() throws Exception {
        //Connect database
        String url = SQLUtils.getUrl(database.getSQLDriver(), database.getHost(), database.getPort(), database.getDBName());
        try {
            report.log("Try to connect at " + url);
            connection = database.getSQLDriver().getConnection(url, database.getUsername(), database.getPasswd());
            report.log("Database connection established");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.close();
                    report.log("Database connection terminated");
                } catch (Exception e) { /* ignore close errors */ }
            }
            report.logIssue(new Issue("Failed to connect at " + url, Issue.Level.CRITICAL, ex));
        }
        if (connection == null) {
            report.logIssue(new Issue("Failed to connect at " + url, Issue.Level.CRITICAL));
        }

        report.log(database.getPropertiesAssociations().getInfos());
        getNodes(connection);
        getEdges(connection);
        getNodesAttributes(connection);
        getEdgesAttributes(connection);
    }

    private void getNodes(Connection connection) throws SQLException {

        //Factory
        ContainerLoader.DraftFactory factory = container.factory();

        //Properties
        PropertiesAssociations properties = database.getPropertiesAssociations();

        Statement s = connection.createStatement();
        ResultSet rs = null;
        try {
            rs = s.executeQuery(database.getNodeQuery());
        } catch (SQLException ex) {
            report.logIssue(new Issue("Failed to execute Node query", Issue.Level.SEVERE, ex));
            return;
        }

        findNodeAttributesColumns(rs);
        AttributeTable nodeClass = container.getAttributeModel().getNodeTable();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        int count = 0;
        while (rs.next()) {
            NodeDraft node = factory.newNodeDraft();
            for (int i = 0; i < columnsCount; i++) {
                String columnName = metaData.getColumnLabel(i + 1);
                NodeProperties p = properties.getNodeProperty(columnName);
                if (p != null) {
                    injectNodeProperty(p, rs, i + 1, node);
                } else {
                    //Inject node attributes
                    AttributeColumn col = nodeClass.getColumn(columnName);
                    injectNodeAttribute(rs, i + 1, col, node);
                }
            }
            injectTimeIntervalProperty(node);
            container.addNode(node);
            ++count;
        }
        rs.close();
        s.close();

    }

    private void getEdges(Connection connection) throws SQLException {

        //Factory
        ContainerLoader.DraftFactory factory = container.factory();

        //Properties
        PropertiesAssociations properties = database.getPropertiesAssociations();

        Statement s = connection.createStatement();
        ResultSet rs = null;
        try {
            rs = s.executeQuery(database.getEdgeQuery());
        } catch (SQLException ex) {
            report.logIssue(new Issue("Failed to execute Edge query", Issue.Level.SEVERE, ex));
            return;
        }
        findEdgeAttributesColumns(rs);
        AttributeTable edgeClass = container.getAttributeModel().getEdgeTable();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        int count = 0;
        while (rs.next()) {
            EdgeDraft edge = factory.newEdgeDraft();
            for (int i = 0; i < columnsCount; i++) {
                String columnName = metaData.getColumnLabel(i + 1);
                EdgeProperties p = properties.getEdgeProperty(columnName);
                if (p != null) {
                    injectEdgeProperty(p, rs, i + 1, edge);
                } else {
                    //Inject edge attributes
                    AttributeColumn col = edgeClass.getColumn(columnName);
                    injectEdgeAttribute(rs, i + 1, col, edge);
                }
            }
            injectTimeIntervalProperty(edge);
            container.addEdge(edge);
            ++count;
        }
        rs.close();
        s.close();
    }

    private void getNodesAttributes(Connection connection) throws SQLException {
    }

    private void getEdgesAttributes(Connection connection) throws SQLException {
    }

    private void injectNodeProperty(NodeProperties p, ResultSet rs, int column, NodeDraft nodeDraft) throws SQLException {
        switch (p) {
            case ID:
                String id = rs.getString(column);
                if (id != null) {
                    nodeDraft.setId(id);
                }
                break;
            case LABEL:
                String label = rs.getString(column);
                if (label != null) {
                    nodeDraft.setLabel(label);
                }
                break;
            case X:
                float x = rs.getFloat(column);
                if (x != 0) {
                    nodeDraft.setX(x);
                }
                break;
            case Y:
                float y = rs.getFloat(column);
                if (y != 0) {
                    nodeDraft.setY(y);
                }
                break;
            case Z:
                float z = rs.getFloat(column);
                if (z != 0) {
                    nodeDraft.setZ(z);
                }
                break;
            case COLOR:
                String color = rs.getString(column);
                if (color != null) {
                    String[] rgb = color.split(",");
                    if (rgb.length == 3) {
                        nodeDraft.setColor(rgb[0], rgb[1], rgb[2]);
                    }
                }
                break;
            case SIZE:
                float size = rs.getFloat(column);
                if (size != 0) {
                    nodeDraft.setSize(size);
                }
                break;
            case START:
                container.setTimeFormat(getTimeFormat(rs, column));
                String start = getDateData(rs, column);
                if (start != null) {
                    timeIntervalStart = start;
                }
                break;
            case START_OPEN:
                container.setTimeFormat(getTimeFormat(rs, column));
                String startOpen = rs.getString(column);
                if (startOpen != null) {
                    timeIntervalStartOpen = startOpen;
                }
                break;
            case END:
                container.setTimeFormat(getTimeFormat(rs, column));
                String end = rs.getString(column);
                if (end != null) {
                    timeIntervalEnd = end;
                }
                break;
            case END_OPEN:
                container.setTimeFormat(getTimeFormat(rs, column));
                String endOpen = rs.getString(column);
                if (endOpen != null) {
                    timeIntervalEndOpen = endOpen;
                }
                break;

        }
    }
    
    private TimeFormat getTimeFormat(ResultSet rs, int column) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int type = metaData.getColumnType(column);
        if (type == Types.DATE) {
            return TimeFormat.DATE;
        } else if (type == Types.TIME) {
            return TimeFormat.DATETIME;
        } else if (type == Types.TIMESTAMP) {
            return TimeFormat.DATETIME;
        } else if (type == Types.VARCHAR) {
            return TimeFormat.DATETIME;
        } else if (type == Types.DOUBLE || type == Types.FLOAT) {
            return TimeFormat.DOUBLE;
        }
        return TimeFormat.DOUBLE;
    }

    private String getDateData(ResultSet rs, int column) throws SQLException {
        String res = null;
        ResultSetMetaData metaData = rs.getMetaData();
        int type = metaData.getColumnType(column);
        if (type == Types.DATE) {
            Date date = rs.getDate(column);
            res = date.toString();
        } else if (type == Types.TIME) {
            Time time = rs.getTime(column);
            res = time.toString();
        } else if (type == Types.TIMESTAMP) {
            Timestamp timeStamp = rs.getTimestamp(column);
            res = timeStamp.toString();
        } else if (type == Types.VARCHAR) {
            res = rs.getString(column);
        } else if (type == Types.DOUBLE || type == Types.FLOAT) {
            Double dbl = rs.getDouble(column);
            res = dbl.toString();
        }
        return res;
    }

    private void injectTimeIntervalProperty(NodeDraft nodeDraft) {
        if (timeIntervalStart != null && timeIntervalEnd != null) {
            nodeDraft.addTimeInterval(timeIntervalStart, timeIntervalEnd, false, false);
        } else if (timeIntervalStart != null && timeIntervalEndOpen != null) {
            nodeDraft.addTimeInterval(timeIntervalStart, timeIntervalEndOpen, false, true);
        } else if (timeIntervalStartOpen != null && timeIntervalEnd != null) {
            nodeDraft.addTimeInterval(timeIntervalStartOpen, timeIntervalEnd, true, false);
        } else if (timeIntervalStartOpen != null && timeIntervalEndOpen != null) {
            nodeDraft.addTimeInterval(timeIntervalStartOpen, timeIntervalEndOpen, true, true);
        } else if (timeIntervalStart != null) {
            nodeDraft.addTimeInterval(timeIntervalStart, null);
        } else if (timeIntervalStartOpen != null) {
            nodeDraft.addTimeInterval(timeIntervalStartOpen, null, true, false);
        } else if (timeIntervalEnd != null) {
            nodeDraft.addTimeInterval(null, timeIntervalEnd);
        } else if (timeIntervalEndOpen != null) {
            nodeDraft.addTimeInterval(null, timeIntervalEndOpen, false, true);
        }

        //Reset temp data
        timeIntervalStart = null;
        timeIntervalStartOpen = null;
        timeIntervalEnd = null;
        timeIntervalEndOpen = null;
    }

    private void injectEdgeProperty(EdgeProperties p, ResultSet rs, int column, EdgeDraft edgeDraft) throws SQLException {
        switch (p) {
            case ID:
                String id = rs.getString(column);
                if (id != null) {
                    edgeDraft.setId(id);
                }
                break;
            case LABEL:
                String label = rs.getString(column);
                if (label != null) {
                    edgeDraft.setLabel(label);
                }
                break;
            case SOURCE:
                String source = rs.getString(column);
                if (source != null) {
                    NodeDraft sourceNode = container.getNode(source);
                    edgeDraft.setSource(sourceNode);
                }
                break;
            case TARGET:
                String target = rs.getString(column);
                if (target != null) {
                    NodeDraft targetNode = container.getNode(target);
                    edgeDraft.setTarget(targetNode);
                }
                break;
            case WEIGHT:
                float weight = rs.getFloat(column);
                if (weight != 0) {
                    edgeDraft.setWeight(weight);
                }
                break;
            case COLOR:
                String color = rs.getString(column);
                if (color != null) {
                    String[] rgb = color.split(",");
                    if (rgb.length == 3) {
                        edgeDraft.setColor(rgb[0], rgb[1], rgb[2]);
                    }
                }
                break;
            case START:
                container.setTimeFormat(getTimeFormat(rs, column));
                String start = getDateData(rs, column);
                if (start != null) {
                    timeIntervalStart = start;
                }
                break;
            case START_OPEN:
                container.setTimeFormat(getTimeFormat(rs, column));
                String startOpen = rs.getString(column);
                if (startOpen != null) {
                    timeIntervalStartOpen = startOpen;
                }
                break;
            case END:
                container.setTimeFormat(getTimeFormat(rs, column));
                String end = rs.getString(column);
                if (end != null) {
                    timeIntervalEnd = end;
                }
                break;
            case END_OPEN:
                container.setTimeFormat(getTimeFormat(rs, column));
                String endOpen = rs.getString(column);
                if (endOpen != null) {
                    timeIntervalEndOpen = endOpen;
                }
                break;
        }
    }
    
    private void injectTimeIntervalProperty(EdgeDraft edgeDraft) {
        if (timeIntervalStart != null && timeIntervalEnd != null) {
            edgeDraft.addTimeInterval(timeIntervalStart, timeIntervalEnd, false, false);
        } else if (timeIntervalStart != null && timeIntervalEndOpen != null) {
            edgeDraft.addTimeInterval(timeIntervalStart, timeIntervalEndOpen, false, true);
        } else if (timeIntervalStartOpen != null && timeIntervalEnd != null) {
            edgeDraft.addTimeInterval(timeIntervalStartOpen, timeIntervalEnd, true, false);
        } else if (timeIntervalStartOpen != null && timeIntervalEndOpen != null) {
            edgeDraft.addTimeInterval(timeIntervalStartOpen, timeIntervalEndOpen, true, true);
        } else if (timeIntervalStart != null) {
            edgeDraft.addTimeInterval(timeIntervalStart, null);
        } else if (timeIntervalStartOpen != null) {
            edgeDraft.addTimeInterval(timeIntervalStartOpen, null, true, false);
        } else if (timeIntervalEnd != null) {
            edgeDraft.addTimeInterval(null, timeIntervalEnd);
        } else if (timeIntervalEndOpen != null) {
            edgeDraft.addTimeInterval(null, timeIntervalEndOpen, false, true);
        }

        //Reset temp data
        timeIntervalStart = null;
        timeIntervalStartOpen = null;
        timeIntervalEnd = null;
        timeIntervalEndOpen = null;
    }

    private void injectNodeAttribute(ResultSet rs, int columnIndex, AttributeColumn column, NodeDraft draft) {
        switch (column.getType()) {
            case BOOLEAN:
                try {
                    boolean val = rs.getBoolean(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a BOOLEAN value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case DOUBLE:
                try {
                    double val = rs.getDouble(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a DOUBLE value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case FLOAT:
                try {
                    float val = rs.getFloat(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a FLOAT value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case INT:
                try {
                    int val = rs.getInt(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a INT value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case LONG:
                try {
                    long val = rs.getLong(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a LONG value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            default: //String
                try {
                    String val = rs.getString(columnIndex);
                    if (val != null) {
                        draft.addAttributeValue(column, val);
                    } else {
                        report.logIssue(new Issue("Failed to get a STRING value for node attribute '" + column.getId() + "'", Issue.Level.WARNING));
                    }
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a STRING value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
        }
    }

    private void injectEdgeAttribute(ResultSet rs, int columnIndex, AttributeColumn column, EdgeDraft draft) {
        switch (column.getType()) {
            case BOOLEAN:
                try {
                    boolean val = rs.getBoolean(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a BOOLEAN value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case DOUBLE:
                try {
                    double val = rs.getDouble(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a DOUBLE value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case FLOAT:
                try {
                    float val = rs.getFloat(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a FLOAT value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case INT:
                try {
                    int val = rs.getInt(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a INT value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case LONG:
                try {
                    long val = rs.getLong(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a LONG value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            default: //String
                try {
                    String val = rs.getString(columnIndex);
                    if (val != null) {
                        draft.addAttributeValue(column, val);
                    } else {
                        report.logIssue(new Issue("Failed to get a BOOLEAN value for edge attribute '" + column.getId() + "'", Issue.Level.WARNING));
                    }
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a STRING value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
        }
    }

    private void findNodeAttributesColumns(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        AttributeTable nodeClass = container.getAttributeModel().getNodeTable();
        for (int i = 0; i < columnsCount; i++) {
            String columnName = metaData.getColumnLabel(i + 1);
            NodeProperties p = database.getPropertiesAssociations().getNodeProperty(columnName);
            if (p == null) {
                //No property associated to this column is found, so we append it as an attribute

                AttributeType type = AttributeType.STRING;
                switch (metaData.getColumnType(i + 1)) {
                    case Types.BIGINT:
                        type = AttributeType.LONG;
                        break;
                    case Types.INTEGER:
                        type = AttributeType.INT;
                        break;
                    case Types.TINYINT:
                        type = AttributeType.INT;
                        break;
                    case Types.SMALLINT:
                        type = AttributeType.INT;
                        break;
                    case Types.BOOLEAN:
                        type = AttributeType.BOOLEAN;
                        break;
                    case Types.FLOAT:
                        type = AttributeType.FLOAT;
                        break;
                    case Types.DOUBLE:
                        type = AttributeType.DOUBLE;
                        break;
                    case Types.VARCHAR:
                        type = AttributeType.STRING;
                        break;
                    default:
                        report.logIssue(new Issue("Unknown SQL Type " + metaData.getColumnType(i + 1) + ", STRING used.", Issue.Level.WARNING));
                        break;
                }
                report.log("Node attribute found: " + columnName + "(" + type + ")");
                nodeClass.addColumn(columnName, type);
            }
        }
    }

    private void findEdgeAttributesColumns(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        AttributeTable edgeClass = container.getAttributeModel().getEdgeTable();
        for (int i = 0; i < columnsCount; i++) {
            String columnName = metaData.getColumnLabel(i + 1);
            EdgeProperties p = database.getPropertiesAssociations().getEdgeProperty(columnName);
            if (p == null) {
                //No property associated to this column is found, so we append it as an attribute
                AttributeType type = AttributeType.STRING;
                switch (metaData.getColumnType(i + 1)) {
                    case Types.BIGINT:
                        type = AttributeType.LONG;
                        break;
                    case Types.INTEGER:
                        type = AttributeType.INT;
                        break;
                    case Types.TINYINT:
                        type = AttributeType.INT;
                        break;
                    case Types.SMALLINT:
                        type = AttributeType.INT;
                        break;
                    case Types.BOOLEAN:
                        type = AttributeType.BOOLEAN;
                        break;
                    case Types.FLOAT:
                        type = AttributeType.FLOAT;
                        break;
                    case Types.DOUBLE:
                        type = AttributeType.DOUBLE;
                        break;
                    case Types.VARCHAR:
                        type = AttributeType.STRING;
                        break;
                    default:
                        report.logIssue(new Issue("Unknown SQL Type " + metaData.getColumnType(i + 1) + ", STRING used.", Issue.Level.WARNING));
                        break;
                }

                report.log("Edge attribute found: " + columnName + "(" + type + ")");
                edgeClass.addColumn(columnName, type);
            }
        }
    }

    public void setDatabase(Database database) {
        this.database = (EdgeListDatabaseImpl) database;
    }

    public Database getDatabase() {
        return database;
    }

    public ContainerLoader getContainer() {
        return container;
    }

    public Report getReport() {
        return report;
    }
}
