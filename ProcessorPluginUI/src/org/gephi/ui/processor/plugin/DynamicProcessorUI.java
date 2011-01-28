/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ui.processor.plugin;

import javax.swing.JPanel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.processor.plugin.DynamicProcessor;
import org.gephi.io.processor.spi.Processor;
import org.gephi.io.processor.spi.ProcessorUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ProcessorUI.class)
public class DynamicProcessorUI implements ProcessorUI {

    private DynamicProcessor dynamicProcessor;
    private DynamicProcessorSettings settings = new DynamicProcessorSettings();
    private DynamicProcessorPanel panel;

    @Override
    public void setup(Processor processor) {
        dynamicProcessor = (DynamicProcessor) processor;
        settings.load(dynamicProcessor);
        panel.setup(dynamicProcessor);
    }

    @Override
    public JPanel getPanel() {
        panel = new DynamicProcessorPanel();
        return DynamicProcessorPanel.createValidationPanel(panel);
    }

    @Override
    public void unsetup() {
        panel.unsetup(dynamicProcessor);
        settings.save(dynamicProcessor);
        panel = null;
        dynamicProcessor = null;
    }

    @Override
    public boolean isUIFoProcessor(Processor processor) {
        return processor instanceof DynamicProcessor;
    }

    @Override
    public boolean isValid(Container container) {
        return !container.isDynamicGraph();
    }

    private static class DynamicProcessorSettings {

        private boolean dateMode = true;
        private String date = "";
        private boolean labelMatching = true;

        private void save(DynamicProcessor dynamicProcessor) {
            this.dateMode = dynamicProcessor.isDateMode();
            this.date = dynamicProcessor.getDate();
            this.labelMatching = dynamicProcessor.isLabelmatching();
        }

        private void load(DynamicProcessor dynamicProcessor) {
            dynamicProcessor.setDateMode(dateMode);
            dynamicProcessor.setDate(date);
            dynamicProcessor.setLabelmatching(labelMatching);
        }
    }
}
