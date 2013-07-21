/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.plugin;

import java.awt.Color;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.graph.api.Element;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = RankingTransformer.class)
public class RankingLabelColorTransformer extends RankingElementColorTransformer {

    @Override
    public void transform(Element element, float rankingValue) {
        Color color = linearGradient.getValue(rankingValue);
        element.getTextProperties().setColor(color);
    }
}
