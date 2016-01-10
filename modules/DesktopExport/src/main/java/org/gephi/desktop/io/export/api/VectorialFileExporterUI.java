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
import org.gephi.desktop.io.export.spi.ExporterClassUI;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.ExporterUI;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.io.exporter.spi.VectorFileExporterBuilder;
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
public final class VectorialFileExporterUI implements ExporterClassUI {

    private VectorFileExporterBuilder selectedBuilder;
    private VectorExporter selectedExporter;
    private File selectedFile;
    private JDialog dialog;

    @Override
    public String getName() {
        return NbBundle.getMessage(VectorialFileExporterUI.class, "VectorialFileExporterUI_title");
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void action() {
        final String LAST_PATH = "VectorialFileExporterUI_Last_Path";
        final String LAST_PATH_DEFAULT = "VectorialFileExporterUI_Last_Path_Default";

        final ExportControllerUI exportController = Lookup.getDefault().lookup(ExportControllerUI.class);
        if (exportController == null) {
            return;
        }

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(VectorialFileExporterUI.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(VectorialFileExporterUI.class).get(LAST_PATH, lastPathDefault);

        //Options panel
        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        JPanel optionsPanel = new JPanel(layout);
        final JButton optionsButton = new JButton(NbBundle.getMessage(VectorialFileExporterUI.class, "VectorialFileExporterUI_optionsButton_name"));
        optionsPanel.add(optionsButton);
        optionsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ExporterUI exporterUI = exportController.getExportController().getUI(selectedExporter);
                if (exporterUI != null) {
                    JPanel panel = exporterUI.getPanel();
                    exporterUI.setup(selectedExporter);
                    DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(VectorialFileExporterUI.class, "VectorialFileExporterUI_optionsDialog_title", selectedBuilder.getName()));
                    TopDialog topDialog = new TopDialog(dialog, dd.getTitle(), dd.isModal(), dd, dd.getClosingOptions(), dd.getButtonListener());
                    topDialog.setVisible(true);
                    Object result = (dd.getValue() != null) ? dd.getValue() : NotifyDescriptor.CLOSED_OPTION;
//                    Object result = DialogDisplayer.getDefault().notify(dd);
                    exporterUI.unsetup(result == NotifyDescriptor.OK_OPTION);
                }
            }
        });

        //Graph Settings Panel
        final JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(optionsPanel, BorderLayout.NORTH);

        //Optionable file chooser
        final JFileChooser chooser = new JFileChooser(lastPath) {

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                dialog = super.createDialog(parent);
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
        chooser.setDialogTitle(NbBundle.getMessage(VectorialFileExporterUI.class, "VectorialFileExporterUI_filechooser_title"));
        chooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DialogFileFilter fileFilter = (DialogFileFilter) evt.getNewValue();

                //Options panel enabling
                selectedBuilder = getExporter(fileFilter);
                if (selectedBuilder != null) {
                    selectedExporter = selectedBuilder.buildExporter();
                }
                if (selectedExporter != null && exportController.getExportController().getUI(selectedExporter) != null) {
                    optionsButton.setEnabled(true);
                } else {
                    optionsButton.setEnabled(false);
                }

                //Selected file extension change
                if (selectedFile != null && fileFilter != null) {
                    String fileName = selectedFile.getName();
                    String directoryPath = chooser.getCurrentDirectory().getAbsolutePath();
                    if (fileName.lastIndexOf(".") != -1) {
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                        fileName = fileName.concat(fileFilter.getExtensions().get(0));
                        selectedFile = new File(directoryPath, fileName);
                        chooser.setSelectedFile(selectedFile);
                    }
                }
            }
        });
        chooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() != null) {
                    selectedFile = (File) evt.getNewValue();
                }
            }
        });

        //File filters
        DialogFileFilter defaultFilter = null;
        for (VectorFileExporterBuilder vectorFileExporter : Lookup.getDefault().lookupAll(VectorFileExporterBuilder.class)) {
            for (FileType fileType : vectorFileExporter.getFileTypes()) {
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
            NbPreferences.forModule(VectorialFileExporterUI.class).put(LAST_PATH, file.getAbsolutePath());

            //Do
            exportController.exportFile(fileObject, selectedExporter);
        }
        dialog = null;
    }

    private boolean canExport(JFileChooser chooser) {
        File file = chooser.getSelectedFile();
        String defaultExtention = selectedBuilder.getFileTypes()[0].getExtension();

        try {
            if (!file.getPath().endsWith(defaultExtention)) {
                file = new File(file.getPath() + defaultExtention);
                chooser.setSelectedFile(file);
            }
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    String failMsg = NbBundle.getMessage(VectorialFileExporterUI.class, "VectorialFileExporterUI_SaveFailed", new Object[]{file.getPath()});
                    JOptionPane.showMessageDialog(null, failMsg);
                    return false;
                }
            } else {
                String overwriteMsg = NbBundle.getMessage(VectorialFileExporterUI.class, "VectorialFileExporterUI_overwriteDialog_message", new Object[]{file.getPath()});
                if (JOptionPane.showConfirmDialog(null, overwriteMsg, NbBundle.getMessage(VectorialFileExporterUI.class, "VectorialFileExporterUI_overwriteDialog_title"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
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

    private VectorFileExporterBuilder getExporter(DialogFileFilter fileFilter) {
        //Find fileFilter
        for (VectorFileExporterBuilder graphFileExporter : Lookup.getDefault().lookupAll(VectorFileExporterBuilder.class)) {
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
