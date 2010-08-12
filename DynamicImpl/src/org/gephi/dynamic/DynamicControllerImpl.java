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
	private DynamicModelImpl model;

	/**
	 * The default constructor.
	 */
	public DynamicControllerImpl() {
		ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
		projectController.addWorkspaceListener(new WorkspaceListener() {
			@Override
			public void initialize(Workspace workspace) {
				workspace.add(new DynamicModelImpl(workspace));
			}

			@Override
			public void select(Workspace workspace) {
				model = workspace.getLookup().lookup(DynamicModelImpl.class);
				if (model == null) {
					model = new DynamicModelImpl(workspace);
					workspace.add(model);
				}
			}

			@Override
			public void unselect(Workspace workspace) { }

			@Override
			public void close(Workspace workspace) { }

			@Override
			public void disable() {
				model = null;
			}
		});
		if (projectController.getCurrentProject() != null) {
			Workspace[] workspaces = projectController.getCurrentProject().getLookup().
					lookup(WorkspaceProvider.class).getWorkspaces();
			for (Workspace workspace : workspaces) {
				DynamicModelImpl m = (DynamicModelImpl)workspace.getLookup().lookup(DynamicModelImpl.class);
				if (m == null) {
					m = new DynamicModelImpl(workspace);
					workspace.add(m);
				}
				if (workspace == projectController.getCurrentWorkspace())
					model = m;
			}
		}
	}

	@Override
	public DynamicModel getModel() {
		return model;
	}

	@Override
	public DynamicModel getModel(Workspace workspace) {
		if (workspace != null) {
			DynamicModel m = workspace.getLookup().lookup(DynamicModel.class);
			if (m != null)
				return m;
			m = new DynamicModelImpl(workspace);
			workspace.add(m);
			return m;
		}
		return null;
	}

	@Override
	public void setVisibleInterval(TimeInterval interval) {
		if (model != null)
			model.setVisibleTimeInterval(interval);
	}

	@Override
	public void setVisibleInterval(double low, double high) {
		setVisibleInterval(new TimeInterval(low, high));
	}
}
