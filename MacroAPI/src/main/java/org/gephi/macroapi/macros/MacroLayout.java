/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.macroapi.macros;

import java.util.List;
import org.gephi.layout.spi.Layout;

/**
 *
 * @author u124275
 */
public class MacroLayout extends Macro{
    List<Layout> actions;
    
    MacroLayout(List<Layout> actions){
        super();
        this.actions = actions;
    }
}
