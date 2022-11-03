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

package org.gephi.layout;

import org.gephi.layout.api.LayoutController;
import org.gephi.layout.spi.Layout;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.spi.Controller;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * @author Mathieu Bastian
 */
@ServiceProviders({
    @ServiceProvider(service = LayoutController.class),
    @ServiceProvider(service = Controller.class)})
public class LayoutControllerImpl implements LayoutController, Controller<LayoutModelImpl> {

    private LayoutRun layoutRun;

    public LayoutControllerImpl() {
        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {

            }

            @Override
            public void select(Workspace workspace) {

            }

            @Override
            public void unselect(Workspace workspace) {
                LayoutModelImpl model = getModel(workspace);
                if (model != null && model.getSelectedLayout() != null) {
                    model.saveProperties(model.getSelectedLayout());
                }
            }

            @Override
            public void close(Workspace workspace) {
                LayoutModelImpl model = getModel(workspace);
                if (model != null) {
                    model.getExecutor().cancel();
                }
            }

            @Override
            public void disable() {
            }
        });
    }

    @Override
    public Class<LayoutModelImpl> getModelClass() {
        return LayoutModelImpl.class;
    }

    @Override
    public LayoutModelImpl newModel(Workspace workspace) {
        return new LayoutModelImpl(workspace);
    }

    @Override
    public LayoutModelImpl getModel(Workspace workspace) {
        return Controller.super.getModel(workspace);
    }

    @Override
    public LayoutModelImpl getModel() {
        return Controller.super.getModel();
    }

    @Override
    public void setLayout(Layout layout) {
        getModel().setSelectedLayout(layout);
    }

    @Override
    public void executeLayout() {
        LayoutModelImpl model = getModel();
        if (model.getSelectedLayout() != null) {
            layoutRun = new LayoutRun(model.getSelectedLayout());
            model.getExecutor().execute(layoutRun, layoutRun);
            model.setRunning(true);
        }
    }

    @Override
    public void executeLayout(int numIterations) {
        LayoutModelImpl model = getModel();
        if (model.getSelectedLayout() != null) {
            layoutRun = new LayoutRun(model.getSelectedLayout(), numIterations);
            model.getExecutor().execute(layoutRun, layoutRun);
            model.setRunning(true);
        }
    }

    @Override
    public boolean canExecute() {
        LayoutModelImpl model = getModel();
        return model.getSelectedLayout() != null && !model.isRunning();
    }

    @Override
    public boolean canStop() {
        LayoutModelImpl model = getModel();
        return model.isRunning();
    }

    @Override
    public void stopLayout() {
        LayoutModelImpl model = getModel();
        model.getExecutor().cancel();
    }

    private static class LayoutRun implements LongTask, Runnable {

        private final Layout layout;
        private final Integer iterations;
        private boolean stopRun = false;
        private ProgressTicket progressTicket;

        public LayoutRun(Layout layout) {
            this.layout = layout;
            this.iterations = null;
        }

        public LayoutRun(Layout layout, int numIterations) {
            this.layout = layout;
            this.iterations = numIterations;
        }

        @Override
        public void run() {
            Progress.setDisplayName(progressTicket, layout.getBuilder().getName());
            Progress.start(progressTicket);
            layout.initAlgo();
            long i = 0;
            while (layout.canAlgo() && !stopRun) {
                layout.goAlgo();
                i++;
                if (iterations != null && iterations.longValue() == i) {
                    break;
                }
            }
            layout.endAlgo();
            if (i > 1) {
                Progress.finish(progressTicket,
                    NbBundle.getMessage(LayoutControllerImpl.class, "LayoutRun.end", layout.getBuilder().getName(), i));
            } else {
                Progress.finish(progressTicket);
            }
        }

        @Override
        public boolean cancel() {
            stopRun = true;
            if (layout instanceof LongTask) {
                return ((LongTask) layout).cancel();
            }
            return false;
        }

        @Override
        public void setProgressTicket(ProgressTicket progressTicket) {
            this.progressTicket = progressTicket;
            if (layout instanceof LongTask) {
                ((LongTask) layout).setProgressTicket(progressTicket);
            }
        }
    }
}
