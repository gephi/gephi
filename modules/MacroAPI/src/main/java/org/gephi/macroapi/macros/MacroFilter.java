/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.macroapi.macros;

import java.util.List;
import org.gephi.filters.api.Query;

/**
 *
 * @author u124275
 */
public class MacroFilter extends Macro{
    List<Query> actions;
    
    MacroFilter(List<Query> actions){
        super();
        this.actions = actions;
    }
}
