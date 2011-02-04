/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.visualization.apiimpl.contextmenuitems;

import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Eduardo
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class CopyToWorkspace extends CopyOrMoveToWorkspace{

    @Override
    protected boolean isCopy() {
        return true;
    }

    public String getName() {
        return NbBundle.getMessage(CopyOrMoveToWorkspace.class, "GraphContextMenu_CopyToWorkspace");
    }

    public int getPosition() {
        return 200;
    }
}
