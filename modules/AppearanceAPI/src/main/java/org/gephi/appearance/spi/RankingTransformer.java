/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.spi;

import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public interface RankingTransformer<E extends Element> extends Transformer {

    public void transform(E element, float rankingValue);
}
