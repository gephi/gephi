/*
Copyright 2011 Gephi
Authors : Sébastien Heymann <sebastien.heymann@gephi.org>
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
package org.gephi.branding.desktop.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URI;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Sébastien Heymann
 */
public class OpenOnlineDoc extends SystemAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(OpenOnlineDoc.class, "CTL_OpenOnlineDoc");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    /*@Override
    protected String iconResource() {
        return "org/gephi/branding/desktop/actions/resources/cleanWorkspace.gif";
    }*/

    @Override
    public void actionPerformed(ActionEvent ev) {
        /*if (Desktop.isDesktopSupported()) {*/
            Desktop desktop = Desktop.getDesktop();
            /*if (desktop.isSupported(Desktop.Action.BROWSE)) {*/
                try {
                    desktop.browse(new URI("http://gephi.org/users/support/"));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            /*}
        }*/
    }
}
