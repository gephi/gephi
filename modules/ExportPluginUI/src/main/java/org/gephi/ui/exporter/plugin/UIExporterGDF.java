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
package org.gephi.ui.exporter.plugin;

import javax.swing.JPanel;
import org.gephi.io.exporter.plugin.ExporterGDF;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterGDF implements ExporterUI {

    private UIExporterGDFPanel panel;
    private ExporterGDF exporterGDF;
    private ExporterGDFSettings settings = new ExporterGDFSettings();

    @Override
    public void setup(Exporter exporter) {
        exporterGDF = (ExporterGDF) exporter;
        settings.load(exporterGDF);
        panel.setup(exporterGDF);
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterGDF);
            settings.save(exporterGDF);
        }
        panel = null;
        exporterGDF = null;
    }

    @Override
    public JPanel getPanel() {
        panel = new UIExporterGDFPanel();
        return panel;
    }

    @Override
    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterGDF;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterGDF.class, "UIExporterGDF.name");
    }

    private static class ExporterGDFSettings {

        private boolean normalize = false;
        private boolean simpleQuotes = false;
        private boolean useQuotes = true;
        private boolean exportColors = true;
        private boolean exportPosition = true;
        private boolean exportAttributes = true;
        private boolean exportVisibility = false;

        private void save(ExporterGDF exporterGDF) {
            this.normalize = exporterGDF.isNormalize();
            this.simpleQuotes = exporterGDF.isSimpleQuotes();
            this.useQuotes = exporterGDF.isUseQuotes();
            this.exportColors = exporterGDF.isExportColors();
            this.exportPosition = exporterGDF.isExportPosition();
            this.exportAttributes = exporterGDF.isExportAttributes();
            this.exportVisibility = exporterGDF.isExportVisibility();
        }

        private void load(ExporterGDF exporterGDF) {
            exporterGDF.setNormalize(normalize);
            exporterGDF.setSimpleQuotes(simpleQuotes);
            exporterGDF.setUseQuotes(useQuotes);
            exporterGDF.setExportColors(exportColors);
            exporterGDF.setExportAttributes(exportAttributes);
            exporterGDF.setExportPosition(exportPosition);
            exporterGDF.setExportVisibility(exportVisibility);
        }
    }
}
