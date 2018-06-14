/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import org.jfree.data.xy.XYSeries;

/**
 *
 * @author u104065
 */
public class csvCreator {
    
    static String csv;
    public static final String separator = "</HTML>";
    
    public static String generateData(XYSeries data){
        
        if(!data.getItems().isEmpty()){
            csv = "x;y\n";
            for (int i = 0; i < data.getItems().size(); i++) {
                csv += data.getX(i) + ";" + data.getY(i) + "\n";
            }
        }else{
            csv += "";
        }
        
        return csv;
    }
    
    public static void splitter(){
        
    }
    
}
