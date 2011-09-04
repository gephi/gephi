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
package org.gephi.preview.spi;

import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;

/**
 * Renderer describes how a particular Item object is rendered on a particular
 * RenderTarget instance.
 *
 * In particular, a Renderer first pre-process all user-specified information
 * from Properties. Then, the Renderer object will execute the render procedure
 * provided by particular sub-types.
 *
 * Renderer should ONLY include information about the rendering procedure.
 *
 * @author Yudi Xue, Mathieu Bastian
 */
public interface Renderer {

    /**
     * Interpret user-specified properties from the PreviewModel.
     *
     * @param previewModel a PreviewModel object
     */
    public void preProcess(PreviewModel previewModel);


    /**
     * specify how to render the Item object to a RenderTarget.
     *
     * @param item an Item object
     * @param target a RendererTarget object
     * @param properties a PreviewProperties object
     */
    public void render(Item item, RenderTarget target,
            PreviewProperties properties);

    /**
     * Return all associated properties from this Renderer object.
     *
     * @return a PreviewProperty array
     */
    public PreviewProperty[] getProperties();

    /**
     * Based on current property state, determin whether this Renderer object
     * is valid to render the provided Item object.
     *
     * @param item an Item object
     * @param properties a PreviewProperties object
     * @return  true - the Renderer is valid for the particular Item object
     *          false - the Renderer is invalid for the particular Item object
     */
    public boolean isRendererForitem(Item item,
            PreviewProperties properties);
}
