package org.gephi.preview.color;

import org.gephi.preview.api.color.Color;

/**
 *
 * @author jeremy
 */
public class SimpleColor implements Color {

    private final java.awt.Color m_color;

    public SimpleColor(int r, int g, int b) {
        m_color = new java.awt.Color(r, g, b);
    }

	public SimpleColor(float r, float g, float b) {
        m_color = new java.awt.Color(r, g, b);
    }

    public SimpleColor(int r, int g, int b, int a) {
        m_color = new java.awt.Color(r, g, b, a);
    }

	public SimpleColor(float r, float g, float b, float a) {
        m_color = new java.awt.Color(r, g, b, a);
    }

    @Override
    public Integer getRed() {
        return m_color.getRed();
    }

    @Override
    public Integer getGreen() {
        return m_color.getGreen();
    }

    @Override
    public Integer getBlue() {
        return m_color.getBlue();
    }

    @Override
    public String toHexString() {
        String str = Integer.toHexString(m_color.getRGB());

        for (int i = str.length(); i > 6; i--)
            str = str.substring(1);

        for (int i = str.length(); i < 6; i++)
            str = "0" + str;

        return "#" + str;
    }
}
