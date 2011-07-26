/*
Copyright 2008-2011 Gephi
Authors : Taras Klaskovsky <megaterik@gmail.com>
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
package org.gephi.ui.exporter.plugin;

import org.gephi.io.exporter.plugin.ExporterDL;
import javax.swing.JPanel;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
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
        return NbBundle.getMessage(UIExporterDL.class, "UIExporterDL.name");
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