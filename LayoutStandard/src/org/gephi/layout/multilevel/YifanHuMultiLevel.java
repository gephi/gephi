/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gmail.com>
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
package org.gephi.layout.multilevel;

import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.force.yifanHu.YifanHu;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class YifanHuMultiLevel implements LayoutBuilder {

    public Layout buildLayout() {
        MultiLevelLayout layout = new MultiLevelLayout(new MaximalMatchingCoarsening(),
                                                       new YifanHu(),
                                                       "Multilevel YifanHu",
                                                       "Multilevel YifanHu");
        return layout;
    }
}
