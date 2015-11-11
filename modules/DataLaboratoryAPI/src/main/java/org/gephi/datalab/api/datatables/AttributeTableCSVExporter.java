/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.api.datatables;

import com.csvreader.CsvWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;
import org.openide.util.Lookup;

public class AttributeTableCSVExporter {

    private static final Character DEFAULT_SEPARATOR = ',';
    public static final int FAKE_COLUMN_EDGE_SOURCE = -1;
    public static final int FAKE_COLUMN_EDGE_TARGET = -2;
    public static final int FAKE_COLUMN_EDGE_TYPE = -3;
    
    /**
     * <p>Export a AttributeTable to the specified file.</p>
     *
     * @param graph Graph containing the table and rows
     * @param table Table to export. Cannot be null
     * @param out Ouput stream to write. Cannot be null.
     * @throws IOException When an error happens while writing the file
     */
    public static void writeCSVFile(Graph graph, Table table, OutputStream out) throws IOException {
        writeCSVFile(graph, table, out, null, null, null, null);
    }
    
    /**
     * <p>Export a AttributeTable to the specified file.</p>
     *
     * @param graph Graph containing the table and rows
     * @param table Table to export. Cannot be null
     * @param file File to write. Cannot be null.
     * @throws IOException When an error happens while writing the file
     */
    public static void writeCSVFile(Graph graph, Table table, File file) throws IOException {
        if(file == null){
            throw new IllegalArgumentException("file cannot be null");
        }
        
        writeCSVFile(graph, table, new FileOutputStream(file), null, null, null, null);
    }
    
    /**
     * <p>Export a AttributeTable to the specified file.</p>
     *
     * @param graph Graph containing the table and rows
     * @param table Table to export. Cannot be null
     * @param file File to write. Cannot be null.
     * @param separator Separator to use for separating values of a row in the CSV file. If null ',' will be used.
     * @param charset Charset encoding for the file. If null, UTF-8 will be used
     * @param columnIndexesToExport Indicates the indexes of the columns to export. All columns will be exported if null. For special columns in edges table, use {@code FAKE_COLUMN_EDGE_} values in this class.
     * @param rows Elements (table rows: nodes/edges) to include in the exported file. Cannot be null. If null, all nodes/edges will be exported.
     * @throws IOException When an error happens while writing the file
     */
    public static void writeCSVFile(Graph graph, Table table, File file, Character separator, Charset charset, Integer[] columnIndexesToExport, Element[] rows) throws IOException {
        if(file == null){
            throw new IllegalArgumentException("file cannot be null");
        }
        
        writeCSVFile(graph, table, new FileOutputStream(file), separator, charset, columnIndexesToExport, rows);
    }
    
    /**
     * <p>Export a AttributeTable to the specified file.</p>
     *
     * @param graph Graph containing the table and rows
     * @param table Table to export. Cannot be null
     * @param out Ouput stream to write. Cannot be null.
     * @param separator Separator to use for separating values of a row in the CSV file. If null ',' will be used.
     * @param charset Charset encoding for the file. If null, UTF-8 will be used
     * @param columnIndexesToExport Indicates the indexes of the columns to export. All columns will be exported if null. For special columns in edges table, use {@code FAKE_COLUMN_EDGE_} values in this class.
     * @param rows Elements (table rows: nodes/edges) to include in the exported file. Cannot be null. If null, all nodes/edges will be exported.
     * @throws IOException When an error happens while writing the file
     */
    public static void writeCSVFile(Graph graph, Table table, OutputStream out, Character separator, Charset charset, Integer[] columnIndexesToExport, Element[] rows) throws IOException {
        if(out == null){
            throw new IllegalArgumentException("out cannot be null");
        }
        
        if (separator == null) {
            separator = DEFAULT_SEPARATOR;
        }

        if(charset == null){
            charset = Charset.forName("UTF-8");
        }
        
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        boolean isEdgeTable = ac.isEdgeTable(table);
        if(rows == null){
            if(isEdgeTable){
                rows = graph.getEdges().toArray();
            }else{
                rows = graph.getNodes().toArray();
            }
        }
        
        TimeFormat timeFormat = graph.getModel().getTimeFormat();

        if (columnIndexesToExport == null) {
            List<Integer> columnIndexesToExportList = new ArrayList<Integer>();
            
            //Add special columns for edges table:
            if(isEdgeTable){
                columnIndexesToExportList.add(FAKE_COLUMN_EDGE_SOURCE);
                columnIndexesToExportList.add(FAKE_COLUMN_EDGE_TARGET);
                columnIndexesToExportList.add(FAKE_COLUMN_EDGE_TYPE);
            }
            
            for (Column column : table) {
                columnIndexesToExportList.add(column.getIndex());
            }
            columnIndexesToExport = columnIndexesToExportList.toArray(new Integer[0]);
        }

        CsvWriter writer = new CsvWriter(out, separator, charset);



        //Write column headers:
        for (int column = 0; column < columnIndexesToExport.length; column++) {
            int columnIndex = columnIndexesToExport[column];

            if (columnIndex == FAKE_COLUMN_EDGE_SOURCE) {
                writer.write("Source");
            } else if (columnIndex == FAKE_COLUMN_EDGE_TARGET) {
                writer.write("Target");
            } else if (columnIndex == FAKE_COLUMN_EDGE_TYPE) {
                writer.write("Type");
            } else {
                writer.write(table.getColumn(columnIndex).getTitle(), true);
            }

        }
        writer.endRecord();

        //Write rows:
        Object value;
        String text;
        for (int row = 0; row < rows.length; row++) {
            for (int i = 0; i < columnIndexesToExport.length; i++) {
                int columnIndex = columnIndexesToExport[i];
                
                if (columnIndex == FAKE_COLUMN_EDGE_SOURCE) {
                    value = ((Edge)rows[row]).getSource().getId();
                } else if (columnIndex == FAKE_COLUMN_EDGE_TARGET) {
                    value = ((Edge)rows[row]).getTarget().getId();
                } else if (columnIndex == FAKE_COLUMN_EDGE_TYPE) {
                    value = ((Edge)rows[row]).isDirected() ? "Directed" : "Undirected";
                } else {
                    value = rows[row].getAttribute(table.getColumn(columnIndex));
                }

                if (value != null) {
                    if(value instanceof TimestampSet){
                        text = ((TimestampSet) value).toString(timeFormat);
                    } else if(value instanceof TimestampMap){
                        text = ((TimestampMap) value).toString(timeFormat);
                    } else if(value instanceof IntervalSet){
                        text = ((IntervalSet) value).toString(timeFormat);
                    } else if(value instanceof IntervalMap){
                        text = ((IntervalMap) value).toString(timeFormat);
                    } else {
                        text = value.toString();
                    }
                } else {
                    text = "";
                }
                writer.write(text, true);
            }
            writer.endRecord();
        }
        writer.close();
    }
}
