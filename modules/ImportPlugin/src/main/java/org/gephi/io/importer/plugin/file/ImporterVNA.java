/*
 Copyright 2008-2010 Gephi
 Authors : Vojtech Bardiovsky <vojtech.bardiovsky@gmail.com>
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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Netdraw .vna files importer implemented as a simple state machine due to very
 * loose specification of .vna format
 * (http://netwiki.amath.unc.edu/DataFormats/NetDrawVna).
 *
 * @author Vojtech Bardiovsky
 */
public class ImporterVNA implements FileImporter, LongTask {

    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    private EdgeWidthFunction edgeWidthFunction;
    Pattern pattern;

    /**
     * States for the state machine.
     */
    private enum State {

        DEFAULT, NODE_DATA, NODE_PROPERTIES, TIE_DATA,
        NODE_DATA_DEF, NODE_PROPERTIES_DEF, TIE_DATA_DEF
    }

    /**
     * Attributes defined by the VNA file: VNA files allow some or no properties
     * to be defined for nodes and edges.
     */
    private enum Attributes {

        OTHER, NODE_X, NODE_Y, NODE_COLOR, NODE_SIZE,
        NODE_SHAPE, NODE_SHORT_LABEL, EDGE_STRENGTH
    }

    /**
     * Declared column labels for all sections.
     */
    private ColumnDraft[] nodeDataColumns, tieDataColumns;
    private String[] nodePropertiesLabels;
    /**
     * Declared attributes for the node properties declaration section.
     */
    private Attributes[] nodeDataAttributes;
    /**
     * Declared attributes for the edge declaration section.
     */
    private Attributes[] tieAttributes;

    @Override
    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        LineNumberReader lineReader = ImportUtils.getTextReader(reader);
        try {
            importData(lineReader);
        } catch (Exception e) {
            report.logIssue(new Issue(e, Issue.Level.SEVERE));
        } finally {
            try {
                lineReader.close();
            } catch (IOException ex) {
            }
        }
        return !cancel;
    }

    private void importData(LineNumberReader reader) throws Exception {
        List<String> lines = new ArrayList<>();
        while (reader.ready()) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                lines.add(line);
            }
        }

        State state = State.DEFAULT;
        Progress.start(progressTicket, lines.size());
        String[] split;

        final Pattern nodeDataPattern = Pattern.compile("^\\*node data\\s*", Pattern.CASE_INSENSITIVE);
        final Pattern nodePropertiesPattern = Pattern.compile("^\\*node properties\\s*", Pattern.CASE_INSENSITIVE);
        final Pattern tieDataPattern = Pattern.compile("^\\*tie data\\s*", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            if (cancel) {
                return;
            }
            if (nodeDataPattern.matcher(line).matches()) {
                state = State.NODE_DATA_DEF;
                continue;
            } else if (nodePropertiesPattern.matcher(line).matches()) {
                state = State.NODE_PROPERTIES_DEF;
                continue;
            } else if (tieDataPattern.matcher(line).matches()) {
                state = State.TIE_DATA_DEF;
                continue;
            }
            switch (state) {
                case NODE_DATA_DEF:
                    String[] nodeDataLabels = line.split("[\\s,]+");
                    nodeDataColumns = new ColumnDraft[nodeDataLabels.length];
                    for (int i = 1; i < nodeDataLabels.length; i++) {
                        nodeDataColumns[i] = container.addNodeColumn(nodeDataLabels[i], String.class);
                    }
                    state = State.NODE_DATA;
                    break;
                case NODE_PROPERTIES_DEF:
                    // Initialize node properties labels and fill nodeAttributes
                    // if some attributes can be used for NodeDraft
                    nodePropertiesLabels = line.split("[\\s,]+");
                    nodeDataAttributes = new Attributes[nodePropertiesLabels.length];
                    for (int i = 1; i < nodePropertiesLabels.length; i++) {
                        if (nodePropertiesLabels[i].equalsIgnoreCase("x")) {
                            nodeDataAttributes[i] = Attributes.NODE_X;
                        } else if (nodePropertiesLabels[i].equalsIgnoreCase("y")) {
                            nodeDataAttributes[i] = Attributes.NODE_Y;
                        } else if (nodePropertiesLabels[i].equalsIgnoreCase("color")) {
                            nodeDataAttributes[i] = Attributes.NODE_COLOR;
                        } else if (nodePropertiesLabels[i].equalsIgnoreCase("size")) {
                            nodeDataAttributes[i] = Attributes.NODE_SIZE;
                        } else if (nodePropertiesLabels[i].equalsIgnoreCase("shortlabel")) {
                            nodeDataAttributes[i] = Attributes.NODE_SHORT_LABEL;
                        } else if (nodePropertiesLabels[i].equalsIgnoreCase("shape")) {
                            nodeDataAttributes[i] = Attributes.NODE_SHAPE;
                        } else {
                            throw new RuntimeException("Unexpected node parameter at line '" + line + "';");
                        }
                    }
                    state = State.NODE_PROPERTIES;
                    break;
                case TIE_DATA_DEF:
                    String tieDataLabels[] = line.split("[\\s,]+");
                    tieDataColumns = new ColumnDraft[tieDataLabels.length];
                    tieAttributes = new Attributes[tieDataColumns.length];
                    if (tieDataColumns.length < 2) {
                        throw new RuntimeException("Edge data labels definition does not contain two necessary variables ('from' and 'to').");
                    }
                    // Initialize edge labels and fill edgeAttributes if some
                    // attributes can be used for EdgeDraft
                    for (int i = 2; i < tieDataColumns.length; i++) {
                        if (tieDataLabels[i].equalsIgnoreCase("strength")) {
                            tieAttributes[i] = Attributes.EDGE_STRENGTH;
                        } else {
                            tieAttributes[i] = Attributes.OTHER;
                            tieDataColumns[i] = container.addEdgeColumn(tieDataLabels[i], String.class);
                        }
                    }
                    state = State.TIE_DATA;
                    break;
                case NODE_DATA:
                    // new node
                    split = split(line);
                    if (split.length != nodeDataColumns.length) {
                        report.logIssue(new Issue("Number of labels and number of data mismatch in: '" + line + "'", Issue.Level.WARNING));
                        break;
                    }
                    addNode(split);
                    // parse - if parse error => LOG error
                    break;
                case NODE_PROPERTIES:
                    split = split(line);
                    if (split.length != nodePropertiesLabels.length) {
                        report.logIssue(new Issue("Number of labels and number of data mismatch in: '" + line + "'", Issue.Level.WARNING));
                        break;
                    }
                    addNodeProperties(split);
                    // parse - if parse error => LOG error
                    break;
                case TIE_DATA:
                    split = split(line);
                    if (split.length != tieDataColumns.length) {
                        report.logIssue(new Issue("Number of labels and number of data mismatch in: '" + line + "'", Issue.Level.WARNING));
                        break;
                    }
                    addEdge(split);
                    // parse - if parse error => LOG error
                    break;
            }
            Progress.progress(progressTicket);
        }
    }

    /**
     * Splits the line using space separator, but respecting quotes.
     */
    private String[] split(String line) {
        // Pattern for splitting by spaces but respecting quotes.
        if (pattern == null) {
            pattern = Pattern.compile("[^\\s\"]+|\"([^\"]*)\"");
        }
        List<String> tokens = new ArrayList<>();
        Matcher patternMatcher = pattern.matcher(line);
        while (patternMatcher.find()) {
            if ((patternMatcher.group(1)) != null) {
                tokens.add(patternMatcher.group(1));
            } else {
                tokens.add(patternMatcher.group());
            }
        }
        return tokens.toArray(new String[]{});
    }

    private void addNode(String[] nodeData) {
        NodeDraft node;
        String id = nodeData[0];
        if (!container.nodeExists(id)) {
            node = container.factory().newNodeDraft(id);
            container.addNode(node);
        } else {
            node = container.getNode(id);
        }
        for (int i = 1; i < nodeDataColumns.length; i++) {
            node.parseAndSetValue(nodeDataColumns[i].getId(), nodeData[i]);
        }
    }

    private void addNodeProperties(String[] nodeProperties) {
        NodeDraft node;
        String id = nodeProperties[0];
        if (!container.nodeExists(id)) {
            node = container.factory().newNodeDraft(id);
            container.addNode(node);
        } else {
            node = container.getNode(id);
        }
        int i = 0;
        try {
            for (i = 1; i < nodeProperties.length; i++) {
                switch (nodeDataAttributes[i]) {
                    case NODE_X:
                        node.setX(Float.parseFloat(nodeProperties[i]));
                        break;
                    case NODE_Y:
                        node.setY(Float.parseFloat(nodeProperties[i]));
                        break;
                    case NODE_COLOR:
                        node.setColor(nodeProperties[i]);
                        break;
                    case NODE_SIZE:
                        node.setSize(Float.parseFloat(nodeProperties[i]));
                        break;
                    case NODE_SHORT_LABEL:
                        node.setLabel(nodeProperties[i]);
                        break;
                }
            }
        } catch (NumberFormatException e) {
            report.logIssue(new Issue("Error parsing numerical value at '" + nodeProperties[i] + "'.", Issue.Level.WARNING));
        }
    }

    private void addEdge(String[] edgeData) {
        NodeDraft sourceNode;
        if (!container.nodeExists(edgeData[0])) {
            sourceNode = container.factory().newNodeDraft(edgeData[0]);
            container.addNode(sourceNode);
        } else {
            sourceNode = container.getNode(edgeData[0]);
        }
        NodeDraft targetNode;
        if (!container.nodeExists(edgeData[1])) {
            targetNode = container.factory().newNodeDraft(edgeData[1]);
            container.addNode(targetNode);
        } else {
            targetNode = container.getNode(edgeData[1]);
        }

        EdgeDraft edge = container.factory().newEdgeDraft();
        edge.setSource(sourceNode);
        edge.setTarget(targetNode);
        int i = 0;
        try {
            for (i = 2; i < edgeData.length; i++) {
                switch (tieAttributes[i]) {
                    case EDGE_STRENGTH:
                        float weight = Float.parseFloat(edgeData[i]);
                        if (edgeWidthFunction != null) {
                            weight = edgeWidthFunction.computeTransformation(weight);
                        }
                        edge.setWeight(weight);
                        break;
                    case OTHER:
                        edge.parseAndSetValue(tieDataColumns[i].getId(), edgeData[i]);
                        break;
                }
            }
        } catch (NumberFormatException e) {
            report.logIssue(new Issue("Error parsing numerical value at '" + edgeData[i] + "'.", Issue.Level.WARNING));
        }
        container.addEdge(edge);

    }

    public void setEdgeWidthFunction(EdgeWidthFunction function) {
        this.edgeWidthFunction = function;
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
        this.progressTicket = progressTicket;
    }

    public static class EdgeWidthFunction {

        public enum Function {

            LINEAR, SQUARE_ROOT, LOGARITHMIC
        }

        public final Function function;
        public final float coefficient;

        public EdgeWidthFunction(Function function, float coefficient) {
            this.function = function;
            this.coefficient = coefficient;
        }

        public float computeTransformation(float value) {
            switch (function) {
                case LINEAR:
                    return value * coefficient;
                case LOGARITHMIC:
                    return (float) Math.log(value + 10);
                case SQUARE_ROOT:
                    return (float) Math.sqrt(value);
            }
            return 0;
        }

        @Override
        public String toString() {
            switch (function) {
                case LINEAR:
                    return "Linear";
                case LOGARITHMIC:
                    return "Logartihmic";
                case SQUARE_ROOT:
                    return "Square root";
            }
            return null;
        }
    }
}
