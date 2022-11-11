/*
Copyright 2008-2011 Gephi
Authors : Taras Klaskovsky <megaterik@gmail.com>
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
import org.gephi.io.exporter.plugin.ExporterVNA;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author megaterik
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterVNA implements ExporterUI {

    UIExporterVNAPanel panel;
    ExporterVNA exporter;
    ExporterVNASettings settings = new ExporterVNASettings();

    @Override
    public JPanel getPanel() {
        panel = new UIExporterVNAPanel();
        return panel;
    }

    @Override
    public void setup(Exporter exporter) {
        this.exporter = (ExporterVNA) exporter;
        settings.load(this.exporter);
        if (panel != null) {
            panel.setup((ExporterVNA) exporter);
        }
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
        return exporter instanceof ExporterVNA;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(UIExporterVNA.class, "UIExporterVNA.name");
    }

    private static class ExporterVNASettings extends AbstractExporterSettings {
        // Preference names
        private final static String EXPORT_EDGE_WEIGHT = "VNA_exportEdgeWeight";
        private final static String EXPORT_COORDINATES = "VNA_exportCoordinates";
        private final static String EXPORT_SIZE = "VNA_exportSize";
        private final static String EXPORT_SHORT_LABEL = "VNA_exportShortLabel";
        private final static String EXPORT_COLOR = "VNA_exportColor";
        private final static String NORMALIZE = "VNA_normalize";
        private final static String EXPORT_ATTRIBUTES = "VNA_exportAttributes";
        // Default
        private final static ExporterVNA DEFAULT = new ExporterVNA();

        private void load(ExporterVNA exporter) {
            exporter.setExportColor(get(EXPORT_COLOR, DEFAULT.isExportColor()));
            exporter.setExportCoords(get(EXPORT_COORDINATES, DEFAULT.isExportCoords()));
            exporter.setExportEdgeWeight(get(EXPORT_EDGE_WEIGHT, DEFAULT.isExportEdgeWeight()));
            exporter.setExportShortLabel(get(EXPORT_SHORT_LABEL, DEFAULT.isExportShortLabel()));
            exporter.setExportSize(get(EXPORT_SIZE, DEFAULT.isExportSize()));
            exporter.setExportAttributes(get(EXPORT_ATTRIBUTES, DEFAULT.isExportAttributes()));
            exporter.setNormalize(get(NORMALIZE, DEFAULT.isNormalize()));
        }

        private void save(ExporterVNA exporter) {
            put(EXPORT_COLOR, exporter.isExportColor());
            put(EXPORT_COORDINATES, exporter.isExportCoords());
            put(EXPORT_EDGE_WEIGHT, exporter.isExportEdgeWeight());
            put(EXPORT_SHORT_LABEL, exporter.isExportShortLabel());
            put(EXPORT_SIZE, exporter.isExportSize());
            put(EXPORT_ATTRIBUTES, exporter.isExportAttributes());
            put(NORMALIZE, exporter.isNormalize());
        }
    }
}
