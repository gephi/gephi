/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.scale;

import javax.swing.Icon;
import org.gephi.layout.api.LayoutBuilder;
import org.openide.util.NbBundle;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class Expand implements LayoutBuilder {

    public String getName() {
        return NbBundle.getMessage(Expand.class, "expand_name");
    }

    public String getDescription() {
        return NbBundle.getMessage(Expand.class, "expand_description");
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ScaleLayout buildLayout() {
        return new ScaleLayout(this, 1.2);
    }
}
