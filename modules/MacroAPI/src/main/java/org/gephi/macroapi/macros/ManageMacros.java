/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.macroapi.macros;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ManageMacros {
    private static List<Macro> macros = new ArrayList<Macro>();
    private static boolean isRecording;
    private static Macro currentMacro;
    
    private ManageMacros(){
        isRecording = false;
        currentMacro = null;
    }
    
    public static void changeRecordingState(boolean state){
        if(state){
            isRecording = true;
            JOptionPane.showMessageDialog(null, "The system will start recording your actions now.");
        }
        else{
            isRecording = false;
            JOptionPane.showMessageDialog(null, "Macro recording stopped. Actions saved.");
        }
    }
     
    public static boolean getRecordingState() {
        return isRecording;
    }
    
    public static void addMacro(Macro macro){
        macros.add(macro);
    }
    
    public static void addCurrentMacro(Macro macro){
        currentMacro = macro;
    }
    
    public static Macro getCurrentMacro(){
        return currentMacro;
    }
    
    public static void executeMacro(String macroName){
        Macro toExecute = null;
        toExecute = getMacroByName(macroName);
        //if(toExecute != null)
            //AppearanceTopComponent.setMacroToExecute(toExecute);
        //AppearanceTopComponent.executeMacro();
    }
    
    public static List<String> getMacrosNames(){
        List<String> names = new ArrayList<String>();

        for(Macro m : macros) {
            names.add(m.getName());
        }
        return names;
    }
    
    public static Macro getMacroByName(String macroName){
        Macro returnMacro = null;
        
        for(Macro iteratedMacro : macros){
            if(iteratedMacro.getName().equals(macroName)){
                returnMacro = iteratedMacro;
                break;
            }
        }
        
        return returnMacro;
    }
    
    public static void editName(String newName, int index){
        macros.get(index).setName(newName);
        //saveMacros();
    }
}
