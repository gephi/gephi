package org.gephi.preview.color;

import org.gephi.preview.util.Holder;

/**
 *
 * @author jeremy
 */
public class InheritedColor extends Color {

    private final Holder<Color> m_colorHolder;

    public InheritedColor(Holder<Color> colorHolder) {
        m_colorHolder = colorHolder;
    }

    @Override
    public int getRed() {
        return m_colorHolder.getComponent().getRed();
    }

    @Override
    public int getGreen() {
        return m_colorHolder.getComponent().getGreen();
    }

    @Override
    public int getBlue() {
        return m_colorHolder.getComponent().getBlue();
    }

    @Override
    public String toHexString() {
        return m_colorHolder.getComponent().toHexString();
    }
}
