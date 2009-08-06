/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gephi.org>.
 */
package org.gephi.layout.force.yifanHu;

import javax.swing.Icon;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.openide.util.NbBundle;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class YifanHu implements LayoutBuilder {

    public Layout buildLayout() {
        YifanHuLayout layout = new YifanHuLayout(this);
        return layout;
    }

    public String getName() {
        return NbBundle.getMessage(YifanHu.class, "YifanHu_name");
    }

    public String getDescription() {
        return NbBundle.getMessage(YifanHu.class, "YifanHu_description");
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
