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
package org.gephi.desktop.perspective.spi;

import org.gephi.desktop.perspective.plugin.LaboratoryPerspective;
import org.gephi.desktop.perspective.plugin.OverviewPerspective;
import org.gephi.desktop.perspective.plugin.PreviewPerspective;

/**
 * Interface to put on <code>TopComponent</code> to say in which perspective it
 * belongs. It has an <b>open</b> and <b>close</b> method to simply say if the
 * component should open or close when asked.
 * <h3>How to set to a TopComponent</h3>
 * <ol><li>Implement this interface to the class that extends <code>TopComponent</code>.</li>
 * <li>Fill <b>open</b> and <b>close</b> methods like explain below.</li>
 * <li>Add the <code>@ServiceProvider</code> annotation to be registered in the system:</li></ol>
 * <pre>
 * <code>@ServiceProvider(service=PerspectiveMember.class)
 * public class MyTopComponent extends TopComponent implements PerpectiveMember {
 * ...
 * </pre>
 * <h3>How to code open and close methods</h3>
 * The code below attach the component to the {@link LaboratoryPerspective}, works also
 * for {@link OverviewPerspective} and {@link PreviewPerspective}:
 * <pre>
 * public boolean open(Perspective perspective) {
 *    returns perspective instanceof LaboratoryPerspective;
 * }
 * public boolean close(Perspective perspective) {
 *    returns true;
 * }
 * </pre>
 * @author Mathieu Bastian
 */
public interface PerspectiveMember {

    /**
     * Returns <code>true</code> if this component opens when
     * <code>perspective</code> opens.
     * @param perspective   the perspective that is to be opened
     * @return              <code>true</code> if this component opens,
     * <code>false</code> otherwise
     */
    public boolean open(Perspective perspective);

    /**
     * Returns <code>true</code> if this component closes when
     * <code>perspective</code> closes.
     * @param perspective   the perspective that is to be closed
     * @return              <code>true</code> if this component closes,
     * <code>false</code> otherwise
     */
    public boolean close(Perspective perspective);
}
