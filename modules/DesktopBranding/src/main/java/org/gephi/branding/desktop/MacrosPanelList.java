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

public class MacrosPanelList extends javax.swing.JPanel {

    private static MacrosPanelList instance;
    private List<String> macrosList;

    public MacrosPanelList() {
        initComponents();
    }
    
    public static synchronized MacrosPanelList getInstance() {
        if (instance == null) {
            instance = new MacrosPanelList();
        }
        return instance;
    }

    public void setMacrosList(List<String> list){
        this.macrosList = list;
        for(String s : macrosList)
            list1.add(s);
    }
    
    public void addMacro(String macro){
        macrosList.add(macro);
        list1.add(macro);
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
        ExecuteButton = new javax.swing.JToggleButton();
        editButton = new javax.swing.JToggleButton();
        recordButton = new javax.swing.JButton();

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

        ExecuteButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ExecuteButton, org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.ExecuteButton.text")); // NOI18N
        ExecuteButton.setToolTipText(org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.ExecuteButton.toolTipText")); // NOI18N
        ExecuteButton.setFocusable(false);
        ExecuteButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ExecuteButton.setMargin(new java.awt.Insets(0, 7, 0, 7));
        ExecuteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ExecuteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExecuteButtonActionPerformed(evt);
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

        org.openide.awt.Mnemonics.setLocalizedText(recordButton, org.openide.util.NbBundle.getMessage(MacrosPanelList.class, "MacrosPanelList.recordButton.text")); // NOI18N
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
                .addGap(43, 43, 43)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ExecuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(recordButton)))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(list1, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(recordButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ExecuteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void list1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_list1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_list1ActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int idx = list1.getSelectedIndex();
        JTextField field = new JTextField(list1.getSelectedItem());
        list1.replaceItem("", idx);
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int idx = list1.getSelectedIndex();
        if(idx != -1){
            int dialogResult = JOptionPane.showConfirmDialog (null, "Are you sure you want to delete this macro?","Warning",JOptionPane.YES_NO_OPTION);
            if(dialogResult == JOptionPane.YES_OPTION){
                    macrosList.remove(idx);
                    list1.remove(idx);
            }
        }else{
            JOptionPane.showMessageDialog(null, "Please select a Macro first");
        }

    }//GEN-LAST:event_removeButtonActionPerformed

    private void ExecuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExecuteButtonActionPerformed
        //EXECUTE RECORDED MACRO
        int idx = list1.getSelectedIndex();
        if(idx != -1){
            String name = macrosList.get(idx);
            ManageMacros.executeMacro(name);
        }else{
            JOptionPane.showMessageDialog(null, "Please select a Macro first");
        }
    }//GEN-LAST:event_ExecuteButtonActionPerformed

    private void recordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordButtonActionPerformed
        if(ManageMacros.getRecordingState()){
            ManageMacros.changeRecordingState(false);
            recordButton.setText("Record Macro");
            Macro macro = ManageMacros.getCurrentMacro();
            String macroName = JOptionPane.showInputDialog("Enter a macro name");
            macro.setName(macroName);
            ManageMacros.addMacro(macro);
            // Testejar això, fa un refresh en teoria
            //revalidate();
            //repaint();
        }else{
            ManageMacros.changeRecordingState(true);
            recordButton.setText("Stop Recording");
            ManageMacros.addCurrentMacro(new Macro());
        }
            
    }//GEN-LAST:event_recordButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton ExecuteButton;
    private javax.swing.JToggleButton editButton;
    private java.awt.List list1;
    private javax.swing.JButton recordButton;
    private javax.swing.JToggleButton removeButton;
    // End of variables declaration//GEN-END:variables
}
