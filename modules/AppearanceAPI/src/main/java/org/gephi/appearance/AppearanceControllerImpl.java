/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.Interpolator;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = AppearanceController.class)
public class AppearanceControllerImpl implements AppearanceController {

    private AppearanceModelImpl model;

    public AppearanceControllerImpl() {
        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(AppearanceModelImpl.class);
                if (model == null) {
                    model = new AppearanceModelImpl(workspace);
                    workspace.add(model);
                }
//                model.select();
            }

            @Override
            public void unselect(Workspace workspace) {
//                model.unselect();
                model = null;
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                model = null;
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(AppearanceModelImpl.class);
            if (model == null) {
                model = new AppearanceModelImpl(pc.getCurrentWorkspace());
                pc.getCurrentWorkspace().add(model);
            }
        }
    }

    @Override
    public AppearanceModelImpl getModel() {
        return model;
    }

    @Override
    public AppearanceModelImpl getModel(Workspace workspace) {
        AppearanceModelImpl m = workspace.getLookup().lookup(AppearanceModelImpl.class);
        if (m == null) {
            m = new AppearanceModelImpl(workspace);
            workspace.add(m);
        }
        return m;
    }

    @Override
    public Transformer getTransformer(TransformerUI ui) {
        Class<? extends Transformer> transformerClass = ui.getTransformerClass();
        Transformer transformer = Lookup.getDefault().lookup(transformerClass);
        if (transformer != null) {
            return transformer;
        }
        return null;
    }

    @Override
    public void setInterpolator(Interpolator interpolator) {
        if (model != null) {
            model.setInterpolator(interpolator);
        }
    }

    @Override
    public void setUseLocalScale(boolean useLocalScale) {
        if (model != null) {
            model.setLocalScale(useLocalScale);
        }
    }
}
