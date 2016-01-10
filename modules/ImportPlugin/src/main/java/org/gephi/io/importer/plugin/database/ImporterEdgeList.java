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

import org.gephi.graph.api.TimeFormat;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.PropertiesAssociations;
import org.gephi.io.importer.api.PropertiesAssociations.EdgeProperties;
import org.gephi.io.importer.api.PropertiesAssociations.NodeProperties;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.DatabaseImporter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * @author Mathieu Bastian
 */
public class ImporterEdgeList implements DatabaseImporter {

    private Report report;
    private EdgeListDatabaseImpl database;
    private ContainerLoader container;
    private Connection connection;
    //TempData
    private String timeIntervalStart;
    private String timeIntervalEnd;

    private NodeColumns nodeColumns = new NodeColumns();
    private EdgeColumns edgeColumns = new EdgeColumns();

    @Override
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
        ElementDraft.Factory factory = container.factory();

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
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();

        int idColumn = nodeColumns.findIdIndex(metaData, properties);

        while (rs.next()) {
            final NodeDraft node = nodeColumns.getNodeDraft(factory, rs, idColumn);

            for (int i = 0; i < columnsCount; i++) {
                String columnName = metaData.getColumnLabel(i + 1);
                NodeProperties p = properties.getNodeProperty(columnName);
                if (p != null) {
                    injectNodeProperty(p, rs, i + 1, node);
                } else {
                    //Inject node attributes
                    ColumnDraft col = container.getNodeColumn(columnName);
                    injectElementAttribute(rs, i + 1, col, node);
                }
            }
            injectTimeIntervalProperty(node);
            container.addNode(node);
        }
        rs.close();
        s.close();
    }

    private void getEdges(Connection connection) throws SQLException {

        //Factory
        ElementDraft.Factory factory = container.factory();

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
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();

        int idColumn = edgeColumns.findIdIndex(metaData, properties);

        while (rs.next()) {
            EdgeDraft edge = edgeColumns.getEdgeDraft(factory, rs, idColumn);

            for (int i = 0; i < columnsCount; i++) {
                String columnName = metaData.getColumnLabel(i + 1);
                EdgeProperties p = properties.getEdgeProperty(columnName);
                if (p != null) {
                    injectEdgeProperty(p, rs, i + 1, edge);
                } else {
                    //Inject edge attributes
                    ColumnDraft col = container.getEdgeColumn(columnName);
                    injectElementAttribute(rs, i + 1, col, edge);
                }
            }
            injectTimeIntervalProperty(edge);
            container.addEdge(edge);
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
                    String[] rgb = color.replace(" ", "").split(",");
                    if (rgb.length == 3) {
                        nodeDraft.setColor(rgb[0], rgb[1], rgb[2]);
                    } else {
                        nodeDraft.setColor(color);
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
                    timeIntervalStart = startOpen;
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
                    timeIntervalEnd = endOpen;
                }
                break;
        }
    }

    private TimeFormat getTimeFormat(ResultSet rs, int column) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int type = metaData.getColumnType(column);
        switch (type) {
            case Types.DATE:
                return TimeFormat.DATE;
            case Types.TIME:
                return TimeFormat.DATETIME;
            case Types.TIMESTAMP:
                return TimeFormat.DATETIME;
            case Types.VARCHAR:
                return TimeFormat.DATETIME;
            case Types.DOUBLE:
            case Types.FLOAT:
                return TimeFormat.DOUBLE;
            default:
                break;
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
        if (timeIntervalStart != null || timeIntervalEnd != null) {
            nodeDraft.addInterval(timeIntervalStart, timeIntervalEnd);
        }

        //Reset temp data
        timeIntervalStart = null;
        timeIntervalEnd = null;
    }

    private void injectEdgeProperty(EdgeProperties p, ResultSet rs, int column, EdgeDraft edgeDraft) throws SQLException {
        switch (p) {
            case LABEL:
                String label = rs.getString(column);
                if (label != null) {
                    edgeDraft.setLabel(label);
                }
                break;
            case SOURCE:
                String source = rs.getString(column);
                if (source != null && !source.isEmpty()) {
                    NodeDraft sourceNode = container.getNode(source);
                    edgeDraft.setSource(sourceNode);
                }
                break;
            case TARGET:
                String target = rs.getString(column);
                if (target != null && !target.isEmpty()) {
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
                    } else {
                        edgeDraft.setColor(color);
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
                    timeIntervalStart = startOpen;
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
                    timeIntervalEnd = endOpen;
                }
                break;
        }
    }

    private void injectTimeIntervalProperty(EdgeDraft edgeDraft) {
        if (timeIntervalStart != null || timeIntervalEnd != null) {
            edgeDraft.addInterval(timeIntervalStart, timeIntervalEnd);
        }

        //Reset temp data
        timeIntervalStart = null;
        timeIntervalEnd = null;
    }

    private void injectElementAttribute(ResultSet rs, int columnIndex, ColumnDraft column, ElementDraft draft) {
        String elementName;
        if (draft instanceof NodeDraft) {
            elementName = "node";
        } else {
            elementName = "edge";
        }
        Class typeClass = column.getTypeClass();
        if (typeClass.equals(Boolean.class)) {
            try {
                boolean val = rs.getBoolean(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a BOOLEAN value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Double.class)) {
            try {
                double val = rs.getDouble(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a DOUBLE value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Float.class)) {
            try {
                float val = rs.getFloat(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a FLOAT value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Integer.class)) {
            try {
                int val = rs.getInt(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a INT value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Long.class)) {
            try {
                long val = rs.getLong(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a LONG value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Short.class)) {
            try {
                short val = rs.getShort(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a SHORT value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Byte.class)) {
            try {
                byte val = rs.getByte(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a BYTE value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else {
            try {
                String val = rs.getString(columnIndex);
                if (val != null) {
                    draft.setValue(column.getId(), val);
                } else {
                    report.logIssue(new Issue("Failed to get a STRING value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.WARNING));
                }
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a STRING value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        }
    }

    private void findNodeAttributesColumns(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        for (int i = 0; i < columnsCount; i++) {
            String columnName = metaData.getColumnLabel(i + 1);
            NodeProperties p = database.getPropertiesAssociations().getNodeProperty(columnName);
            if (p == null) {
                //No property associated to this column is found, so we append it as an attribute
                Class typeClass = findTypeClass(metaData, i);
                container.addNodeColumn(columnName, typeClass);
            }
        }
    }

    private void findEdgeAttributesColumns(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        for (int i = 0; i < columnsCount; i++) {
            String columnName = metaData.getColumnLabel(i + 1);
            EdgeProperties p = database.getPropertiesAssociations().getEdgeProperty(columnName);
            if (p == null) {
                //No property associated to this column is found, so we append it as an attribute
                Class typeClass = findTypeClass(metaData, i);
                container.addEdgeColumn(columnName, typeClass);
            }
        }
    }

    private Class findTypeClass(ResultSetMetaData metaData, int columnIndex) throws SQLException {
        Class type = String.class;
        switch (metaData.getColumnType(columnIndex + 1)) {
            case Types.BIGINT:
                type = Long.class;
                break;
            case Types.INTEGER:
                type = Integer.class;
                break;
            case Types.TINYINT:
                type = Byte.class;
                break;
            case Types.SMALLINT:
                type = Short.class;
                break;
            case Types.BOOLEAN:
                type = Boolean.class;
                break;
            case Types.FLOAT:
                type = Float.class;
                break;
            case Types.DOUBLE:
                type = Double.class;
                break;
            case Types.VARCHAR:
                type = String.class;
                break;
            case Types.BIT:
                type = Boolean.class;
                break;
            case Types.REAL:
                type = Float.class;
                break;
            default:
                report.logIssue(new Issue("Unknown SQL Type " + metaData.getColumnType(columnIndex + 1) + ", STRING used.", Issue.Level.WARNING));
                break;
        }
        return type;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = (EdgeListDatabaseImpl) database;
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }
}
