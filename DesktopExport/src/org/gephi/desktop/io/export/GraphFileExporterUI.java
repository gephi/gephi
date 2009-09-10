/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.desktop.io.export;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.gephi.desktop.io.export.api.ExporterClassUI;
import org.gephi.io.exporter.ExportController;
import org.gephi.io.exporter.FileType;
import org.gephi.io.exporter.GraphFileExporter;
import org.gephi.ui.exporter.ExporterUI;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphFileExporterUI implements ExporterClassUI {

    private GraphFileExporter selectedExporter;

    public String getName() {
        return "Graph File...";
    }

    public boolean isEnable() {
        return true;
    }

    public void action() {
        final String LAST_PATH = "GraphFileExporterUI_Last_Path";
        final String LAST_PATH_DEFAULT = "GraphFileExporterUI_Last_Path_Default";

        final ExportController exportController = Lookup.getDefault().lookup(ExportController.class);
        if (exportController == null) {
            return;
        }

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(GraphFileExporterUI.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(GraphFileExporterUI.class).get(LAST_PATH, lastPathDefault);

        //Options panel
        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        final JPanel optionsPanel = new JPanel(layout);
        final JButton optionsButton = new JButton(NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_optionsButton_name"));
        optionsPanel.add(optionsButton);
        optionsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ExporterUI exporterUI = exportController.getUI(selectedExporter);
                if (exporterUI != null) {
                    exporterUI.setup(selectedExporter);
                    DialogDescriptor dd = new DialogDescriptor(exporterUI.getPanel(), NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_optionsDialog_title", selectedExporter.getName()));
                    Object result = DialogDisplayer.getDefault().notify(dd);
                    if (result != NotifyDescriptor.OK_OPTION) {
                        return;
                    }
                    exporterUI.unsetup();
                }
            }
        });

        //Optionable file chooser
        final JFileChooser chooser = new JFileChooser(lastPath) {

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                Component c = dialog.getContentPane().getComponent(0);
                if (c != null && c instanceof JComponent) {
                    Insets insets = ((JComponent) c).getInsets();
                    optionsPanel.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
                }
                dialog.getContentPane().add(optionsPanel, BorderLayout.SOUTH);

                return dialog;
            }
        };
        chooser.setDialogTitle(NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_filechooser_title"));
        chooser.addPropertyChangeListener("fileFilterChanged", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                DialogFileFilter fileFilter = (DialogFileFilter) evt.getNewValue();
                selectedExporter = getExporter(fileFilter);
                if (selectedExporter != null && exportController.hasUI(selectedExporter)) {
                    optionsButton.setEnabled(true);
                } else {
                    optionsButton.setEnabled(false);
                }
            }
        });

        //File filters
        for (GraphFileExporter graphFileExporter : exportController.getGraphFileExporters()) {
            for (FileType fileType : graphFileExporter.getFileTypes()) {
                DialogFileFilter dialogFileFilter = new DialogFileFilter(fileType.getName());
                dialogFileFilter.addExtensions(fileType.getExtensions());
                chooser.addChoosableFileFilter(dialogFileFilter);
            }
        }
        chooser.setAcceptAllFileFilterUsed(false);

        //Show
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            file = FileUtil.normalizeFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);

            //Save last path
            NbPreferences.forModule(GraphFileExporterUI.class).put(LAST_PATH, file.getAbsolutePath());

            //Do
            exportController.doExport(selectedExporter, fileObject);
        }
    }

    private GraphFileExporter getExporter(DialogFileFilter fileFilter) {
        final ExportController exportController = Lookup.getDefault().lookup(ExportController.class);
        //Find fileFilter
        for (GraphFileExporter graphFileExporter : exportController.getGraphFileExporters()) {
            for (FileType fileType : graphFileExporter.getFileTypes()) {
                DialogFileFilter tempFilter = new DialogFileFilter(fileType.getName());
                tempFilter.addExtensions(fileType.getExtensions());
                if (tempFilter.equals(fileFilter)) {
                    return graphFileExporter;
                }
            }
        }
        return null;
    }
}
