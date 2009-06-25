/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.multilevel;

import javax.swing.Icon;
import org.gephi.layout.force.yifanHu.YifanHu;
import org.openide.util.NbBundle;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class MultiLevelLayout extends AbstractMultiLevelLayout {

    @Override
    protected YifanHu getForceLayout() {
        YifanHu layout = new YifanHu();
        
        layout.initAlgo(getGraph());
        layout.resetPropertiesValues();

        return layout;
    }

    @Override
    protected CoarseningStrategy getCoarseningStrategy() {
        return new MaximalMatchingCoarsening();
    }

    public String getName() {
        return NbBundle.getMessage(MultiLevelLayout.class, "name");
    }

    public String getDescription() {
        return NbBundle.getMessage(MultiLevelLayout.class, "description");
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
