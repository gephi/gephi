/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

import org.gephi.data.attributes.api.AttributeType;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Eduardo Ramos<eduramiba@gmail.com>
 */
public class DynamicParserTest {
    
    private String parseDynamic(String str){
        return parseDynamic(str, AttributeType.DYNAMIC_STRING);
    }
    
    private String parseDynamic(String str, AttributeType type){
        return type.parse(str).toString();
    }

    @Test
    public void testParseIntervals() throws Exception {
        assertEquals(parseDynamic("[2.0, 3.5, \"; A3R; JJG; JJG\"); [3.5, 8.0, \"; A3R; JJG; [ ] () , JJG\"]; [10,20,30]")
                , "<[2.0, 3.5, \"; A3R; JJG; JJG\"); [3.5, 8.0, \"; A3R; JJG; [ ] () , JJG\"]; [10.0, 20.0, 30]>");
        
        assertEquals(parseDynamic("<[' 2.0', '3.5', ';a b c')")
              , "<[2.0, 3.5, \";a b c\")>");
        
        assertEquals(parseDynamic(" (  1, 2,  )  (4,5, '[\\'a;b\\']']")
              , "<(1.0, 2.0); (4.0, 5.0, \"['a;b']\"]>");
        
        assertEquals(parseDynamic("[1.25,1.55, <test>]"), "<[1.25, 1.55, <test>]>");
        assertEquals(parseDynamic("[1.25,'1.55' <test>]"), "<[1.25, 1.55, <test>]>");
        
        assertEquals(parseDynamic("[1.25,1.55,   \"21.12  \"  ]", AttributeType.DYNAMIC_DOUBLE), "<[1.25, 1.55, 21.12]>");
        
        assertEquals(parseDynamic("[1.25,1.55]", AttributeType.DYNAMIC_DOUBLE), "<[1.25, 1.55]>");
        
        assertEquals(parseDynamic("[1.25,1.55]", AttributeType.TIME_INTERVAL), "<[1.25, 1.55]>");
    }
}
