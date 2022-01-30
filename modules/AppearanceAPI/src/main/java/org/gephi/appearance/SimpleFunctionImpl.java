/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.appearance;

import org.gephi.appearance.api.SimpleFunction;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;

/**
 * @author mbastian
 */
public class SimpleFunctionImpl extends FunctionImpl implements SimpleFunction {

    public SimpleFunctionImpl(String name, Class<? extends Element> elementClass, Transformer transformer,
                              TransformerUI transformerUI) {
        super(name, elementClass, null, transformer, transformerUI, null, null);
    }
}
