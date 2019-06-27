/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.branding.desktop;

import org.gephi.macroapi.macros.Macro;
import org.gephi.macroapi.macros.ManageMacros;

import javax.swing.*;
import java.util.List;
import java.io.FileWriter;
import java.util.Map;
import org.gephi.desktop.appearance.AppearanceTopComponent;
import org.gephi.desktop.filters.FiltersPanel;
import org.gephi.desktop.layout.LayoutPanel;
import org.gephi.macroapi.macros.MacroType;

public class MacrosPanelList extends javax.swing.JPanel {

    private static MacrosPanelList instance;

    public MacrosPanelList() {
        initComponents();
    }
    
    public static synchronized MacrosPanelList getInstance() {
        if (instance == null) {
            instance = new MacrosPanelList();
        }
        return instance;
    }
    
    public void updateList(){
        list1.removeAll();
        for(String name : ManageMacros.getMacrosNames()){
            list1.add(name);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        list1 = new java.awt.List();
        removeButton = new javax.swing.JToggleButton();
        executeButton = new javax.swing.JToggleButton();
        editButton = new javax.swing.JToggleButton();
        recordButton = new javax.swing.JToggleButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(400, 400));
        setPreferredSize(new java.awt.Dimension(300, 275));

        list1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                list1ActionPerformed(evt);
            }
        });

        removeButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.removeButton.text")); // NOI18N
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.removeButton.toolTipText")); // NOI18N
        removeButton.setFocusable(false);
        removeButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        removeButton.setMargin(new java.awt.Insets(0, 7, 0, 7));
        removeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        executeButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(executeButton, org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.executeButton.text")); // NOI18N
        executeButton.setToolTipText(org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.executeButton.toolTipText")); // NOI18N
        executeButton.setFocusable(false);
        executeButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        executeButton.setMargin(new java.awt.Insets(0, 7, 0, 7));
        executeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        executeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeButtonActionPerformed(evt);
            }
        });

        editButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.editButton.text")); // NOI18N
        editButton.setToolTipText(org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.editButton.toolTipText")); // NOI18N
        editButton.setFocusable(false);
        editButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        editButton.setMargin(new java.awt.Insets(0, 7, 0, 7));
        editButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        recordButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(recordButton, org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.recordButton.text")); // NOI18N
        recordButton.setToolTipText(org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.recordButton.toolTipText")); // NOI18N
        recordButton.setFocusable(false);
        recordButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        recordButton.setMargin(new java.awt.Insets(0, 7, 0, 7));
        recordButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        recordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recordButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(list1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(executeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(recordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(66, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(list1, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(recordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(executeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void list1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_list1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_list1ActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        
        int idx = list1.getSelectedIndex();
        String selectedName= list1.getSelectedItem();

        if(selectedName != null){
            String macroName = JOptionPane.showInputDialog("Enter a new name");
            if(macroName == null)   
                return;
            while(ManageMacros.existMacro(macroName) || ("".equals(macroName))){
                macroName = JOptionPane.showInputDialog("A macro with that name already exist! Please, enter a new macro name");
            }
            ManageMacros.editName(macroName, idx);
            updateList();
        }else{
            JOptionPane.showMessageDialog(null, "Please select a Macro first");
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed

        String macroName= list1.getSelectedItem();

        if(!(macroName == null || macroName.equals(""))){
                int dialogResult = JOptionPane.showConfirmDialog (null, "Are you sure you want to delete this macro?","Warning",JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    ManageMacros.deleteMacro(macroName);
                    updateList();
                }
        }else{
            JOptionPane.showMessageDialog(null, "Please select a Macro first");
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void executeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeButtonActionPerformed
        String macroName= list1.getSelectedItem();

        if(macroName != null){
            //ManageMacros.executeMacro(macrosName);
            Macro macro = ManageMacros.getMacroByName(macroName);
            
            for(Map<MacroType, Object> currentAction : macro.getActions()){
                if(currentAction.get(MacroType.APPEARANCE) != null){
                    Object f = currentAction.get(MacroType.APPEARANCE);
                    AppearanceTopComponent appearanceInstance = AppearanceTopComponent.getInstance();
                    appearanceInstance.executeAction(f);
                }
                if(currentAction.get(MacroType.LAYOUT) != null){
                    Object o = currentAction.get(MacroType.LAYOUT);
                    LayoutPanel layoutInstance = LayoutPanel.getInstance();
                    layoutInstance.executeAction(o);
                }
                if(currentAction.get(MacroType.FILTER) != null){
                    Object o = currentAction.get(MacroType.FILTER);
                    FiltersPanel filtersInstance = FiltersPanel.getInstance();
                    filtersInstance.executeAction(o);
                }
            }
        }else{
            JOptionPane.showMessageDialog(null, "Please select a Macro first");
        }
    }//GEN-LAST:event_executeButtonActionPerformed

    private void recordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordButtonActionPerformed
        
        if(ManageMacros.getRecordingState()){
            ManageMacros.changeRecordingState(false);
            recordButton.setText("Record Macro");
            Macro macro = ManageMacros.getCurrentMacro();

            if(macro.getActions().isEmpty()) {
                JOptionPane.showMessageDialog(null, "No actions were detected.");
            } else {
                String macroName = JOptionPane.showInputDialog("Enter a macro name");
                if(macroName == null){
                    ManageMacros.getCurrentMacro();
                    return;
                }
                while(ManageMacros.existMacro(macroName) || ("".equals(macroName))){
                    if(ManageMacros.existMacro(macroName)){
                        macroName = JOptionPane.showInputDialog("A macro with that name already exist! Please, enter a new macro name");
                    } else {
                        macroName = JOptionPane.showInputDialog("Empty value is not allowed, please enter a name:");
                    }
                }
                macro.setName(macroName);
                ManageMacros.addMacro(macro);
                updateList();
            }
        }else{
            ManageMacros.changeRecordingState(true);
            recordButton.setText("Stop Recording");
            ManageMacros.addCurrentMacro(new Macro());                         
        }
    }//GEN-LAST:event_recordButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton editButton;
    private javax.swing.JToggleButton executeButton;
    private java.awt.List list1;
    private javax.swing.JToggleButton recordButton;
    private javax.swing.JToggleButton removeButton;
    // End of variables declaration//GEN-END:variables
}