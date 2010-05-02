/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
