/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.desktop.io.export.api;

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
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.gephi.desktop.io.export.ExportControllerUI;
import org.gephi.desktop.io.export.GraphFileExporterUIPanel;
import org.gephi.desktop.io.export.spi.ExporterClassUI;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.ExporterUI;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterClassUI.class)
public final class GraphFileExporterUI implements ExporterClassUI {

    private GraphFileExporterBuilder selectedBuilder;
    private GraphExporter selectedExporter;
    private File selectedFile;
    private boolean visibleOnlyGraph = false;

    public String getName() {
        return NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_title");
    }

    public boolean isEnable() {
        return true;
    }

    public void action() {
        final String LAST_PATH = "GraphFileExporterUI_Last_Path";
        final String LAST_PATH_DEFAULT = "GraphFileExporterUI_Last_Path_Default";

        final ExportControllerUI exportController = Lookup.getDefault().lookup(ExportControllerUI.class);
        if (exportController == null) {
            return;
        }

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(GraphFileExporterUI.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(GraphFileExporterUI.class).get(LAST_PATH, lastPathDefault);

        //Options panel
        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        JPanel optionsPanel = new JPanel(layout);
        final JButton optionsButton = new JButton(NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_optionsButton_name"));
        optionsPanel.add(optionsButton);
        optionsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ExporterUI exporterUI = exportController.getExportController().getUI(selectedExporter);
                if (exporterUI != null) {
                    JPanel panel = exporterUI.getPanel();
                    exporterUI.setup(selectedExporter);
                    DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_optionsDialog_title", selectedBuilder.getName()));
                    Object result = DialogDisplayer.getDefault().notify(dd);
                    exporterUI.unsetup(result == NotifyDescriptor.OK_OPTION);
                }
            }
        });

        //Graph Settings Panel
        final JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(optionsPanel, BorderLayout.NORTH);
        GraphFileExporterUIPanel graphSettings = new GraphFileExporterUIPanel();
        graphSettings.setVisibleOnlyGraph(visibleOnlyGraph);
        southPanel.add(graphSettings, BorderLayout.CENTER);

        //Optionable file chooser
        final JFileChooser chooser = new JFileChooser(lastPath) {

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                Component c = dialog.getContentPane().getComponent(0);
                if (c != null && c instanceof JComponent) {
                    Insets insets = ((JComponent) c).getInsets();
                    southPanel.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
                }
                dialog.getContentPane().add(southPanel, BorderLayout.SOUTH);

                return dialog;
            }

            @Override
            public void approveSelection() {
                if (canExport(this)) {
                    super.approveSelection();
                }
            }
        };
        chooser.setDialogTitle(NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_filechooser_title"));
        chooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                DialogFileFilter fileFilter = (DialogFileFilter) evt.getNewValue();

                //Options panel enabling
                selectedBuilder = getExporter(fileFilter);
                if (selectedBuilder != null) {
                    selectedExporter = selectedBuilder.buildExporter();
                }
                if (selectedBuilder != null && exportController.getExportController().getUI(selectedExporter) != null) {
                    optionsButton.setEnabled(true);
                } else {
                    optionsButton.setEnabled(false);
                }

                //Selected file extension change
                if (selectedFile != null && fileFilter != null) {
                    String fileName = selectedFile.getName();
                    String directoryPath = chooser.getCurrentDirectory().getAbsolutePath();
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    fileName = fileName.concat(fileFilter.getExtensions().get(0));
                    selectedFile = new File(directoryPath, fileName);
                    chooser.setSelectedFile(selectedFile);
                }
            }
        });
        chooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() != null) {
                    selectedFile = (File) evt.getNewValue();
                }
            }
        });

        //File filters
        DialogFileFilter defaultFilter = null;
        for (GraphFileExporterBuilder graphFileExporter : Lookup.getDefault().lookupAll(GraphFileExporterBuilder.class)) {
            for (FileType fileType : graphFileExporter.getFileTypes()) {
                DialogFileFilter dialogFileFilter = new DialogFileFilter(fileType.getName());
                dialogFileFilter.addExtensions(fileType.getExtensions());
                if (defaultFilter == null) {
                    defaultFilter = dialogFileFilter;
                }
                chooser.addChoosableFileFilter(dialogFileFilter);
            }
        }
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(defaultFilter);
        selectedFile = new File(chooser.getCurrentDirectory(), "Untitled" + defaultFilter.getExtensions().get(0));
        chooser.setSelectedFile(selectedFile);

        //Show
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            file = FileUtil.normalizeFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);

            //Save last path
            NbPreferences.forModule(GraphFileExporterUI.class).put(LAST_PATH, file.getAbsolutePath());

            //Save variable
            visibleOnlyGraph = graphSettings.isVisibleOnlyGraph();

            //Do
            selectedExporter.setExportVisible(visibleOnlyGraph);
            exportController.exportFile(fileObject, selectedExporter);
        }
    }

    private boolean canExport(JFileChooser chooser) {
        File file = chooser.getSelectedFile();
        String defaultExtention = selectedBuilder.getFileTypes()[0].getExtension();

        try {
            if (!file.getPath().endsWith(defaultExtention)) {
                file = new File(file.getPath() + defaultExtention);
                selectedFile = file;
                chooser.setSelectedFile(file);
            }
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    String failMsg = NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_SaveFailed", new Object[]{file.getPath()});
                    JOptionPane.showMessageDialog(null, failMsg);
                    return false;
                }
            } else {
                String overwriteMsg = NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_overwriteDialog_message", new Object[]{file.getPath()});
                if (JOptionPane.showConfirmDialog(null, overwriteMsg, NbBundle.getMessage(GraphFileExporterUI.class, "GraphFileExporterUI_overwriteDialog_title"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
        } catch (IOException ex) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);
            return false;
        }

        return true;
    }

    private GraphFileExporterBuilder getExporter(DialogFileFilter fileFilter) {
        //Find fileFilter
        for (GraphFileExporterBuilder graphFileExporter : Lookup.getDefault().lookupAll(GraphFileExporterBuilder.class)) {
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
