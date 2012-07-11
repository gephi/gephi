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
package org.gephi.utils;

import com.csvreader.CsvWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class TableCSVExporter {

    private static final Character DEFAULT_SEPARATOR = ',';

    /**
     * <p>Export a JTable to the specified file.</p>
     * @param table Table to export
     * @param file File to write
     * @param separator Separator to use for separating values of a row in the CSV file. If null ',' will be used.
     * @param charset Charset encoding for the file
     * @param columnsToExport Indicates the indexes of the columns to export. All columns will be exported if null
     * @throws IOException When an error happens while writing the file
     */
    public static void writeCSVFile(JTable table, File file, Character separator, Charset charset, Integer[] columnsToExport) throws IOException {
        TableModel model = table.getModel();
        FileOutputStream out = new FileOutputStream(file);
        if (separator == null) {
            separator = DEFAULT_SEPARATOR;
        }

        if (columnsToExport == null) {
            columnsToExport = new Integer[model.getColumnCount()];
            for (int i = 0; i < columnsToExport.length; i++) {
                columnsToExport[i] = i;
            }
        }

        CsvWriter writer = new CsvWriter(out, separator, charset);

        //Write column headers:
        for (int column = 0; column < columnsToExport.length; column++) {
            writer.write(model.getColumnName(columnsToExport[column]), true);
        }
        writer.endRecord();

        //Write rows:
        Object value;
        String text;
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int column = 0; column < columnsToExport.length; column++) {
                value = model.getValueAt(table.convertRowIndexToModel(row), columnsToExport[column]);
                if (value != null) {
                    text = value.toString();
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
