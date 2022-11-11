package org.gephi.desktop.io.export;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.gephi.io.exporter.spi.VectorFileExporterBuilder;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(id = "org.gephi.desktop.io.export.ExportImage", category = "File")
@ActionRegistration(displayName = "#CTL_ExportImageAction", lazy = false)
public class ImageFileAction extends AbstractAction {

    private final AbstractExporterUI<VectorFileExporterBuilder> exporterUI;

    public ImageFileAction() {
        super(NbBundle.getMessage(ImageFileAction.class, "CTL_ExportImageAction"));

        exporterUI = new AbstractExporterUI<>("VectorialFileExporterUI", VectorFileExporterBuilder.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        exporterUI.action();
    }
}
