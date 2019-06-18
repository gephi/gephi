/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.macroapi.macros;

import java.util.ArrayList;
import javax.swing.JOptionPane;

public class ManageMacros {
    private static ArrayList<Macro> macros = new ArrayList<Macro>();
    private static boolean isRecording;
    private static Macro currentMacro;
    
    private ManageMacros(){
        isRecording = false;
        currentMacro = null;
        getMacros("abc");
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
    
    // Get a JSON filepath and transform it into a list of macros
    public void getMacros(String filepath){
        
    }
    
    // Transform macros array into JSON file
    public void saveMacros(){
        
    }
    
    public static ArrayList<String> getMacrosNames(){
        ArrayList<String> names = new ArrayList<String>();
        
        for(Macro m : macros)
            names.add(m.getName());
        return names;
    }
    
    public static void editName(String newName, int index){
        macros.get(index).setName(newName);
        //saveMacros();
    }
}
