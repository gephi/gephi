/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.generate;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.io.generator.api.GeneratorController;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.Report;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.project.api.Workspace;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = GeneratorController.class)
public class DesktopGeneratorController implements GeneratorController {

    private LongTaskExecutor executor;

    public DesktopGeneratorController() {
        executor = new LongTaskExecutor(true, "Generator");
    }

    public Generator[] getGenerators() {
        return Lookup.getDefault().lookupAll(Generator.class).toArray(new Generator[0]);
    }

    public void generate(final Generator generator) {

        String title = generator.getName();
        GeneratorUI ui = generator.getUI();
        if (ui != null) {
            ui.setup(generator);
            JPanel panel = ui.getPanel();
            final DialogDescriptor dd = new DialogDescriptor(panel, title);
            if (panel instanceof ValidationPanel) {
                ValidationPanel vp = (ValidationPanel) panel;
                vp.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                    }
                });
            }
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (result != NotifyDescriptor.OK_OPTION) {
                return;
            }
            ui.unsetup();
        }

        final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
        container.setSource("" + generator.getName());
        container.setReport(new Report());
        String taskname = "Generate " + generator.getName();

        //Error handler
        LongTaskErrorHandler errorHandler = new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.WARNING, "", t.getCause() != null ? t.getCause() : t);
            }
        };

        //Execute
        executor.execute(generator, new Runnable() {

            public void run() {
                generator.generate(container.getLoader());
                finishGenerate(container);
            }
        }, taskname, errorHandler);
    }

    private void finishGenerate(Container container) {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace;
        if (pc.getCurrentProject() == null) {
            pc.newProject();
            workspace = pc.getCurrentWorkspace();
        } else {
            if (pc.getCurrentWorkspace() == null) {
                workspace = pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(workspace);
            } else {
                workspace = pc.getCurrentWorkspace();
            }
        }
        if (container.getSource() != null) {
            pc.setSource(workspace, container.getSource());
        }

        Lookup.getDefault().lookup(Processor.class).process(workspace, container.getUnloader());
    }
}
