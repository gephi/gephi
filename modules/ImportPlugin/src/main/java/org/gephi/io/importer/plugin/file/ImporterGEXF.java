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
package org.gephi.io.importer.plugin.file;

import java.awt.Color;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.io.importer.api.*;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.joda.time.DateTimeZone;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ImporterGEXF implements FileImporter, LongTask {

    //GEXF
    private static final String GEXF = "gexf";
    private static final String GEXF_VERSION = "version";
    private static final String GRAPH = "graph";
    private static final String GRAPH_DEFAULT_EDGETYPE = "defaultedgetype";
    private static final String GRAPH_TIMEFORMAT = "timeformat";
    private static final String GRAPH_TIMEREPRESENTATION = "timerepresentation";
    private static final String GRAPH_TIMEFORMAT2 = "timetype"; // GEXF 1.1
    private static final String GRAPH_TIMEZONE = "timezone";
    private static final String GRAPH_IDTYPE = "idtype";
    private static final String START = "start";
    private static final String END = "end";
    private static final String START_OPEN = "startopen"; // GEXF 1.2
    private static final String END_OPEN = "endopen"; // GEXF 1.2
    private static final String TIMESTAMP = "timestamp";
    private static final String TIMESTAMPS = "timestamps";
    private static final String INTERVALS = "intervals";
    private static final String NODE = "node";
    private static final String NODE_ID = "id";
    private static final String NODE_LABEL = "label";
    private static final String NODE_PID = "pid"; // GEXF 1.2
    private static final String NODE_POSITION = "position";
    private static final String NODE_COLOR = "color";
    private static final String NODE_SIZE = "size";
    private static final String NODE_SPELL = "slice"; // GEXF 1.1
    private static final String NODE_SPELL2 = "spell";
    private static final String EDGE = "edge";
    private static final String EDGE_ID = "id";
    private static final String EDGE_SOURCE = "source";
    private static final String EDGE_TARGET = "target";
    private static final String EDGE_LABEL = "label";
    private static final String EDGE_TYPE = "type";
    private static final String EDGE_WEIGHT = "weight";
    private static final String EDGE_COLOR = "color";
    private static final String EGDE_KIND = "kind";
    private static final String EDGE_SPELL = "slice"; // GEXF 1.1
    private static final String EDGE_SPELL2 = "spell";
    private static final String ATTRIBUTE = "attribute";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_TITLE = "title";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_DEFAULT = "default";
    private static final String ATTRIBUTES = "attributes";
    private static final String ATTRIBUTES_CLASS = "class"; // GEXF 1.0
    private static final String ATTRIBUTES_TYPE = "type";
    private static final String ATTRIBUTES_TYPE2 = "mode";
    private static final String ATTVALUE = "attvalue";
    private static final String ATTVALUE_FOR = "for";
    private static final String ATTVALUE_FOR2 = "id"; // GEXF 1.0
    private static final String ATTVALUE_VALUE = "value";
    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private boolean cancel;
    private Report report;
    private ProgressTicket progress;
    private XMLStreamReader xmlReader;

    @Override
    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        Progress.start(progress);
        try {

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
                inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
            }
            inputFactory.setXMLReporter(new XMLReporter() {
                @Override
                public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
                }
            });
            xmlReader = inputFactory.createXMLStreamReader(reader);

            while (xmlReader.hasNext()) {

                Integer eventType = xmlReader.next();
                if (eventType.equals(XMLEvent.START_ELEMENT)) {
                    String name = xmlReader.getLocalName();
                    if (GEXF.equalsIgnoreCase(name)) {
                        readGexf(xmlReader);
                    } else if (GRAPH.equalsIgnoreCase(name)) {
                        readGraph(xmlReader);
                    } else if (NODE.equalsIgnoreCase(name)) {
                        readNode(xmlReader);
                    } else if (EDGE.equalsIgnoreCase(name)) {
                        readEdge(xmlReader);
                    } else if (ATTRIBUTES.equalsIgnoreCase(name)) {
                        readAttributes(xmlReader);
                    }
                } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                    String name = xmlReader.getLocalName();
                    if (NODE.equalsIgnoreCase(name)) {
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        } finally {
            try {
                xmlReader.close();
            } catch (XMLStreamException e) {
            }
        }
        Progress.finish(progress);
        return !cancel;
    }

    private void readGexf(XMLStreamReader reader) throws Exception {
        String version = "";

        //Attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (GEXF_VERSION.equalsIgnoreCase(attName)) {
                version = reader.getAttributeValue(i);
            }
        }

        if (!version.isEmpty() && version.equals("1.0")) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_version10"), Issue.Level.INFO));
        } else if (!version.isEmpty() && version.equals("1.1")) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_version11"), Issue.Level.INFO));
        } else if (!version.isEmpty() && version.equals("1.2")) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_version12"), Issue.Level.INFO));
        } else if (!version.isEmpty() && version.equals("1.3")) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_version13"), Issue.Level.INFO));
        } else {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_version_undef"), Issue.Level.WARNING));
        }
    }

    private void readGraph(XMLStreamReader reader) throws Exception {
        String mode = "";
        String defaultEdgeType = "";
        String timeFormat = "";
        String timeRepresentation = "";
        String timeZone = "";
        String timestamp = "";
        String start = "";
        String end = "";
        String idType = "";

        //Attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (GRAPH_DEFAULT_EDGETYPE.equalsIgnoreCase(attName)) {
                defaultEdgeType = reader.getAttributeValue(i);
            } else if (ATTRIBUTES_TYPE2.equalsIgnoreCase(attName)) {
                mode = reader.getAttributeValue(i);
            } else if (GRAPH_TIMEFORMAT.equalsIgnoreCase(attName) || GRAPH_TIMEFORMAT2.equalsIgnoreCase(attName)) {
                timeFormat = reader.getAttributeValue(i);
            } else if (GRAPH_TIMEREPRESENTATION.equalsIgnoreCase(attName)) {
                timeRepresentation = reader.getAttributeValue(i);
            } else if (GRAPH_TIMEZONE.equalsIgnoreCase(attName)) {
                timeZone = reader.getAttributeValue(i);
            } else if (TIMESTAMP.equalsIgnoreCase(attName)) {
                timestamp = reader.getAttributeValue(i);
            } else if (START.equalsIgnoreCase(attName)) {
                start = reader.getAttributeValue(i);
            } else if (END.equalsIgnoreCase(attName)) {
                end = reader.getAttributeValue(i);
            } else if (GRAPH_IDTYPE.equals(attName)) {
                idType = reader.getAttributeValue(i);
            }
        }

        //Edge Type
        if (!defaultEdgeType.isEmpty()) {
            if (defaultEdgeType.equalsIgnoreCase("undirected")) {
                container.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
            } else if (defaultEdgeType.equalsIgnoreCase("directed")) {
                container.setEdgeDefault(EdgeDirectionDefault.DIRECTED);
            } else if (defaultEdgeType.equalsIgnoreCase("mutual")) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgedouble"), Issue.Level.WARNING));
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_defaultedgetype", defaultEdgeType), Issue.Level.SEVERE));
            }
        }

        //TimeFormat
        if (!timeFormat.isEmpty()) {
            if ("double".equalsIgnoreCase(timeFormat) || "float".equalsIgnoreCase(timeFormat)) {
                container.setTimeFormat(TimeFormat.DOUBLE);
            } else if ("date".equalsIgnoreCase(timeFormat)) {
                container.setTimeFormat(TimeFormat.DATE);
            } else if ("datetime".equalsIgnoreCase(timeFormat)) {
                container.setTimeFormat(TimeFormat.DATETIME);
            } else if ("timestamp".equalsIgnoreCase(timeFormat)) {
                container.setTimeFormat(TimeFormat.DATETIME);
            }
        } else if (mode.equalsIgnoreCase("dynamic")) {
            container.setTimeFormat(TimeFormat.DOUBLE);
        }

        //TimeRepresentation
        if (!timeRepresentation.isEmpty()) {
            if ("timestamp".equalsIgnoreCase(timeRepresentation)) {
                container.setTimeRepresentation(TimeRepresentation.TIMESTAMP);
            } else if ("interval".equalsIgnoreCase(timeRepresentation)) {
                container.setTimeRepresentation(TimeRepresentation.INTERVAL);
            }
        }

        //Timezone
        if (!timeZone.isEmpty()) {
            try {
                container.setTimeZone(DateTimeZone.forID(timeZone));
            } catch (IllegalArgumentException e) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_timezone_parseerror"), Issue.Level.SEVERE));
            }
        }

        // Slice
        if (!mode.isEmpty() && mode.equalsIgnoreCase("slice")) {
            if (timestamp.isEmpty() && (start.isEmpty() && end.isEmpty())) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_slice_bound_missing"), Issue.Level.SEVERE));
            }

            if (!timestamp.isEmpty() && checkTimerepresentationIsTimestamp()) {
                container.setTimestamp(timestamp);
            }

            // Interval
            if ((!start.isEmpty() || !end.isEmpty()) && checkTimerepresentationIsInterval()) {
                container.setInterval(start, end);
            }
        }

        //Id type
        if (!idType.isEmpty()) {
            if (idType.equalsIgnoreCase("integer")) {
                container.setElementIdType(ElementIdType.INTEGER);
            } else if (idType.equalsIgnoreCase("long")) {
                container.setElementIdType(ElementIdType.LONG);
            } else if (idType.equalsIgnoreCase("string")) {
                container.setElementIdType(ElementIdType.STRING);
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_idtype_error", idType), Issue.Level.SEVERE));
            }
        }
    }

    private void readNode(XMLStreamReader reader) throws Exception {
        String id = "";
        String label = "";
        String startDate = "";
        String endDate = "";
        String pid = "";
        String timestamp = "";
        String timestamps = "";
        String intervals = "";
        boolean startOpen = false;
        boolean endOpen = false;

        //Attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (NODE_ID.equalsIgnoreCase(attName)) {
                id = reader.getAttributeValue(i);
            } else if (NODE_LABEL.equalsIgnoreCase(attName)) {
                label = reader.getAttributeValue(i);
            } else if (START.equalsIgnoreCase(attName)) {
                startDate = reader.getAttributeValue(i);
            } else if (START_OPEN.equalsIgnoreCase(attName)) {
                startDate = reader.getAttributeValue(i);
                startOpen = true;
            } else if (END.equalsIgnoreCase(attName)) {
                endDate = reader.getAttributeValue(i);
            } else if (END_OPEN.equalsIgnoreCase(attName)) {
                endDate = reader.getAttributeValue(i);
                endOpen = true;
            } else if (NODE_PID.equalsIgnoreCase(attName)) {
                pid = reader.getAttributeValue(i);
            } else if (TIMESTAMP.equalsIgnoreCase(attName)) {
                timestamp = reader.getAttributeValue(i);
            } else if (TIMESTAMPS.equalsIgnoreCase(attName)) {
                timestamps = reader.getAttributeValue(i);
            } else if (INTERVALS.equalsIgnoreCase(attName)) {
                intervals = reader.getAttributeValue(i);
            }
        }

        if (id.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeid"), Issue.Level.SEVERE));
            return;
        }

        NodeDraft node = null;
        if (container.nodeExists(id)) {
            node = container.getNode(id);
        } else {
            node = container.factory().newNodeDraft(id);
        }
        node.setLabel(label);

        //Parent
        if (!pid.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_pid", id), Issue.Level.SEVERE));
        }

        if (!container.nodeExists(id)) {
            container.addNode(node);
        }

        boolean end = false;
        boolean spells = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = xmlReader.getLocalName();
                    if (ATTVALUE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        readElementAttValue(reader, node);
                    } else if (NODE_POSITION.equalsIgnoreCase(name)) {
                        readNodePosition(reader, node);
                    } else if (NODE_COLOR.equalsIgnoreCase(name)) {
                        readElementColor(reader, node);
                    } else if (NODE_SIZE.equalsIgnoreCase(name)) {
                        readNodeSize(reader, node);
                    } else if (NODE_SPELL.equalsIgnoreCase(name) || NODE_SPELL2.equalsIgnoreCase(name)) {
                        readElementSpell(reader, node);
                        spells = true;
                    } else if (NODE.equalsIgnoreCase(name)) {
                        readNode(reader);
                    }
                    break;

                case XMLStreamReader.END_ELEMENT:
                    if (NODE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }

        //Dynamic
        if (!spells) {
            if ((!startDate.isEmpty() || !endDate.isEmpty()) && checkTimerepresentationIsInterval()) {
                if (startOpen || endOpen) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_node_open_interval", id), Issue.Level.WARNING));
                }
                try {
                    node.addInterval(startDate, endDate);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_node_timeinterval_parseerror", id), Issue.Level.SEVERE));
                }
            } else if (!intervals.isEmpty() && checkTimerepresentationIsInterval()) {
                try {
                    node.addIntervals(intervals);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_node_timeintervals_parseerror", id), Issue.Level.SEVERE));
                }
            } else if (!timestamp.isEmpty() && checkTimerepresentationIsTimestamp()) {
                try {
                    node.addTimestamp(timestamp);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_node_timestamp_parseerror", id), Issue.Level.SEVERE));
                }
            } else if (!timestamps.isEmpty() && checkTimerepresentationIsTimestamp()) {
                try {
                    node.addTimestamps(timestamps);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_node_timestamps_parseerror", id), Issue.Level.SEVERE));
                }
            }
        }
    }

    private void readElementAttValue(XMLStreamReader reader, ElementDraft element) {
        String fore = "";
        String value = "";
        String startDate = "";
        String endDate = "";
        String timestamp = "";
        boolean startOpen = false;
        boolean endOpen = false;

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (ATTVALUE_FOR.equalsIgnoreCase(attName) || ATTVALUE_FOR2.equalsIgnoreCase(attName)) {
                fore = reader.getAttributeValue(i);
            } else if (ATTVALUE_VALUE.equalsIgnoreCase(attName)) {
                value = reader.getAttributeValue(i);
            } else if (START.equalsIgnoreCase(attName)) {
                startDate = reader.getAttributeValue(i);
            } else if (START_OPEN.equalsIgnoreCase(attName)) {
                startDate = reader.getAttributeValue(i);
                startOpen = true;
            } else if (END.equalsIgnoreCase(attName)) {
                endDate = reader.getAttributeValue(i);
            } else if (END_OPEN.equalsIgnoreCase(attName)) {
                endDate = reader.getAttributeValue(i);
                endOpen = true;
            } else if (TIMESTAMP.equalsIgnoreCase(attName)) {
                timestamp = reader.getAttributeValue(i);
            }
        }

        if (fore.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datakey", element), Issue.Level.SEVERE));
            return;
        }

        if (!value.isEmpty()) {
            //Data attribute value
            ColumnDraft column = element instanceof NodeDraft ? container.getNodeColumn(fore) : container.getEdgeColumn(fore);
            if (column != null) {
                if (column.isDynamic()) {
                    if ((!startDate.isEmpty() || !endDate.isEmpty()) && checkTimerepresentationIsInterval()) {
                        if (startOpen || endOpen) {
                            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "_open_interval", element), Issue.Level.WARNING));
                        }
                        try {
                            element.parseAndSetValue(column.getId(), value, startDate, endDate);
                        } catch (Exception e) {
                            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "attribute_timeinterval_parseerror", element), Issue.Level.SEVERE));
                        }
                    } else if (!timestamp.isEmpty() && checkTimerepresentationIsTimestamp()) {
                        try {
                            element.parseAndSetValue(column.getId(), value, timestamp);
                        } catch (Exception e) {
                            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "attribute_timestamp_parseerror", element), Issue.Level.SEVERE));
                        }
                    } else {
                        try {
                            //Try to parse the whole dynamic type first
                            element.parseAndSetValue(column.getId(), value);
                        } catch (Exception e1) {
                            if (checkTimerepresentationIsInterval()) {
                                //In order to support old atrribute values without start or end, try to parse a single value with infinite interval instead
                                try {
                                    element.parseAndSetValue(column.getId(), value, "-inf", "inf");
                                } catch (Exception e2) {
                                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "attribute_timeinterval_parseerror", element), Issue.Level.SEVERE));
                                }
                            } else {
                                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "attribute_timeset_parseerror", element), Issue.Level.SEVERE));
                            }
                        }
                    }
                } else {
                    Object valueObj = null;
                    try {
                        valueObj = AttributeUtils.parse(value, column.getTypeClass());
                        element.setValue(column.getId(), valueObj);
                    } catch (Exception e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datavalue", fore, element, column.getTitle()), Issue.Level.SEVERE));
                    }
                }
            }
        }
    }

    private void readElementColor(XMLStreamReader reader, ElementDraft element) throws Exception {
        String rStr = "";
        String gStr = "";
        String bStr = "";
        String aStr = "";
        String hexStr = "";

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if ("r".equalsIgnoreCase(attName)) {
                rStr = reader.getAttributeValue(i);
            } else if ("g".equalsIgnoreCase(attName)) {
                gStr = reader.getAttributeValue(i);
            } else if ("b".equalsIgnoreCase(attName)) {
                bStr = reader.getAttributeValue(i);
            } else if ("a".equalsIgnoreCase(attName)) {
                aStr = reader.getAttributeValue(i);
            } else if ("hex".equalsIgnoreCase(attName)) {
                hexStr = reader.getAttributeValue(i);
            }
        }

        if (!hexStr.isEmpty()) {
            element.setColor(hexStr);
            if (!aStr.isEmpty()) {
                float a = (aStr.isEmpty()) ? 1f : Float.parseFloat(aStr);
                if (a < 0f || a > 1f) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "opacityvalue", aStr, element), Issue.Level.WARNING));
                    a = 1f;
                }
                Color cl = element.getColor();
                element.setColor(new Color(cl.getRed(), cl.getGreen(), cl.getBlue(), a));
            }
        } else {
            int r = (rStr.isEmpty()) ? 0 : Integer.parseInt(rStr);
            int g = (gStr.isEmpty()) ? 0 : Integer.parseInt(gStr);
            int b = (bStr.isEmpty()) ? 0 : Integer.parseInt(bStr);
            float a = (aStr.isEmpty()) ? 1f : Float.parseFloat(aStr);
            if (r < 0 || r > 255) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "colorvalue", rStr, element, "r"), Issue.Level.WARNING));
                r = 0;
            }
            if (g < 0 || g > 255) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "colorvalue", gStr, element, "g"), Issue.Level.WARNING));
                g = 0;
            }
            if (b < 0 || b > 255) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "colorvalue", bStr, element, "b"), Issue.Level.WARNING));
                b = 0;
            }
            if (a < 0f || a > 1f) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "opacityvalue", aStr, element), Issue.Level.WARNING));
                a = 1f;
            }

            if (!aStr.isEmpty()) {
                element.setColor(new Color(r, g, b, (int) (a * 255)));
            } else {
                element.setColor(new Color(r, g, b));
            }
        }
    }

    private void readNodePosition(XMLStreamReader reader, NodeDraft node) throws Exception {
        String xStr = "";
        String yStr = "";
        String zStr = "";

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if ("x".equalsIgnoreCase(attName)) {
                xStr = reader.getAttributeValue(i);
            } else if ("y".equalsIgnoreCase(attName)) {
                yStr = reader.getAttributeValue(i);
            } else if ("z".equalsIgnoreCase(attName)) {
                zStr = reader.getAttributeValue(i);
            }
        }

        if (!xStr.isEmpty()) {
            try {
                float x = Float.parseFloat(xStr);
                node.setX(x);
            } catch (NumberFormatException e) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeposition", node, "X"), Issue.Level.WARNING));
            }
        }
        if (!yStr.isEmpty()) {
            try {
                float y = Float.parseFloat(yStr);
                node.setY(y);
            } catch (NumberFormatException e) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeposition", node, "Y"), Issue.Level.WARNING));
            }
        }
        if (!zStr.isEmpty()) {
            try {
                float z = Float.parseFloat(zStr);
                node.setZ(z);
            } catch (NumberFormatException e) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeposition", node, "Z"), Issue.Level.WARNING));
            }
        }
    }

    private void readNodeSize(XMLStreamReader reader, NodeDraft node) throws Exception {
        String attName = reader.getAttributeName(0).getLocalPart();
        if ("value".equalsIgnoreCase(attName)) {
            String sizeStr = reader.getAttributeValue(0);
            if (!sizeStr.isEmpty()) {
                try {
                    float size = Float.parseFloat(sizeStr);
                    node.setSize(size);
                } catch (NumberFormatException e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodesize", node), Issue.Level.WARNING));
                }
            }
        }
    }

    private void readElementSpell(XMLStreamReader reader, ElementDraft element) throws Exception {
        String start = "";
        String end = "";
        String timestamp = "";
        boolean startOpen = false;
        boolean endOpen = false;

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (START.equalsIgnoreCase(attName)) {
                start = reader.getAttributeValue(i);
            } else if (END.equalsIgnoreCase(attName)) {
                end = reader.getAttributeValue(i);
            } else if (START_OPEN.equalsIgnoreCase(attName)) {
                start = reader.getAttributeValue(i);
                startOpen = true;
            } else if (END_OPEN.equalsIgnoreCase(attName)) {
                end = reader.getAttributeValue(i);
                endOpen = true;
            } else if (TIMESTAMP.equalsIgnoreCase(attName)) {
                timestamp = reader.getAttributeValue(i);
            }
        }

        if ((!start.isEmpty() || !end.isEmpty()) && checkTimerepresentationIsInterval()) {
            if (startOpen || endOpen) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "_open_interval", element), Issue.Level.WARNING));
            }
            try {
                element.addInterval(start, end);
            } catch (IllegalArgumentException e) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "_timeinterval_parseerror", element), Issue.Level.SEVERE));
            }
        } else if (!timestamp.isEmpty() && checkTimerepresentationIsTimestamp()) {
            try {
                element.addTimestamp(timestamp);
            } catch (IllegalArgumentException e) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_" + (element instanceof NodeDraft ? "node" : "edge") + "_timestamp_parseerror", element), Issue.Level.SEVERE));
            }
        }
    }

    private void readEdge(XMLStreamReader reader) throws Exception {
        String id = "";
        String label = "";
        String source = "";
        String target = "";
        String weight = "";
        String kind = "";
        String edgeType = "";
        String startDate = "";
        String endDate = "";
        String timestamp = "";
        String timestamps = "";
        String intervals = "";
        boolean startOpen = false;
        boolean endOpen = false;

        //Attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (EDGE_SOURCE.equalsIgnoreCase(attName)) {
                source = reader.getAttributeValue(i);
            } else if (EDGE_TARGET.equalsIgnoreCase(attName)) {
                target = reader.getAttributeValue(i);
            } else if (EDGE_WEIGHT.equalsIgnoreCase(attName)) {
                weight = reader.getAttributeValue(i);
            } else if (EDGE_ID.equalsIgnoreCase(attName)) {
                id = reader.getAttributeValue(i);
            } else if (EDGE_TYPE.equalsIgnoreCase(attName)) {
                edgeType = reader.getAttributeValue(i);
            } else if (EDGE_LABEL.equalsIgnoreCase(attName)) {
                label = reader.getAttributeValue(i);
            } else if (EGDE_KIND.equalsIgnoreCase(attName)) {
                kind = reader.getAttributeValue(i);
            } else if (START.equalsIgnoreCase(attName)) {
                startDate = reader.getAttributeValue(i);
            } else if (END.equalsIgnoreCase(attName)) {
                endDate = reader.getAttributeValue(i);
            } else if (START_OPEN.equalsIgnoreCase(attName)) {
                startDate = reader.getAttributeValue(i);
                startOpen = true;
            } else if (END_OPEN.equalsIgnoreCase(attName)) {
                endDate = reader.getAttributeValue(i);
                endOpen = true;
            } else if (TIMESTAMP.equalsIgnoreCase(attName)) {
                timestamp = reader.getAttributeValue(i);
            } else if (TIMESTAMPS.equalsIgnoreCase(attName)) {
                timestamps = reader.getAttributeValue(i);
            } else if (INTERVALS.equalsIgnoreCase(attName)) {
                intervals = reader.getAttributeValue(i);
            }
        }

        //Create edge
        EdgeDraft edge;
        if (!id.isEmpty()) {
            edge = container.factory().newEdgeDraft(id);
        } else {
            edge = container.factory().newEdgeDraft();
        }

        try {
            NodeDraft nodeSource = container.getNode(source);
            NodeDraft nodeTarget = container.getNode(target);
            edge.setSource(nodeSource);
            edge.setTarget(nodeTarget);
        } catch (Exception e) {
            if (source.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgesource"), Issue.Level.SEVERE));
            } else if (target.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgetarget"), Issue.Level.SEVERE));
            } else {
                report.logIssue(new Issue(e.getMessage(), Issue.Level.SEVERE));
            }
            return;
        }

        //Type
        if (!edgeType.isEmpty()) {
            if (edgeType.equalsIgnoreCase("undirected")) {
                edge.setDirection(EdgeDirection.UNDIRECTED);
            } else if (edgeType.equalsIgnoreCase("directed")) {
                edge.setDirection(EdgeDirection.DIRECTED);
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgetype", edgeType, edge), Issue.Level.SEVERE));
            }
        }

        //Weight
        if (!weight.isEmpty()) {
            try {
                float weightNumber = Float.parseFloat(weight);
                edge.setWeight(weightNumber);
            } catch (NumberFormatException e) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgeweight", edge), Issue.Level.WARNING));
            }
        }

        //Label
        if (!label.isEmpty()) {
            edge.setLabel(label);
        }

        //Kind
        if (!kind.isEmpty()) {
            edge.setType(kind);
        }

        container.addEdge(edge);

        boolean end = false;
        boolean spells = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    if (ATTVALUE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        readElementAttValue(reader, edge);
                    } else if (EDGE_COLOR.equalsIgnoreCase(xmlReader.getLocalName())) {
                        readElementColor(reader, edge);
                    } else if (EDGE_SPELL.equalsIgnoreCase(xmlReader.getLocalName())
                            || EDGE_SPELL2.equalsIgnoreCase(xmlReader.getLocalName())) {
                        readElementSpell(reader, edge);
                        spells = true;
                    }
                    break;

                case XMLStreamReader.END_ELEMENT:
                    if (EDGE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }

        //Dynamic
        if (!spells) {
            if ((!startDate.isEmpty() || !endDate.isEmpty()) && checkTimerepresentationIsInterval()) {
                if (startOpen || endOpen) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edge_open_interval", edge), Issue.Level.WARNING));
                }
                try {
                    edge.addInterval(startDate, endDate);
                } catch (IllegalArgumentException e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edge_timeinterval_parseerror", edge), Issue.Level.SEVERE));
                }
            } else if (!intervals.isEmpty() && checkTimerepresentationIsInterval()) {
                try {
                    edge.addIntervals(intervals);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edge_timeintervals_parseerror", edge), Issue.Level.SEVERE));
                }
            } else if (!timestamp.isEmpty() && checkTimerepresentationIsTimestamp()) {
                try {
                    edge.addTimestamp(timestamp);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edge_timestamp_parseerror", edge), Issue.Level.SEVERE));
                }
            } else if (!timestamps.isEmpty() && checkTimerepresentationIsTimestamp()) {
                try {
                    edge.addTimestamps(timestamps);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edge_timestamps_parseerror", edge), Issue.Level.SEVERE));
                }
            }
        }
    }

    private void readAttributes(XMLStreamReader reader) throws Exception {
        String classAtt = "";
        String typeAtt = "";
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (ATTRIBUTES_CLASS.equalsIgnoreCase(attName)) {
                classAtt = reader.getAttributeValue(i);
            } else if (ATTRIBUTES_TYPE.equalsIgnoreCase(attName) || ATTRIBUTES_TYPE2.equalsIgnoreCase(attName)) {
                typeAtt = reader.getAttributeValue(i);
            }
        }

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    if (ATTRIBUTE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        readAttribute(reader, classAtt, typeAtt);
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ATTRIBUTES.equalsIgnoreCase(xmlReader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    private void readAttribute(XMLStreamReader reader, String classAtt, String typeAtt) throws Exception {
        String id = "";
        String type = "";
        String title = "";
        String defaultStr = "";
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (ATTRIBUTE_ID.equalsIgnoreCase(attName)) {
                id = reader.getAttributeValue(i);
            } else if (ATTRIBUTE_TYPE.equalsIgnoreCase(attName)) {
                type = reader.getAttributeValue(i);
            } else if (ATTRIBUTE_TITLE.equalsIgnoreCase(attName)) {
                title = reader.getAttributeValue(i);
            }
        }

        if (title.isEmpty()) {
            title = id;
        }

        if (!id.isEmpty() && !type.isEmpty()) {
            //Class type
            if (classAtt.isEmpty() || !(classAtt.equalsIgnoreCase("node") || classAtt.equalsIgnoreCase("edge"))) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributeclass", title), Issue.Level.SEVERE));
            }

            //Default?
            boolean end = false;
            boolean defaultFlag = false;
            while (reader.hasNext() && !end) {
                int xmltype = reader.next();

                switch (xmltype) {
                    case XMLStreamReader.START_ELEMENT:
                        if (ATTRIBUTE_DEFAULT.equalsIgnoreCase(xmlReader.getLocalName())) {
                            defaultFlag = true;
                        }
                        break;
                    case XMLStreamReader.CHARACTERS:
                        if (defaultFlag && !xmlReader.isWhiteSpace()) {
                            defaultStr = xmlReader.getText();
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        if (ATTRIBUTE.equalsIgnoreCase(xmlReader.getLocalName())) {
                            end = true;
                        }
                        break;
                }
            }

            //Dynamic?
            boolean dynamic = typeAtt.equalsIgnoreCase("dynamic");

            //Type
            Class attributeType = String.class;
            if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("bool")) {
                attributeType = Boolean.class;
            } else if (type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("int")) {
                attributeType = Integer.class;
            } else if (type.equalsIgnoreCase("long")) {
                attributeType = Long.class;
            } else if (type.equalsIgnoreCase("float")) {
                attributeType = Float.class;
            } else if (type.equalsIgnoreCase("double")) {
                attributeType = Double.class;
            } else if (type.equalsIgnoreCase("string")) {
                attributeType = String.class;
            } else if (type.equalsIgnoreCase("bigdecimal")) {
                attributeType = BigDecimal.class;
            } else if (type.equalsIgnoreCase("biginteger")) {
                attributeType = BigInteger.class;
            } else if (type.equalsIgnoreCase("byte")) {
                attributeType = Byte.class;
            } else if (type.equalsIgnoreCase("char") || type.equalsIgnoreCase("character")) {
                attributeType = Character.class;
            } else if (type.equalsIgnoreCase("short")) {
                attributeType = Short.class;
            } else if (type.equalsIgnoreCase("listboolean")) {
                attributeType = boolean[].class;
            } else if (type.equalsIgnoreCase("listint") || type.equalsIgnoreCase("listinteger")) {
                attributeType = int[].class;
            } else if (type.equalsIgnoreCase("listlong")) {
                attributeType = long[].class;
            } else if (type.equalsIgnoreCase("listfloat")) {
                attributeType = float[].class;
            } else if (type.equalsIgnoreCase("listdouble")) {
                attributeType = double[].class;
            } else if (type.equalsIgnoreCase("liststring")) {
                attributeType = String[].class;
            } else if (type.equalsIgnoreCase("listbigdecimal")) {
                attributeType = BigDecimal[].class;
            } else if (type.equalsIgnoreCase("listbiginteger")) {
                attributeType = BigInteger[].class;
            } else if (type.equalsIgnoreCase("listbyte")) {
                attributeType = byte[].class;
            } else if (type.equalsIgnoreCase("listchar") || type.equalsIgnoreCase("listcharacter")) {
                attributeType = char[].class;
            } else if (type.equalsIgnoreCase("listshort")) {
                attributeType = short[].class;
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributetype2", type), Issue.Level.SEVERE));
                return;
            }

            //Add to model
            ColumnDraft column = null;
            if ("node".equalsIgnoreCase(classAtt) || classAtt.isEmpty()) {
                if (container.getNodeColumn(id) != null) {
                    report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributecolumn_exist", id));
                    return;
                }
                column = container.addNodeColumn(id, attributeType, dynamic);
                column.setTitle(title);
                report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_nodeattribute", title, attributeType.getCanonicalName()));
            } else if ("edge".equalsIgnoreCase(classAtt) || classAtt.isEmpty()) {
                if (container.getEdgeColumn(id) != null) {
                    report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributecolumn_exist", id));
                    return;
                }
                column = container.addEdgeColumn(id, attributeType, dynamic);
                column.setTitle(title);
                report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_edgeattribute", title, attributeType.getCanonicalName()));
            }

            //Default Object
            if (column != null && !defaultStr.isEmpty()) {
                try {
                    column.setDefaultValueString(defaultStr);
                    report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_default", defaultStr, title));
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributedefault", title, attributeType.getCanonicalName()), Issue.Level.SEVERE));
                }
            }
        } else {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributeempty", title), Issue.Level.SEVERE));
        }
    }

    private boolean checkTimerepresentationIsInterval() {
        if (!container.getTimeRepresentation().equals(TimeRepresentation.INTERVAL)) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_timerepresentation_intervalerror"), Issue.Level.SEVERE));
            return false;
        }
        return true;
    }

    private boolean checkTimerepresentationIsTimestamp() {
        if (!container.getTimeRepresentation().equals(TimeRepresentation.TIMESTAMP)) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_timerepresentation_timestamperror"), Issue.Level.SEVERE));
            return false;
        }
        return true;
    }

    @Override
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
