/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.macros;

import java.util.ArrayList;
import java.util.Date;
import org.gephi.appearance.api.Function;
import org.gephi.desktop.appearance.AppearanceUIController;
import org.openide.util.Lookup;

public class Macro {
    private ArrayList<Function> functions;
    private String name;
    private Date date;
    
    public Macro(){
        functions = new ArrayList<Function>();
    }
    
    public String getName(){
        return this.name;
    }
    
    public Date getDate(){
        return this.date;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setDate(Date date){
        this.date = date;
    }
    
    public void addFunction(Function f){
        functions.add(f);
    }
    
    public void save(){
        this.date = new Date();
    }
    
    public void execute(){
        
        AppearanceUIController controller;
        controller = Lookup.getDefault().lookup(AppearanceUIController.class);
        
        for(Function f : functions){
            System.out.println("Executing function " + f);
            //controller.appearanceController.transform(f);
        }
    }
}