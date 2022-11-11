package org.gephi.desktop.io.export;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.gephi.io.exporter.spi.FileExporterBuilder;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class AbstractExporterUI<T extends FileExporterBuilder> {

    private FileExporterBuilder selectedBuilder;
    private Exporter selectedExporter;
    private File selectedFile;
    private boolean visibleOnlyGraph = false;
    private boolean exportAll = false;
    private JDialog dialog;

    private final Class<T> builderClass;
    private final String preferencesPrefix;

    public AbstractExporterUI(String preferencesPrefix, Class<T> builderClass) {
        this.builderClass = builderClass;
        this.preferencesPrefix = preferencesPrefix;
    }

    public void action() {
        action(Lookup.getDefault().lookupAll(builderClass));
    }

    public void action(final Collection<? extends T> exporterBuilders) {
        final String LAST_PATH = preferencesPrefix + "_Last_Path";
        final String LAST_PATH_DEFAULT = preferencesPrefix + "_Last_Path_Default";
        final String LAST_FILE_FILTER = preferencesPrefix + "_Last_File_Filter";

        final DesktopExportController exportController = Lookup.getDefault().lookup(DesktopExportController.class);
        if (exportController == null) {
            return;
        }

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(AbstractExporterUI.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(AbstractExporterUI.class).get(LAST_PATH, lastPathDefault);
        String lastFileFilterString = NbPreferences.forModule(AbstractExporterUI.class).get(LAST_FILE_FILTER, null);

        //Get last directory as a file
        File lastPathDir = null;
        if (lastPath != null) {
            lastPathDir = new File(lastPath);
            while (lastPathDir != null && (lastPathDir.isFile() || !lastPathDir.exists())) {
                lastPathDir = lastPathDir.getParentFile();
            }
        }

        //Options button (that shows the dialog)
        final JButton optionsButton =
            new JButton(NbBundle.getMessage(AbstractExporterUI.class, "AbstractExporterUI.optionsButton.name"));
        optionsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ExporterUI exporterUI = exportController.getExportController().getUI(selectedExporter);
                if (exporterUI != null) {
                    JPanel panel = exporterUI.getPanel();
                    exporterUI.setup(selectedExporter);

                    DialogDescriptor dd = new DialogDescriptor(panel, NbBundle
                        .getMessage(AbstractExporterUI.class, "AbstractExporterUI.optionsDialog.title",
                            selectedBuilder.getName()));
                    TopDialog topDialog = new TopDialog(dialog, dd.getTitle(), dd.isModal(), dd, dd.getClosingOptions(),
                        dd.getButtonListener());
                    topDialog.setVisible(true);
                    Object result = (dd.getValue() != null) ? dd.getValue() : NotifyDescriptor.CLOSED_OPTION;
//                    Object result = DialogDisplayer.getDefault().notify(dd);
                    exporterUI.unsetup(result == NotifyDescriptor.OK_OPTION);
                }
            }
        });

        // Settings panel
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.add(optionsButton, BorderLayout.EAST);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        //Graph Settings Panel
        final JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(optionsPanel, BorderLayout.NORTH);

        GraphFileExporterUIPanel graphSettings = new GraphFileExporterUIPanel();
        graphSettings.setVisibleOnlyGraph(visibleOnlyGraph);
        if (GraphFileExporterBuilder.class.isAssignableFrom(builderClass)) {
            // Only needed for the graph exporters
            southPanel.add(graphSettings, BorderLayout.CENTER);
        }

        //Optionable file chooser
        final JFileChooser chooser = new JFileChooser(lastPathDir) {

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                dialog = super.createDialog(parent);
                dialog.setSize(640, 480);
                dialog.setResizable(true);
                Component c = dialog.getContentPane().getComponent(0);
                if (c != null && c instanceof JComponent) {
                    Insets insets = ((JComponent) c).getInsets();
                    southPanel.setBorder(
                        BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
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
        chooser.setFileSelectionMode(exportAll ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(NbBundle.getMessage(AbstractExporterUI.class, "AbstractExporterUI.filechooser.title"));
        chooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DialogFileFilter fileFilter = (DialogFileFilter) evt.getNewValue();

                //Save last file filter
                NbPreferences.forModule(AbstractExporterUI.class)
                    .put(LAST_FILE_FILTER, fileFilter.getExtensions().toString());

                //Options panel enabling
                selectedBuilder = getExporter(exporterBuilders, fileFilter);
                if (selectedBuilder != null) {
                    selectedExporter = selectedBuilder.buildExporter();

                    Workspace workspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
                    selectedExporter.setWorkspace(workspace);

                    ExporterUI ui = exportController.getExportController().getUI(selectedExporter);
                    if (ui != null) {
                        // Load saved values into exporter
                        ui.setup(selectedExporter);
                        optionsButton.setEnabled(true);
                    } else {
                        optionsButton.setEnabled(false);
                    }
                } else {
                    optionsButton.setEnabled(false);
                }

                //Selected file extension change
                if (selectedFile != null && !exportAll) {
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

        //Export all checkbox
        JCheckBox exportAllCheckBox =
            new JCheckBox(NbBundle.getMessage(AbstractExporterUI.class, "AbstractExporterUI.exportAllCheckBox.text"));
        exportAllCheckBox.setSelected(exportAll);
        exportAllCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportAll = exportAllCheckBox.isSelected();
                chooser.setFileSelectionMode(exportAll ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
            }
        });
        optionsPanel.add(exportAllCheckBox, BorderLayout.WEST);

        //File filters
        DialogFileFilter defaultFileFilter = null;
        DialogFileFilter lastFileFilter = null;

        for (FileExporterBuilder graphFileExporter : exporterBuilders) {
            for (FileType fileType : graphFileExporter.getFileTypes()) {
                DialogFileFilter dialogFileFilter = new DialogFileFilter(fileType.getName());
                dialogFileFilter.addExtensions(fileType.getExtensions());
                if (defaultFileFilter == null) {
                    defaultFileFilter = dialogFileFilter;
                }

                if (lastFileFilterString != null) {
                    if (dialogFileFilter.getExtensions().toString().equals(lastFileFilterString)) {
                        lastFileFilter = dialogFileFilter;
                    }
                }

                chooser.addChoosableFileFilter(dialogFileFilter);
            }
        }

        chooser.setAcceptAllFileFilterUsed(false);

        if (lastFileFilter != null) {
            defaultFileFilter = lastFileFilter;
        }

        chooser.setFileFilter(defaultFileFilter);

        if (exportAll) {
            selectedFile = chooser.getCurrentDirectory();
        } else if (lastPathDir != null && lastPathDir.exists() && lastPathDir.isDirectory()) {
            selectedFile = new File(lastPath);
        } else {
            selectedFile = new File(chooser.getCurrentDirectory(),
                NbBundle.getMessage(AbstractExporterUI.class, "AbstractExporterUI.untitledFileName") +
                    defaultFileFilter.getExtensions().get(0));
        }
        chooser.setSelectedFile(selectedFile);

        //Show
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            file = FileUtil.normalizeFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);

            //Save last path
            NbPreferences.forModule(AbstractExporterUI.class).put(LAST_PATH, file.getAbsolutePath());

            //Save variable
            visibleOnlyGraph = graphSettings.isVisibleOnlyGraph();

            //Do
            if (selectedExporter instanceof GraphExporter) {
                ((GraphExporter) selectedExporter).setExportVisible(visibleOnlyGraph);
            }

            if (exportAll) {
                String extension = selectedBuilder.getFileTypes()[0].getExtension();
                exportController.exportFiles(fileObject, selectedExporter, extension);
            } else {
                exportController.exportFile(fileObject, selectedExporter);
            }
        }
        dialog = null;
    }

    private boolean canExport(JFileChooser chooser) {
        if (exportAll) {
            // TODO: Warning if directory is not empty
            return true;
        }
        File file = chooser.getSelectedFile();
        String defaultExtension = selectedBuilder.getFileTypes()[0].getExtension();

        try {
            if (!file.getPath().endsWith(defaultExtension)) {
                file = new File(file.getPath() + defaultExtension);
                selectedFile = file;
                chooser.setSelectedFile(file);
            }
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    String failMsg = NbBundle.getMessage(AbstractExporterUI.class, "AbstractExporterUI.SaveFailed",
                        new Object[] {file.getPath()});
                    JOptionPane.showMessageDialog(chooser, failMsg);
                    return false;
                }
            } else {
                String overwriteMsg = NbBundle
                    .getMessage(AbstractExporterUI.class, "AbstractExporterUI.overwriteDialog.message",
                        new Object[] {file.getPath()});
                if (JOptionPane.showConfirmDialog(chooser, overwriteMsg,
                    NbBundle.getMessage(AbstractExporterUI.class, "AbstractExporterUI.overwriteDialog.title"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
        } catch (IOException ex) {
            NotifyDescriptor.Message msg =
                new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);
            return false;
        }

        return true;
    }

    private FileExporterBuilder getExporter(Collection<? extends T> exporterBuilders,
                                            DialogFileFilter fileFilter) {
        //Find fileFilter
        for (FileExporterBuilder graphFileExporter : exporterBuilders) {
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
