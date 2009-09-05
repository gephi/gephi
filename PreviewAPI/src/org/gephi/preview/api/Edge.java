package org.gephi.preview.api;

import org.gephi.preview.api.color.Color;
import java.util.Iterator;

/**
 *
 * @author jeremy
 */
public interface Edge {

    public float getThickness();

    public Color getColor();

    public boolean isCurved(GraphCustomizer m_customizer);

    public boolean showArrows(GraphCustomizer m_customizer);

    public Iterator<EdgeArrow> getArrows();

    public boolean showLabel(GraphCustomizer m_customizer);

    public EdgeLabel getLabel();

    public boolean showMiniLabels(GraphCustomizer m_customizer);

    public Iterator<EdgeMiniLabel> getMiniLabels();

    public Node getNode1();

    public Node getNode2();

    public Iterator<CubicBezierCurve> getCurves();

}
