/*
Copyright 2008-2016 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2016 Gephi Consortium. All rights reserved.

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

Portions Copyrighted 2016 Gephi Consortium.
 */
package org.gephi.io.importer.plugin.file.spreadsheet;

import java.io.File;
import org.gephi.io.importer.plugin.file.spreadsheet.process.ImportNodesProcess;
import org.gephi.io.importer.plugin.file.spreadsheet.process.AbstractImportProcess;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalIntegerMap;
import org.gephi.graph.api.types.IntervalLongMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.IntervalStringMap;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.graph.api.types.TimestampLongMap;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.plugin.file.spreadsheet.process.ImportAdjacencyListProcess;
import org.gephi.io.importer.plugin.file.spreadsheet.process.ImportEdgesProcess;
import org.gephi.io.importer.plugin.file.spreadsheet.process.ImportMatrixProcess;
import org.gephi.io.importer.plugin.file.spreadsheet.process.SpreadsheetGeneralConfiguration;
import org.gephi.io.importer.plugin.file.spreadsheet.process.SpreadsheetGeneralConfiguration.Mode;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetParser;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetRow;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.joda.time.DateTimeZone;
import org.openide.util.Exceptions;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractImporterSpreadsheet implements FileImporter, FileImporter.FileAware, LongTask {

    private static final int MAX_ROWS_TO_ANALYZE_COLUMN_TYPES = 25;

    protected ContainerLoader container;
    protected Report report;
    protected ProgressTicket progressTicket;
    protected boolean cancel = false;

    protected AbstractImportProcess importer = null;

    protected File file;

    //General configuration:
    protected final SpreadsheetGeneralConfiguration generalConfig = new SpreadsheetGeneralConfiguration();

    @Override
    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();

        this.container.setTimeRepresentation(generalConfig.getTimeRepresentation());
        this.container.setTimeZone(generalConfig.getTimeZone());

        try (SheetParser parser = createParser()) {
            switch (getMode()) {
                case NODES_TABLE:
                    importer = new ImportNodesProcess(generalConfig, parser, container, progressTicket);
                    break;
                case EDGES_TABLE:
                    importer = new ImportEdgesProcess(generalConfig, parser, container, progressTicket);
                    break;
                case ADJACENCY_LIST:
                    importer = new ImportAdjacencyListProcess(generalConfig, container, progressTicket, parser);
                    break;
                case MATRIX:
                    importer = new ImportMatrixProcess(generalConfig, container, progressTicket, parser);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown mode " + getMode());
            }

            importer.execute();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (importer != null) {
                report.append(importer.getReport());
                importer = null;
            }
        }

        return !cancel;
    }

    public abstract SheetParser createParser() throws IOException;

    public abstract SheetParser createParserWithoutHeaders() throws IOException;

    public Map<String, Integer> getHeadersMap() throws IOException {
        try (SheetParser parser = createParser()) {
            return parser.getHeaderMap();
        }
    }

    public List<SheetRow> getFirstRows(int maxRows) throws IOException {
        try (SheetParser parser = createParser()) {
            return getFirstRows(parser, maxRows);
        }
    }

    public List<SheetRow> getFirstRows(SheetParser parser, int maxRows) throws IOException {
        List<SheetRow> rows = new ArrayList<>();

        Iterator<SheetRow> iterator = parser.iterator();
        for (int i = 0; i < maxRows && iterator.hasNext(); i++) {
            rows.add(iterator.next());
        }

        return rows;
    }

    protected void autoDetectImportMode() {
        try {
            SheetParser parser = createParserWithoutHeaders();

            Mode mode = null;

            Iterator<SheetRow> iterator = parser.iterator();
            if (iterator.hasNext()) {
                SheetRow firstRow = iterator.next();

                if (firstRow.get(0) == null || firstRow.get(0).trim().isEmpty()) {
                    mode = Mode.MATRIX;
                } else {
                    //Detect very probable edges table:
                    for (int i = 0; i < firstRow.size(); i++) {
                        String value = firstRow.get(i);
                        if ("source".equalsIgnoreCase(value) || "target".equalsIgnoreCase(value)) {
                            mode = Mode.EDGES_TABLE;
                            break;
                        }
                    }

                    //Detect probable nodes table:
                    if (mode == null) {
                        for (int i = 0; i < firstRow.size(); i++) {
                            String value = firstRow.get(i);
                            if ("id".equalsIgnoreCase(value) || "label".equalsIgnoreCase(value) || "timeset".equalsIgnoreCase(value)) {
                                mode = Mode.NODES_TABLE;
                            }
                        }
                    }
                }
            }

            if (mode == null) {
                //Default adjacency list:
                mode = Mode.ADJACENCY_LIST;
            }

            setMode(mode);
        } catch (IOException ex) {
            //NOOP
        }
    }

    protected void autoDetectColumnTypes() {
        try (SheetParser parser = createParser()) {
            List<SheetRow> rows = getFirstRows(parser, MAX_ROWS_TO_ANALYZE_COLUMN_TYPES);
            int rowCount = rows.size();

            if (rowCount == 0) {
                return;
            }

            Map<String, Integer> headerMap = parser.getHeaderMap();
            if (headerMap.isEmpty()) {
                return;
            }

            Map<String, LinkedHashSet<Class>> classMatchByHeader = new HashMap<>();

            List<Class> classesToTry = Arrays.asList(new Class[]{
                //Classes to check, in order of preference
                Boolean.class,
                Integer.class,
                Long.class,
                BigInteger.class,
                Double.class,
                BigDecimal.class,
                IntervalIntegerMap.class,
                IntervalLongMap.class,
                IntervalDoubleMap.class,
                IntervalStringMap.class,
                IntervalSet.class,
                TimestampIntegerMap.class,
                TimestampLongMap.class,
                TimestampDoubleMap.class,
                TimestampStringMap.class,
                TimestampSet.class
            });

            //Initialize:
            for (String column : headerMap.keySet()) {
                classMatchByHeader.put(column, new LinkedHashSet<Class>());

                classMatchByHeader.get(column).addAll(classesToTry); //First assume all values match
            }

            //Try to parse all types:
            for (SheetRow row : rows) {
                for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                    String column = entry.getKey();
                    int index = entry.getValue();
                    String value = row.get(index);
                    if (value != null) {
                        value = value.trim();
                    }

                    LinkedHashSet<Class> columnMatches = classMatchByHeader.get(column);

                    for (Class clazz : classesToTry) {
                        if (columnMatches.contains(clazz)) {
                            if (value != null && !value.isEmpty()) {
                                if (clazz.equals(Boolean.class)) {//Special case for booleans to not accept 0/1, only true or false
                                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                                        columnMatches.remove(clazz);
                                    }
                                } else {
                                    try {
                                        Object parsed;
                                        if (clazz.equals(Integer.class)) {
                                            parsed = Integer.parseInt(value);
                                        } else if (clazz.equals(Long.class)) {
                                            parsed = Long.parseLong(value);
                                        } else if (clazz.equals(BigInteger.class)) {
                                            parsed = new BigInteger(value);
                                        } else if (clazz.equals(Double.class)) {
                                            parsed = Double.parseDouble(value);
                                        } else if (clazz.equals(BigDecimal.class)) {
                                            parsed = new BigDecimal(value);
                                        } else {
                                            parsed = AttributeUtils.parse(value, clazz);
                                        }

                                        if (parsed instanceof TimeMap && ((TimeMap) parsed).isEmpty()) {
                                            parsed = null;//Actually invalid
                                        }
                                        if (parsed instanceof TimeSet && ((TimeSet) parsed).isEmpty()) {
                                            parsed = null;//Actually invalid
                                        }

                                        if (parsed == null) {
                                            columnMatches.remove(clazz);//Non empty value produced null, invalid parsing
                                        }
                                    } catch (Exception parseError) {
                                        //Invalid value
                                        columnMatches.remove(clazz);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //Obtain best match for each column:
            TimeRepresentation foundTimeRepresentation = TimeRepresentation.INTERVAL;
            for (String column : headerMap.keySet()) {
                LinkedHashSet<Class> columnMatches = classMatchByHeader.get(column);

                Class detectedClass = String.class;//Default

                //Use the detected type matching if any:
                if (!columnMatches.isEmpty() && columnMatches.size() != classesToTry.size()) {
                    detectedClass = columnMatches.iterator().next();//First match
                }

                //Change some typical column types to expected types when possible:
                if (column.equalsIgnoreCase("id") || column.equalsIgnoreCase("label")) {
                    detectedClass = String.class;
                }

                if (detectedClass.equals(String.class)) {//No other thing than String found, try to guess very probable dynamic types:
                    if (column.toLowerCase().contains("interval")) {
                        detectedClass = IntervalSet.class;
                    }

                    if (column.toLowerCase().contains("timestamp")) {
                        detectedClass = TimestampSet.class;
                    }

                    if (column.equalsIgnoreCase("timeset")) {
                        if (foundTimeRepresentation == TimeRepresentation.INTERVAL) {
                            detectedClass = IntervalSet.class;
                        } else {
                            detectedClass = TimestampSet.class;
                        }
                    }
                }

                if (getMode() == Mode.EDGES_TABLE) {
                    if (column.equalsIgnoreCase("source") || column.equalsIgnoreCase("target") || column.equalsIgnoreCase("type") || column.equalsIgnoreCase("kind")) {
                        detectedClass = String.class;
                    }

                    //Favor double types for weight column:
                    if (column.equalsIgnoreCase("weight")) {
                        if (columnMatches.contains(Double.class)) {
                            detectedClass = Double.class;
                        } else if (columnMatches.contains(IntervalDoubleMap.class)) {
                            detectedClass = IntervalDoubleMap.class;
                        } else if (columnMatches.contains(TimestampDoubleMap.class)) {
                            detectedClass = TimestampDoubleMap.class;
                        }
                    }
                }

                setColumnClass(column, detectedClass);

                if (TimestampSet.class.isAssignableFrom(detectedClass) || TimestampMap.class.isAssignableFrom(detectedClass)) {
                    foundTimeRepresentation = TimeRepresentation.TIMESTAMP;
                }
            }

            setTimeRepresentation(foundTimeRepresentation);
        } catch (IOException ex) {
            //NOOP
        }
    }

    public void refreshAutoDetections() {
        autoDetectImportMode();
        autoDetectColumnTypes();
    }

    @Override
    public void setReader(Reader reader) {
        //We can't use a reader since we might need to read the file many times (get the headers first, then read again...)
        //See setFile(File file)
    }

    @Override
    public void setFile(File file) {
        File previousFile = this.file;
        this.file = file;

        if (previousFile == null && file != null) {
            //First time setting the file, auto detect settings. They can be changed later by the programmer/UI user.
            //But not auto detect again if the importer controller sets the file a second time, or that would cancel the possible changes made by the programmer/UI user.
            refreshAutoDetections();
        }
    }

    public File getFile() {
        return file;
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
        if (importer != null) {
            importer.cancel();
            importer = null;
        }
        return cancel = true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public Mode getMode() {
        return generalConfig.getMode();
    }

    public void setMode(Mode table) {
        generalConfig.setTable(table);
    }

    public TimeRepresentation getTimeRepresentation() {
        return generalConfig.getTimeRepresentation();
    }

    public void setTimeRepresentation(TimeRepresentation timeRepresentation) {
        generalConfig.setTimeRepresentation(timeRepresentation);
    }

    public DateTimeZone getTimeZone() {
        return generalConfig.getTimeZone();
    }

    public void setTimeZone(DateTimeZone timeZone) {
        generalConfig.setTimeZone(timeZone);
    }

    public Map<String, Class> getColumnsClasses() {
        return generalConfig.getColumnsClasses();
    }

    public void setColumnsClasses(Map<String, Class> columnsClasses) {
        generalConfig.setColumnsClasses(columnsClasses);
    }

    public Class getColumnClass(String column) {
        return generalConfig.getColumnClass(column);
    }

    public void setColumnClass(String column, Class clazz) {
        generalConfig.setColumnClass(column, clazz);
    }
}
