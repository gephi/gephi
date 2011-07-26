package org.gephi.io.exporter.ucinet;

import javax.swing.JPanel;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ExporterUI.class)
public class UIExporterDL implements ExporterUI {

    private UIExporterDLPanel panel;
    private ExporterDL exporter;
    private ExporterDLSettings settings = new ExporterDLSettings();

    @Override
    public JPanel getPanel() {
        panel = new UIExporterDLPanel();
        return panel;
    }

    @Override
    public void setup(Exporter exporter) {
        this.exporter = (ExporterDL) exporter;
        settings.load(this.exporter);
        panel.setup(this.exporter);
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
          panel.unsetup(exporter);
          settings.save(exporter);
        }

        panel = null;
        exporter = null;
    }

    @Override
    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterDL;
    }

    @Override
    public String getDisplayName() {
        return "Exporter DL";
    }

private static class ExporterDLSettings
{
    private boolean useListFormat = true;
    private boolean useMatrixFormat = false;
    private boolean makeSymmetricMatrix = false;
   private void load(ExporterDL exporterDL)
   {
       exporterDL.setUseListFormat(useListFormat);
       exporterDL.setUseMatrixFormat(useMatrixFormat);
       exporterDL.setMakeSymmetricMatrix(makeSymmetricMatrix);
   }
   
   private void save(ExporterDL exporterDL)
   {
       useListFormat = exporterDL.isUseListFormat();
       useMatrixFormat = exporterDL.isUseMatrixFormat();
       makeSymmetricMatrix = exporterDL.isMakeSymmetricMatrix();
   }
}
}