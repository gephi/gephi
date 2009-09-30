package org.gephi.preview.api.color;

/**
 *
 * @author jeremy
 */
public interface Color {

    public Integer getRed();

    public Integer getGreen();

    public Integer getBlue();

    public String toHexString();
}
