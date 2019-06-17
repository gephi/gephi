/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.branding.desktop.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;
import org.gephi.macros.Macro;

public final class Macros extends SystemAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> macros = new ArrayList<>();
                
                macros.add("Macro 1");
                macros.add("Macro 2");
                macros.add("Macro 3");
                
                
                MacrosPanelList component = MacrosPanelList.getInstance();
                
                component.setMacrosList(macros);
                
                JDialog dialog = new JDialog(WindowManager.getDefault().getMainWindow(),
                        component.getName(), false);
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.getContentPane().add(component);
                dialog.setBounds(200, 200, 420, 350);
                dialog.setVisible(true);
            }
        });
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(Macros.class, "CTL_Macros");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}