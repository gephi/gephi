package org.gephi.preview.color;

import org.gephi.preview.api.color.Color;
import org.gephi.preview.util.HolderImpl;

/**
 *
 * @author jeremy
 */
public class MixedColor implements Color {

    private final HolderImpl<Color> m_colorHolder1;
    private final HolderImpl<Color> m_colorHolder2;

    public MixedColor(HolderImpl<Color> colorHolder1, HolderImpl<Color> colorHolder2) {
        m_colorHolder1 = colorHolder1;
        m_colorHolder2 = colorHolder2;
    }

    @Override
    public Integer getRed() {
        Color c1 = m_colorHolder1.getComponent();
        Color c2 = m_colorHolder2.getComponent();
        return (c1.getRed() + c2.getRed()) / 2;
    }

    @Override
    public Integer getGreen() {
        Color c1 = m_colorHolder1.getComponent();
        Color c2 = m_colorHolder2.getComponent();
        return (c1.getGreen() + c2.getGreen()) / 2;
    }

    @Override
    public Integer getBlue() {
        Color c1 = m_colorHolder1.getComponent();
        Color c2 = m_colorHolder2.getComponent();
        return (c1.getBlue() + c2.getBlue()) / 2;
    }

    @Override
    public String toHexString() {
        Color c = new SimpleColor(getRed(), getGreen(), getBlue());
        return c.toHexString();
    }

}
