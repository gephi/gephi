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
package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import org.gephi.branding.desktop.BannerTopComponent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class ResetWindows extends SystemAction {

    public void actionPerformed(ActionEvent e) {
        BannerTopComponent banner = (BannerTopComponent) WindowManager.getDefault().findTopComponent("BannerTopComponent");
        if (banner != null) {
            banner.reset();
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ResetWindows.class, "CTL_ResetWindows");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
