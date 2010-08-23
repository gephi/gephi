/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

package org.gephi.timeline.spi;

import java.awt.MenuContainer;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import javax.accessibility.Accessible;
import org.gephi.timeline.api.TimelineAnimator;
import org.gephi.timeline.api.TimelineModel;

/**
 *
 * @author jbilcke
 */
public interface TimelineDrawer
        extends Accessible,
        ImageObserver,
        MenuContainer,
        Serializable {

    public void setModel(TimelineModel model);
    public TimelineModel getModel();

    // public void setAnimator(TimelineAnimator animator);

}
