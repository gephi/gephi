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

/*
 * DefaultLayer.java
 *
 * Created on Jun 21, 2009, 5:02:48 PM
 */
package org.gephi.timeline.ui.layers.impl;

import org.gephi.timeline.api.TimelineDataModel;
import org.gephi.timeline.ui.FakeTimelineDataModel;
import org.gephi.timeline.ui.layers.api.Layer;
import org.gephi.timeline.ui.skins.impl.DefaultSkin;
import org.gephi.timeline.ui.skins.api.TimelineSkin;

/**
 *
 * @author Julian Bilcke
 */
public class DefaultLayer extends javax.swing.JPanel implements Layer {

    private static final long serialVersionUID = 1L;
    protected TimelineSkin skin = new DefaultSkin();
    protected TimelineDataModel model = new FakeTimelineDataModel();

    public void setSkin(TimelineSkin skin) {
        this.skin = skin;
    }

    public void setModel(TimelineDataModel model) {
        this.model = model;
    }

    public TimelineDataModel getModel() {
       return model;
    }

    public TimelineSkin getSkin() {
        return skin;
    }
}
