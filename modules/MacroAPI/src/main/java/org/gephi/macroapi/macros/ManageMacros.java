/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.macroapi.macros;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        String filepath = System.getProperty("user.dir") + "/current_macro.json";
        if(state){
            isRecording = true;
            JOptionPane.showMessageDialog(null, "The system will start recording your actions now.");        
            try{
                FileWriter file = new FileWriter(filepath);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        else{
            isRecording = false;
            JOptionPane.showMessageDialog(null, "Macro recording stopped.");
            File file = new File(filepath); 
            file.delete();
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
    
    public static void deleteCurrentMacro(){
        currentMacro = null;
    }
    
    public static void executeMacro(String macroName){
        Macro macro = getMacroByName(macroName);
        System.out.println("Macro is: " + macro.toString());

        //switch(macro.getActions()){
            
        //}
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
        
        for(Macro iteratedMacro : macros){
            if(iteratedMacro.getName().equals(macroName)){
                return iteratedMacro;
            }
        }
        
        return null;
    }
    
    public static boolean existMacro(String macroName){
        
        for(Macro iteratedMacro : macros){
            if(iteratedMacro.getName().equals(macroName)){
                return true;
            }
        }
        return false;
    }
    
    
    public static void editName(String newName, int index){
        macros.get(index).setName(newName);
        //saveMacros();
    }
    
    public static void deleteMacro(String name){
        for(Macro iteratedMacro : macros){
            if(iteratedMacro.getName().equals(name)){
                macros.remove(iteratedMacro);
                break;
            }
        }
    }
}
