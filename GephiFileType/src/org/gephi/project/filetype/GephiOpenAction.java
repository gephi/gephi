/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.project.filetype;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class GephiOpenAction extends CookieAction {

    protected void performAction(Node[] activatedNodes) {
        GephiDataObject gephiDataObject = activatedNodes[0].getLookup().lookup(GephiDataObject.class);
    // TODO use gephiDataObject
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(GephiOpenAction.class, "CTL_GephiOpenAction");
    }

    protected Class[] cookieClasses() {
        return new Class[]{GephiDataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

