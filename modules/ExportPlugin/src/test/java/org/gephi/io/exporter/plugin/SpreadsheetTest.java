package org.gephi.io.exporter.plugin;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.TimeFormat;
import org.gephi.project.api.Workspace;
import org.junit.Test;

public class SpreadsheetTest {

    @Test
    public void testEmptyEdges() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build();

        Utils.assertExporterMatch("spreadsheet/empty.csv", createExporter(graphGenerator));
    }

    @Test
    public void testSingleEdge() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        graphGenerator.getGraph().getEdge("1").setWeight(1.33);

        Utils.assertExporterMatch("spreadsheet/single.csv", createExporter(graphGenerator));
    }

    @Test
    public void testMultigraphEdges() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyMultiGraph();

        Utils.assertExporterMatch("spreadsheet/multigraph.csv", createExporter(graphGenerator));
    }

    @Test
    public void testFieldDelimiter() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setFieldDelimiter(';');

        Utils.assertExporterMatch("spreadsheet/field_delimiter.csv", exporterSpreadsheet);
    }

    @Test
    public void testEdgeLabelQuote() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        graphGenerator.getGraph().getEdge("1").setLabel("str with, and \"quotes\"");

        Utils.assertExporterMatch("spreadsheet/quotes.csv", createExporter(graphGenerator));
    }

    @Test
    public void testNodeAttributeInt() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addIntNodeColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);

        Utils.assertExporterMatch("spreadsheet/int_attribute.csv", exporterSpreadsheet);
    }

    @Test
    public void testNodeAttributeArray() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().generateTinyGraph().addStringArrayNodeColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);

        Utils.assertExporterMatch("spreadsheet/array_attribute.csv", exporterSpreadsheet);
    }

    @Test
    public void testExcludeColumn() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addIntNodeColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExcludedColumns(new LinkedHashSet<>(Arrays.asList("label", GraphGenerator.INT_COLUMN)));

        Utils.assertExporterMatch("spreadsheet/exclude_column.csv", exporterSpreadsheet);
    }

    @Test
    public void testWithoutAttributes() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addIntNodeColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExportAttributes(false);

        Utils.assertExporterMatch("spreadsheet/without_attributes.csv", exporterSpreadsheet);
    }

    @Test
    public void testInfinity() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().generateTinyGraph().addDoubleNodeColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);

        graphGenerator.getGraph().getNode(GraphGenerator.FIRST_NODE)
            .setAttribute(GraphGenerator.DOUBLE_COLUMN, Double.POSITIVE_INFINITY);
        graphGenerator.getGraph().getNode(GraphGenerator.SECOND_NODE)
            .setAttribute(GraphGenerator.DOUBLE_COLUMN, Double.NEGATIVE_INFINITY);

        Utils.assertExporterMatch("spreadsheet/infinity.csv", exporterSpreadsheet);
    }

    @Test
    public void testNodeTimestampSetOtherColumn() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().generateTinyGraph().addTimestampSetColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);

        Utils.assertExporterMatch("spreadsheet/timestampset_column.csv", exporterSpreadsheet);
    }

    @Test
    public void testNodeTimestampSet() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().generateTinyGraph().setTimestampSet();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);

        Utils.assertExporterMatch("spreadsheet/timestampset.csv", exporterSpreadsheet);
    }

    @Test
    public void testNodeTimestampDouble() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().generateTinyGraph().addTimestampDoubleColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExportDynamic(true);

        Utils.assertExporterMatch("spreadsheet/timestamp.csv", exporterSpreadsheet);
    }

    @Test
    public void testNodeTimestampDoubleWithTimeFormat() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().generateTinyGraph().withTimeFormat(TimeFormat.DATE)
                .addTimestampDoubleColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExportDynamic(true);

        Utils.assertExporterMatch("spreadsheet/timestamp_with_time_format.csv", exporterSpreadsheet);
    }

    @Test
    public void testTimestampWithEstimator() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().generateTinyGraph().addTimestampDoubleColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExportDynamic(false);

        Utils.assertExporterMatch("spreadsheet/timestamp_with_estimator.csv", exporterSpreadsheet);
    }

    @Test
    public void testDecimalFormat() throws IOException {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.GERMAN);
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        ExporterSpreadsheet exporterSpreadsheet = createExporter(graphGenerator);
        exporterSpreadsheet.setDecimalFormatSymbols(symbols);
        graphGenerator.getGraph().getEdge("1").setWeight(1.33);

        Utils.assertExporterMatch("spreadsheet/decimal_separator.csv", exporterSpreadsheet);
    }

    @Test
    public void testNumberFormat() throws IOException {
        DecimalFormat numberFormat = new DecimalFormat("0.#");
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        ExporterSpreadsheet exporterSpreadsheet = createExporter(graphGenerator);
        exporterSpreadsheet.setNumberFormat(numberFormat);
        graphGenerator.getGraph().getEdge("1").setWeight(1.353643);

        Utils.assertExporterMatch("spreadsheet/number_format.csv", exporterSpreadsheet);
    }

    @Test
    public void testLargeNumber() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addIntNodeColumn();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        graphGenerator.getGraph().getNode("1").setAttribute(GraphGenerator.INT_COLUMN, 1000000000);

        Utils.assertExporterMatch("spreadsheet/large_number.csv", exporterSpreadsheet);
    }

    @Test
    public void testPositions() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExportPosition(true);

        Utils.assertExporterMatch("spreadsheet/positions.csv", exporterSpreadsheet);
    }

    @Test
    public void testPositionsWithZ() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExportPosition(true);

        graphGenerator.getGraph().getNode(GraphGenerator.FIRST_NODE).setZ(5);

        Utils.assertExporterMatch("spreadsheet/positions_z.csv", exporterSpreadsheet);
    }

    @Test
    public void testSize() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExportSize(true);

        Utils.assertExporterMatch("spreadsheet/size.csv", exporterSpreadsheet);
    }

    @Test
    public void testNormalize() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExportSize(true);
        exporterSpreadsheet.setExportPosition(true);
        exporterSpreadsheet.setNormalize(true);

        graphGenerator.getGraph().getNode(GraphGenerator.FIRST_NODE).setX(-100);
        graphGenerator.getGraph().getNode(GraphGenerator.FIRST_NODE).setY(300);
        graphGenerator.getGraph().getNode(GraphGenerator.FIRST_NODE).setSize(10);
        graphGenerator.getGraph().getNode(GraphGenerator.SECOND_NODE).setX(100);
        graphGenerator.getGraph().getNode(GraphGenerator.SECOND_NODE).setY(-300);
        graphGenerator.getGraph().getNode(GraphGenerator.SECOND_NODE).setSize(20);

        Utils.assertExporterMatch("spreadsheet/normalize.csv", exporterSpreadsheet);
    }

    @Test
    public void testColor() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);
        exporterSpreadsheet.setExportColors(true);

        Utils.assertExporterMatch("spreadsheet/color.csv", exporterSpreadsheet);
    }

    @Test
    public void testEdgeColor() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        ExporterSpreadsheet exporterSpreadsheet = createExporter(graphGenerator);
        exporterSpreadsheet.setExportColors(true);

        Utils.assertExporterMatch("spreadsheet/edge_color.csv", exporterSpreadsheet);
    }

    @Test
    public void testUtf8() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        graphGenerator.getGraph().getNode(GraphGenerator.FIRST_NODE).setLabel("Säännöt");
        ExporterSpreadsheet exporterSpreadsheet = createNodeExporter(graphGenerator);

        Utils.assertExporterMatch("spreadsheet/utf8.csv", exporterSpreadsheet);
    }

    // Utilities

    private static ExporterSpreadsheet createNodeExporter(GraphGenerator graphGenerator) {
        ExporterSpreadsheet exporterSpreadsheet = createExporter(graphGenerator);
        exporterSpreadsheet.setTableToExport(ExporterSpreadsheet.ExportTable.NODES);
        return exporterSpreadsheet;
    }

    private static ExporterSpreadsheet createExporter(GraphGenerator graphGenerator) {
        Workspace workspace = graphGenerator.getWorkspace();
        ExporterSpreadsheet exporterSpreadsheet = new ExporterSpreadsheet();
        exporterSpreadsheet.setWorkspace(workspace);
        return exporterSpreadsheet;
    }
}
