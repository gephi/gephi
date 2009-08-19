package org.gephi.preview.api;

import java.util.Iterator;

/**
 *
 * @author jeremy
 */
public interface Edge {

    public float getThickness();

    public Color getColor();

    public boolean isCurved(Customizer m_customizer);

    public boolean showArrows(Customizer m_customizer);

    public Iterator<EdgeArrow> getArrows();

    public boolean showLabel(Customizer m_customizer);

    public EdgeLabel getLabel();

    public boolean showMiniLabels(Customizer m_customizer);

    public Iterator<EdgeMiniLabel> getMiniLabels();

    public Node getNode1();

    public Node getNode2();

    public Iterator<CubicBezierCurve> getCurves();

}
