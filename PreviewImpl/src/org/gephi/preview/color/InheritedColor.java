package org.gephi.preview.color;

import org.gephi.preview.api.Holder;
import org.gephi.preview.api.color.Color;

/**
 *
 * @author jeremy
 */
public class InheritedColor implements Color {

    private final Holder<Color> m_colorHolder;

    public InheritedColor(Holder<Color> colorHolder) {
        m_colorHolder = colorHolder;
    }

    @Override
    public Integer getRed() {
        return m_colorHolder.getComponent().getRed();
    }

    @Override
    public Integer getGreen() {
        return m_colorHolder.getComponent().getGreen();
    }

    @Override
    public Integer getBlue() {
        return m_colorHolder.getComponent().getBlue();
    }

    @Override
    public String toHexString() {
        return m_colorHolder.getComponent().toHexString();
    }
}
