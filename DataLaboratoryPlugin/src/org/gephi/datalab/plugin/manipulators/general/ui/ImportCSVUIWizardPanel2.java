/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.general.ui;

import java.awt.Component;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.datalab.plugin.manipulators.general.ui.ImportCSVUIWizardAction.Mode;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ImportCSVUIWizardPanel2 implements WizardDescriptor.Panel {

    private WizardDescriptor wizardDescriptor;
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ImportCSVUIVisualPanel2 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new ImportCSVUIVisualPanel2(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    public boolean isValid() {
        return component.isValidCSV();
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        component.setSeparator((Character) wizardDescriptor.getProperty("separator"));
        component.setFile((File) wizardDescriptor.getProperty("file"));
        component.setMode((Mode) wizardDescriptor.getProperty("mode"));
        component.setCharset((Charset) wizardDescriptor.getProperty("charset"));
        component.reloadSettings();
    }

    public void unSetup() {
        component.unSetup();
    }

    public void storeSettings(Object settings) {
        wizardDescriptor.putProperty("columns-names", component.getColumnsToImport());
        wizardDescriptor.putProperty("columns-types", component.getColumnsToImportTypes());
        wizardDescriptor.putProperty("assign-new-node-ids", component.getAssignNewNodeIds());
        wizardDescriptor.putProperty("create-new-nodes", component.getCreateNewNodes());
    }

    public void setWizardDescriptor(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }
}
