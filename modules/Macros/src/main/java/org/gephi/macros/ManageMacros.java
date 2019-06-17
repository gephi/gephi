/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.macros;

import java.util.ArrayList;
import org.gephi.macros.Macro;

public class ManageMacros {
    private ArrayList<Macro> macros;
    
    public ManageMacros(){
        macros = new ArrayList<Macro>();
        getMacros("abc");
    }
    
    // Get a JSON filepath and transform it into a list of macros
    public void getMacros(String filepath){
        
    }
    
    // Transform macros array into JSON file
    public void saveMacros(){
        
    }
    
    public ArrayList<String> getMacrosNames(){
        ArrayList<String> names = new ArrayList<String>();
        
        for(Macro m : macros)
            names.add(m.getName());
        return names;
    }
    
    public void editName(String newName, int index){
        macros.get(index).setName(newName);
        saveMacros();
    }
}
