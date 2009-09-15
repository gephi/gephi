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
package org.gephi.visualization.opengl.text;

import javax.swing.ImageIcon;
import org.gephi.visualization.api.ModelImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class FixedSizeMode implements SizeMode {

    private TextModel textModel;

    public FixedSizeMode(TextModel model) {
        this.textModel = model;
    }

    public void setSizeFactor(TextDataImpl text, ModelImpl model) {
        float factor = textModel.sizeFactor*1.9f +0.1f;        //Between 0.1 and 2
        text.setSizeFactor(factor);
    }

    public String getName() {
        return "Fixed";
    }

    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/visualization/opengl/text/FixedSizeMode.png"));
    }
}
