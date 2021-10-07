/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.macroapi.macros;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Macro {
    private List<Map<MacroType, Object>> actions;
    private String name;
    private Date date;
    
    public Macro(){
        actions = new ArrayList<>();
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
    
    public List<Map<MacroType, Object>> getActions(){
        return actions;
    }
    
    public void setActions(List<Map<MacroType, Object>> actions) {
        this.actions = actions;
    }
        
    public void addAction(Map<MacroType, Object> action){
        this.actions.add(action);
    }
    
    public void save(){
        this.date = new Date();
    }
    
    public void execute(){
        
    }
}