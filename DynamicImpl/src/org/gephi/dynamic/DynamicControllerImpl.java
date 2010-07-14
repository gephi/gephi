/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.dynamic;

import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.api.WorkspaceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * It is the default implementation of the {@code DynamicController} class.
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = DynamicController.class)
public final class DynamicControllerImpl implements DynamicController {
	private ProjectController projectController;

	/**
	 * The default constructor.
	 */
	public DynamicControllerImpl() {
		projectController = Lookup.getDefault().lookup(ProjectController.class);
		projectController.addWorkspaceListener(new WorkspaceListener() {
			@Override
			public void initialize(Workspace workspace) {
				workspace.add(new DynamicModelImpl(workspace));
			}

			@Override
			public void select(Workspace workspace) { }

			@Override
			public void unselect(Workspace workspace) { }

			@Override
			public void close(Workspace workspace) { }

			@Override
			public void disable() { }
		});
		if (projectController.getCurrentProject() != null) {
			Workspace[] workspaces = projectController.getCurrentProject().getLookup().
										lookup(WorkspaceProvider.class).getWorkspaces();
			for (Workspace workspace : workspaces) {
				DynamicModel m = workspace.getLookup().lookup(DynamicModel.class);
				if (m == null)
					workspace.add(new DynamicModelImpl(workspace));
			}
		}
	}

	@Override
	public DynamicModel getModel() {
		Workspace workspace = projectController.getCurrentWorkspace();
		if (workspace != null) {
			DynamicModel model = workspace.getLookup().lookup(DynamicModel.class);
			if (model != null)
				return model;
			model = new DynamicModelImpl(workspace);
			workspace.add(model);
			return model;
		}
		return null;
	}

	@Override
	public DynamicModel getModel(Workspace workspace) {
		if (workspace != null) {
			DynamicModel model = workspace.getLookup().lookup(DynamicModel.class);
			if (model != null)
				return model;
			model = new DynamicModelImpl(workspace);
			workspace.add(model);
			return model;
		}
		return null;
	}

	@Override
	public void setVisibleInterval(TimeInterval interval) {
		setVisibleInterval(interval.getLow(), interval.getHigh());
	}

	@Override
	public void setVisibleInterval(double low, double high) {
		Workspace workspace = projectController.getCurrentWorkspace();
		if (workspace != null) {
			DynamicModel model = workspace.getLookup().lookup(DynamicModel.class);
			if (model != null)
				model.setVisibleInterval(low, high);
			model = new DynamicModelImpl(workspace);
			model.setVisibleInterval(low, high);
			workspace.add(model);
		}
	}
}
