/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
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
package org.gephi.preview.api;

import org.gephi.project.api.Workspace;

/**
 *
 * @author Yudi Xue, Mathieu Bastian
 */
public interface PreviewController {

    public void refreshPreview(Workspace workspace);

    public void refreshPreview();

    public PreviewModel getModel();

    public PreviewModel getModel(Workspace workspace);
    
    public void render(RenderTarget target);
    
    public RenderTarget getRenderTarget(String name);
}
