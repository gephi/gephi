package org.gephi.preview.color;

import org.gephi.preview.util.Holder;

/**
 *
 * @author jeremy
 */
public class MixedColor extends Color {

    private final Holder<Color> m_colorHolder1;
    private final Holder<Color> m_colorHolder2;

    public MixedColor(Holder<Color> colorHolder1, Holder<Color> colorHolder2) {
        m_colorHolder1 = colorHolder1;
        m_colorHolder2 = colorHolder2;
    }

    @Override
    public int getRed() {
        Color c1 = m_colorHolder1.getComponent();
        Color c2 = m_colorHolder2.getComponent();
        return (int) ((c1.getRed() + c2.getRed()) / 2);
    }

    @Override
    public int getGreen() {
        Color c1 = m_colorHolder1.getComponent();
        Color c2 = m_colorHolder2.getComponent();
        return (int) ((c1.getGreen() + c2.getGreen()) / 2);
    }

    @Override
    public int getBlue() {
        Color c1 = m_colorHolder1.getComponent();
        Color c2 = m_colorHolder2.getComponent();
        return (int) ((c1.getBlue() + c2.getBlue()) / 2);
    }

    @Override
    public String toHexString() {
        Color c = new SimpleColor(getRed(), getGreen(), getBlue());
        return c.toHexString();
    }

}
