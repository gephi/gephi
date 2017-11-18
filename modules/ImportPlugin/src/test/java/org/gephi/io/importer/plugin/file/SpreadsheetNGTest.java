/*
Copyright 2008-2017 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2017 Gephi Consortium. All rights reserved.

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

Portions Copyrighted 2017 Gephi Consortium.
 */
package org.gephi.io.importer.plugin.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalLongMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.io.exporter.plugin.ExporterSpreadsheet;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeMergeStrategy;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.file.spreadsheet.ImporterSpreadsheetCSV;
import org.gephi.io.importer.plugin.file.spreadsheet.ImporterSpreadsheetExcel;
import org.gephi.io.importer.plugin.file.spreadsheet.process.SpreadsheetGeneralConfiguration.Mode;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/**
 *
 * @author Eduardo Ramos
 */
public class SpreadsheetNGTest {

    private final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
    private final ImportController importController = Lookup.getDefault().lookup(ImportController.class);
    private final GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
    private Workspace workspace;
    private String testName = null;

    @BeforeMethod
    public void setup(Method method) {
        testName = method.getName();
        System.out.println("Starting test: " + testName);

        projectController.newProject();
        workspace = projectController.getCurrentWorkspace();
    }

    @AfterMethod
    public void teardown() {
        projectController.closeCurrentProject();
        workspace = null;
    }

    @Test
    public void testAdjacencyList() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/adj_list.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ';');
        Assert.assertEquals(importer.getMode(), Mode.ADJACENCY_LIST);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);
        Assert.assertTrue(container.getReport().isEmpty());

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testAdjacencyList_AutoDetectImporter() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/adj_list.csv"));

        importController.importFile(file);

        Container container = importController.importFile(file);
        Assert.assertNotNull(container);
        Assert.assertTrue(container.getReport().isEmpty());

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testMatrix_CSV() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/matrix.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ',');
        Assert.assertEquals(importer.getMode(), Mode.MATRIX);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);
        Assert.assertTrue(container.getReport().isEmpty());

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testMatrix_CSV_AutoDetectImporter() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/matrix.csv"));

        Container container = importController.importFile(file);
        Assert.assertNotNull(container);
        Assert.assertTrue(container.getReport().isEmpty());

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testMatrix_Excel() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/matrix.xlsx"));

        ImporterSpreadsheetExcel importer = new ImporterSpreadsheetExcel();

        importer.setFile(file);

        Assert.assertEquals(importer.getMode(), Mode.MATRIX);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);
        Assert.assertTrue(container.getReport().isEmpty());

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testComplexMatrix() throws FileNotFoundException, IOException {
        //File from https://github.com/gephi/gephi/issues/1661
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/complex_matrix.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ';');
        Assert.assertEquals(importer.getMode(), Mode.MATRIX);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);
        Assert.assertFalse(container.getReport().isEmpty());//Missing labels at the start

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testEdgesTableRepeatedWithIds() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_repeated_with_ids.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ',');
        Assert.assertEquals(importer.getMode(), Mode.EDGES_TABLE);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);
        Assert.assertFalse(container.getReport().isEmpty());//Repeated edge id issue

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testEdgesTableRepeatedWithoutIds_Merged() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_repeated_without_ids.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ',');
        Assert.assertEquals(importer.getMode(), Mode.EDGES_TABLE);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testEdgesTableRepeatedWithoutIds_Merge_Disabled() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_repeated_without_ids.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ',');
        Assert.assertEquals(importer.getMode(), Mode.EDGES_TABLE);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);
        container.getLoader().setEdgesMergeStrategy(EdgeMergeStrategy.NO_MERGE);

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testEdgesTableWithTimeset_Timestamp() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_with_timeset_timestamps.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ' ');
        Assert.assertEquals(importer.getMode(), Mode.EDGES_TABLE);
        Assert.assertEquals(importer.getTimeRepresentation(), TimeRepresentation.TIMESTAMP);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        graphController.getGraphModel(workspace).setTimeFormat(TimeFormat.DATE);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testEdgesTableWithTimeset_Interval() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_with_timeset_intervals.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ' ');
        Assert.assertEquals(importer.getMode(), Mode.EDGES_TABLE);
        Assert.assertEquals(importer.getTimeRepresentation(), TimeRepresentation.INTERVAL);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        graphController.getGraphModel(workspace).setTimeFormat(TimeFormat.DATE);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testEdgesTableDynamicWeightsMerged() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_dynamic_weights.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ',');
        Assert.assertEquals(importer.getMode(), Mode.EDGES_TABLE);
        Assert.assertEquals(importer.getTimeRepresentation(), TimeRepresentation.INTERVAL);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    @Test
    public void testEdgesTableTypesTest() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_types_test.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ',');
        Assert.assertEquals(importer.getMode(), Mode.EDGES_TABLE);
        Assert.assertEquals(importer.getTimeRepresentation(), TimeRepresentation.INTERVAL);

        Map<String, Class> columnsClasses = importer.getColumnsClasses();

        Assert.assertEquals(columnsClasses.get("id"), String.class);
        Assert.assertEquals(columnsClasses.get("label"), String.class);
        Assert.assertEquals(columnsClasses.get("source"), String.class);
        Assert.assertEquals(columnsClasses.get("target"), String.class);
        Assert.assertEquals(columnsClasses.get("kind"), String.class);
        Assert.assertEquals(columnsClasses.get("type"), String.class);
        Assert.assertEquals(columnsClasses.get("weight"), Double.class);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet(false);
    }

    @Test
    public void testEdgesTableTypesTest_AutoDetectImporter() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_types_test.csv"));

        Container container = importController.importFile(file);
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet(false);
    }

    @Test
    public void testNodesTableTypesTest() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/nodes_table_types_test.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getCharset(), Charset.forName("UTF-8"));
        Assert.assertEquals(importer.getFieldDelimiter(), ',');
        Assert.assertEquals(importer.getMode(), Mode.NODES_TABLE);
        Assert.assertEquals(importer.getTimeRepresentation(), TimeRepresentation.INTERVAL);

        Map<String, Class> columnsClasses = importer.getColumnsClasses();

        Assert.assertEquals(columnsClasses.get("id"), String.class);
        Assert.assertEquals(columnsClasses.get("label"), String.class);
        Assert.assertEquals(columnsClasses.get("int"), Integer.class);
        Assert.assertEquals(columnsClasses.get("long"), Long.class);
        Assert.assertEquals(columnsClasses.get("double"), Double.class);
        Assert.assertEquals(columnsClasses.get("boolean"), Boolean.class);
        Assert.assertEquals(columnsClasses.get("timeset"), IntervalSet.class);
        Assert.assertEquals(columnsClasses.get("string"), String.class);
        Assert.assertEquals(columnsClasses.get("intervallongmap"), IntervalLongMap.class);
        Assert.assertEquals(columnsClasses.get("bigint"), BigInteger.class);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        checkNodesSpreadsheet();
    }

    @Test
    public void testRepeatedHeaders() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/repeated_headers.xls"));

        ImporterSpreadsheetExcel importer = new ImporterSpreadsheetExcel();

        importer.setFile(file);

        Assert.assertEquals(importer.getMode(), Mode.NODES_TABLE);

        Map<String, Class> columnsClasses = importer.getColumnsClasses();

        Assert.assertEquals(columnsClasses.size(), 3);
        Assert.assertEquals(columnsClasses.get("id"), String.class);
        Assert.assertEquals(columnsClasses.get("string"), String.class);
        Assert.assertEquals(columnsClasses.get("String"), String.class);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        checkNodesSpreadsheet();
    }

    @Test
    public void testUTF8Chars() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/test_utf8_chars.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getMode(), Mode.NODES_TABLE);

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        checkNodesSpreadsheet();
    }
    
    @Test
    public void testUTF8CharsWithBOM() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/test_utf8_chars_with_bom.csv"));

        ImporterSpreadsheetCSV importer = new ImporterSpreadsheetCSV();

        importer.setFile(file);

        Assert.assertEquals(importer.getMode(), Mode.NODES_TABLE);
        Assert.assertEquals(importer.getCharset().name(), "UTF-8");

        Container container = importController.importFile(
                file, importer
        );
        Assert.assertNotNull(container);

        importController.process(container, new DefaultProcessor(), workspace);

        checkNodesSpreadsheet();
    }
    
    @Test
    public void testEdgesTableOppositeForceUndirected_Merged() throws FileNotFoundException, IOException {
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_opposite_force_undirected_merged.csv"));

        Container container = importController.importFile(file);
        Assert.assertNotNull(container);

        //Force undirected:
        container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
        container.getLoader().setEdgesMergeStrategy(EdgeMergeStrategy.SUM);

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }
    
    @Test
    public void testEdgesTableOppositeForceUndirected_Issue1848() throws FileNotFoundException, IOException {
        //https://github.com/gephi/gephi/issues/1848
        File file = FileUtil.archiveOrDirForURL(SpreadsheetNGTest.class.getResource("/org/gephi/io/importer/plugin/file/spreadsheet/edges_table_opposite_force_undirected_issue_1848.csv"));

        Container container = importController.importFile(file);
        Assert.assertNotNull(container);

        //Force undirected:
        container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
        container.getLoader().setEdgesMergeStrategy(EdgeMergeStrategy.SUM);

        importController.process(container, new DefaultProcessor(), workspace);

        checkEdgesSpreadsheet();
    }

    private void checkEdgesSpreadsheet() throws IOException {
        checkEdgesSpreadsheet(true);
    }

    private void checkEdgesSpreadsheet(boolean ignoreId) throws IOException {
        File tmpFile = File.createTempFile(testName, ".csv");
        Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8");

        ExporterSpreadsheet exporter = new ExporterSpreadsheet();
        exporter.setWorkspace(workspace);
        exporter.setTableToExport(ExporterSpreadsheet.ExportTable.EDGES);
        exporter.setWriter(writer);

        if (ignoreId) {
            Table edgeTable = graphController.getGraphModel(workspace).getEdgeTable();
            LinkedHashSet<String> columnIdsToExport = new LinkedHashSet<>();
            for (Column column : edgeTable) {
                if (!column.getId().equals("id")) {
                    columnIdsToExport.add(column.getId());
                }
            }
            exporter.setColumnIdsToExport(columnIdsToExport);
        }

        exporter.execute();

        String result = Files.readFile(tmpFile).trim().replace("\r", "");
        String expected = Files.readFile(SpreadsheetNGTest.class.getResourceAsStream("/org/gephi/io/importer/plugin/file/spreadsheet/expected/" + testName.replace("_AutoDetectImporter", "") + "_edges.csv")).trim();

        Assert.assertEquals(result, expected);
    }

    private void checkNodesSpreadsheet() throws IOException {
        File tmpFile = File.createTempFile(testName, ".csv");
        Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8");

        ExporterSpreadsheet exporter = new ExporterSpreadsheet();
        exporter.setWorkspace(workspace);
        exporter.setTableToExport(ExporterSpreadsheet.ExportTable.NODES);
        exporter.setWriter(writer);

        exporter.execute();

        String result = Files.readFile(tmpFile).trim().replace("\r", "");

        String expected = Files.readFile(SpreadsheetNGTest.class.getResourceAsStream("/org/gephi/io/importer/plugin/file/spreadsheet/expected/" + testName + "_nodes.csv")).trim();

        Assert.assertEquals(result, expected);
    }
}
