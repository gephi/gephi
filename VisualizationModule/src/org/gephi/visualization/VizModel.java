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
package org.gephi.visualization;

import java.util.Arrays;
import org.gephi.visualization.api.GraphDrawable;
import org.gephi.visualization.opengl.text.TextModel;

/**
 *
 * @author Mathieu Bastian
 */
public class VizModel {

    protected float[] cameraLocation;
    protected float[] cameraTarget;
    protected TextModel textModel;

    public void writeModel() {
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        cameraLocation = Arrays.copyOf(drawable.getCameraLocation(), 3);
        cameraTarget = Arrays.copyOf(drawable.getCameraTarget(), 3);
        textModel = VizController.getInstance().getTextManager().getModel();
    }

    public void loadModel() {
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        drawable.setCameraLocation(Arrays.copyOf(cameraLocation, 3));
        drawable.setCameraTarget(Arrays.copyOf(cameraTarget, 3));
        VizController.getInstance().getTextManager().setModel(textModel);
    }
}
