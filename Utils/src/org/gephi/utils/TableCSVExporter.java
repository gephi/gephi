/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.utils;

import com.csvreader.CsvWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class TableCSVExporter {

    private static final Character DEFAULT_SEPARATOR = ',';

    /**
     * Export a JTable to the specified file.
     * @param table Table to export
     * @param file File to write
     * @param separator Separator to use for separating values of a row in the CSV file. If null ',' will be used.
     * @param columnsToExport Indicates the indexes of the columns to export. All columns will be exported if null
     * @throws IOException When an error happens while writing the file
     */
    public static void writeCSVFile(JTable table, File file, Character separator, Integer[] columnsToExport) throws IOException {
        TableModel model = table.getModel();
        FileWriter out = new FileWriter(file);
        if (separator == null) {
            separator = DEFAULT_SEPARATOR;
        }

        if(columnsToExport==null){
            columnsToExport=new Integer[model.getColumnCount()];
            for (int i = 0; i < columnsToExport.length; i++) {
                columnsToExport[i]=i;
            }
        }

        CsvWriter writer = new CsvWriter(out, separator);

        //Write column headers:
        for (int column = 0; column < columnsToExport.length; column++) {
            writer.write(model.getColumnName(columnsToExport[column]), true);
        }
        writer.endRecord();

        //Write rows:
        Object value;
        String text;
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int column = 0; column < columnsToExport.length; column++) {
                value = model.getValueAt(row, columnsToExport[column]);
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

    /**
     * Exports a JTable to a CSV file showing first a dialog to select the file to write.
     * @param parent Parent window
     * @param table Table to export
     * @param separator Separator to use for separating values of a row in the CSV file. If null ',' will be used.
     * @param columnsToExport Indicates the indexes of the columns to export. All columns will be exported if null
     */
    public static void exportTableAsCSV(JComponent parent, JTable table, Character separator, Integer[] columnsToExport) {
        String lastPath = NbPreferences.forModule(TableCSVExporter.class).get(LAST_PATH, null);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setAcceptAllFileFilterUsed(false);
        DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(TableCSVExporter.class, "TableCSVExporter.filechooser.csvDescription"));
        dialogFileFilter.addExtension("csv");
        chooser.addChoosableFileFilter(dialogFileFilter);
        File selectedFile = new File(chooser.getCurrentDirectory(), "table.csv");
        chooser.setSelectedFile(selectedFile);
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();

        if (!file.getPath().endsWith(".csv")) {
            file = new File(file.getPath() + ".csv");
        }

        //Save last path
        String defaultDirectory = file.getParentFile().getAbsolutePath();
        NbPreferences.forModule(TableCSVExporter.class).put(LAST_PATH, defaultDirectory);
        try {
            writeCSVFile(table, file, separator, columnsToExport);
            JOptionPane.showMessageDialog(parent, NbBundle.getMessage(TableCSVExporter.class, "TableCSVExporter.dialog.success"));
            java.awt.Desktop.getDesktop().open(file);//Try to open the created file
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, NbBundle.getMessage(TableCSVExporter.class, "TableCSVExporter.dialog.error"), NbBundle.getMessage(TableCSVExporter.class, "TableCSVExporter.dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }
    private static final String LAST_PATH = "TableCSVExporter_Save_Last_Path";
}
