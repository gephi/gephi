package org.gephi.desktop.io.export;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.AbstractAction;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(id = "org.gephi.desktop.io.export.ExportGraph", category = "File")
@ActionRegistration(displayName = "#CTL_ExportGraphAction", lazy = false)
public class GraphFileAction extends AbstractAction {

    private final AbstractExporterUI<GraphFileExporterBuilder> exporterUI;

    public GraphFileAction() {
        super(NbBundle.getMessage(GraphFileAction.class, "CTL_ExportGraphAction"));

        exporterUI = new AbstractExporterUI<>("GraphFileExporterUI", GraphFileExporterBuilder.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof GraphFileExporterBuilder[]) {
            exporterUI.action(Arrays.asList((GraphFileExporterBuilder[]) e.getSource()));
        } else {
            exporterUI.action();
        }
    }
}
