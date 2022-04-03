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
import org.gephi.io.exporter.plugin.ExporterGML;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author megaterik
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterGML implements ExporterUI {

    ValidationPanel validationPanel;
    UIExporterGMLPanel panel;
    ExporterGML exporter;
    ExporterGMLSettings settings = new ExporterGMLSettings();

    @Override
    public JPanel getPanel() {
        panel = new UIExporterGMLPanel();
        validationPanel = UIExporterGMLPanel.createValidationPanel(panel);
        return validationPanel;
    }

    @Override
    public void setup(Exporter exporter) {
        this.exporter = (ExporterGML) exporter;
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
        return exporter instanceof ExporterGML;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterGEXF.class, "UIExporterGML.name");
    }

    private static class ExporterGMLSettings extends AbstractExporterSettings {

        // Preference names
        private final static String EXPORT_LABEL = "GML_exportLabel";
        private final static String EXPORT_COORDINATES = "GML_exportCoordinates";
        private final static String EXPORT_NODE_SIZE = "GML_exportNodeSize";
        private final static String EXPORT_EDGE_SIZE = "GML_exportEdgeSize";
        private final static String EXPORT_COLOR = "GML_exportColor";
        private final static String EXPORT_UNKNOWNS = "GML_exportNotRecognizedElements";
        private final static String NORMALIZE = "GML_normalize";
        private final static String SPACES = "GML_spaces";
        // Default
        private final static ExporterGML DEFAULT = new ExporterGML();

        private void load(ExporterGML exporter) {
            exporter.setExportColor(get(EXPORT_COLOR, DEFAULT.isExportColor()));
            exporter.setExportCoordinates(get(EXPORT_COORDINATES, DEFAULT.isExportCoordinates()));
            exporter.setExportEdgeSize(get(EXPORT_EDGE_SIZE, DEFAULT.isExportEdgeSize()));
            exporter.setExportLabel(get(EXPORT_LABEL, DEFAULT.isExportLabel()));
            exporter.setExportNodeSize(get(EXPORT_NODE_SIZE, DEFAULT.isExportNodeSize()));
            exporter.setExportNotRecognizedElements(get(EXPORT_UNKNOWNS,
                DEFAULT.isExportNotRecognizedElements()));
            exporter.setNormalize(get(NORMALIZE, DEFAULT.isNormalize()));
            exporter.setSpaces(get(SPACES, DEFAULT.getSpaces()));
        }

        private void save(ExporterGML exporter) {
            put(EXPORT_COLOR, exporter.isExportColor());
            put(EXPORT_COORDINATES, exporter.isExportCoordinates());
            put(EXPORT_EDGE_SIZE, exporter.isExportEdgeSize());
            put(EXPORT_LABEL, exporter.isExportLabel());
            put(EXPORT_NODE_SIZE, exporter.isExportNodeSize());
            put(EXPORT_UNKNOWNS, exporter.isExportNotRecognizedElements());
            put(NORMALIZE, exporter.isNormalize());
            put(SPACES, exporter.getSpaces());
        }
    }
}