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
package org.gephi.ui.exporter.preview;

import javax.swing.JPanel;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Taras Klaskovsky
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterPNG implements ExporterUI {

    private UIExporterPNGPanel panel;
    private PNGExporter exporter;
    private ExporterPNGSettings settings = new ExporterPNGSettings();
    private ValidationPanel validationPanel;

    @Override
    public JPanel getPanel() {
        panel = new UIExporterPNGPanel();
        validationPanel = UIExporterPNGPanel.createValidationPanel(panel);
        return validationPanel;
    }

    @Override
    public void setup(Exporter exporter) {
        this.exporter = (PNGExporter) exporter;
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
        return exporter instanceof PNGExporter;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterPDF.class, "UIExporterPNG.name");
    }

    private static class ExporterPNGSettings {

        private int width = 1024;
        private int height = 1024;
        private int margin = 4;
        private boolean transparentBackground;

        void load(PNGExporter exporter) {
            exporter.setHeight(height);
            exporter.setWidth(width);
            exporter.setMargin(margin);
            exporter.setTransparentBackground(transparentBackground);
        }

        void save(PNGExporter exporter) {
            height = exporter.getHeight();
            width = exporter.getWidth();
            margin = exporter.getMargin();
            transparentBackground = exporter.isTransparentBackground();
        }
    }
}
