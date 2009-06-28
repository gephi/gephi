/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force.yifanHu;

import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class YifanHu implements LayoutBuilder {

    public Layout buildLayout() {
        YifanHuLayout layout = new YifanHuLayout();
        return layout;
    }
}
