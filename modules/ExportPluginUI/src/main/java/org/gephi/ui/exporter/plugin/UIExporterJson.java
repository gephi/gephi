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
import org.gephi.io.exporter.plugin.ExporterJson;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterJson implements ExporterUI {

    private final ExporterJsonSettings settings = new ExporterJsonSettings();
    private UIExporterJsonPanel panel;
    private ExporterJson exporterJson;

    @Override
    public void setup(Exporter exporter) {
        exporterJson = (ExporterJson) exporter;
        settings.load(exporterJson);
        if (panel != null) {
            panel.setup(exporterJson);
        }
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterJson);
            settings.save(exporterJson);
        }
        panel = null;
        exporterJson = null;
    }

    @Override
    public JPanel getPanel() {
        panel = new UIExporterJsonPanel();
        return panel;
    }

    @Override
    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterJson;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterJson.class, "UIExporterJson.name");
    }

    private static class ExporterJsonSettings extends AbstractExporterSettings {

        // Preference names
        private final static String NORMALIZE = "Json_normalize";
        private final static String EXPORT_COLORS = "Json_exportColors";
        private final static String EXPORT_POSITION = "Json_exportPosition";
        private final static String EXPORT_ATTRIBUTES = "Json_exportAttributes";
        private final static String EXPORT_SIZE = "Json_exportSize";
        private final static String EXPORT_DYNAMICS = "Json_exportDynamics";
        private final static String EXPORT_META = "Json_exportMeta";
        private final static String PRETTY_PRINT = "Json_prettyPrint";
        private final static String FORMAT = "Json_format";
        // Default
        private final static ExporterJson DEFAULT = new ExporterJson();

        private void save(ExporterJson exporterJson) {
            put(NORMALIZE, exporterJson.isNormalize());
            put(EXPORT_COLORS, exporterJson.isExportColors());
            put(EXPORT_POSITION, exporterJson.isExportPosition());
            put(EXPORT_SIZE, exporterJson.isExportSize());
            put(EXPORT_ATTRIBUTES, exporterJson.isExportAttributes());
            put(EXPORT_DYNAMICS, exporterJson.isExportDynamic());
            put(EXPORT_META, exporterJson.isExportMeta());
            put(FORMAT, exporterJson.getFormat().name());
            put(PRETTY_PRINT, exporterJson.isPrettyPrint());
        }

        private void load(ExporterJson exporterJson) {
            exporterJson.setNormalize(get(NORMALIZE, DEFAULT.isNormalize()));
            exporterJson.setExportColors(get(EXPORT_COLORS, DEFAULT.isExportColors()));
            exporterJson.setExportAttributes(get(EXPORT_ATTRIBUTES, DEFAULT.isExportAttributes()));
            exporterJson.setExportPosition(get(EXPORT_POSITION, DEFAULT.isExportPosition()));
            exporterJson.setExportSize(get(EXPORT_SIZE, DEFAULT.isExportSize()));
            exporterJson.setExportDynamic(get(EXPORT_DYNAMICS, DEFAULT.isExportDynamic()));
            exporterJson.setExportMeta(get(EXPORT_META, DEFAULT.isExportMeta()));
            exporterJson.setFormat(ExporterJson.Format.valueOf(get(FORMAT, DEFAULT.getFormat().name())));
            exporterJson.setPrettyPrint(get(PRETTY_PRINT, DEFAULT.isPrettyPrint()));
        }
    }
}
