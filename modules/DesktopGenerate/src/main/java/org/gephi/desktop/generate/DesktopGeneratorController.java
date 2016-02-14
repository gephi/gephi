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
package org.gephi.desktop.generate;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.io.generator.api.GeneratorController;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.Report;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = GeneratorController.class)
public class DesktopGeneratorController implements GeneratorController {

    private final LongTaskExecutor executor;

    public DesktopGeneratorController() {
        executor = new LongTaskExecutor(true, "Generator");
    }

    @Override
    public Generator[] getGenerators() {
        return Lookup.getDefault().lookupAll(Generator.class).toArray(new Generator[0]);
    }

    @Override
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

                    @Override
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

        final Container container = Lookup.getDefault().lookup(Container.Factory.class).newContainer();
        container.setSource("" + generator.getName());
        container.setReport(new Report());
        String taskname = NbBundle.getMessage(DesktopGeneratorController.class, "DesktopGeneratorController.taskname", generator.getName());

        //Error handler
        LongTaskErrorHandler errorHandler = new LongTaskErrorHandler() {

            @Override
            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.SEVERE, "", t.getCause() != null ? t.getCause() : t);
            }
        };

        //Execute
        executor.execute(generator, new Runnable() {

            @Override
            public void run() {
                generator.generate(container.getLoader());
                finishGenerate(container);
            }
        }, taskname, errorHandler);
    }

    private void finishGenerate(Container container) {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        ProjectControllerUI pcui = Lookup.getDefault().lookup(ProjectControllerUI.class);
        Workspace workspace;
        if (pc.getCurrentProject() == null) {
            pcui.newProject();
            workspace = pc.getCurrentWorkspace();
        } else {
            workspace = pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace);
        }
        if (container.getSource() != null) {
            pc.setSource(workspace, container.getSource());
        }

        container.closeLoader();

        DefaultProcessor defaultProcessor = new DefaultProcessor();
        defaultProcessor.setContainers(new ContainerUnloader[]{container.getUnloader()});
        defaultProcessor.setWorkspace(workspace);
        defaultProcessor.process();
    }
}
