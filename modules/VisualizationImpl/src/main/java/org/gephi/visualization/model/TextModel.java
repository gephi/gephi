/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.model;

import org.gephi.graph.api.ElementProperties;

/**
 *
 * @author mbastian
 */
public interface TextModel {

    boolean hasCustomTextColor();

    void setText(String text);

    float getTextWidth();

    float getTextHeight();

    String getText();

    float getTextSize();

    float getTextR();

    float getTextG();

    float getTextB();

    float getTextAlpha();

    boolean isTextVisible();

    ElementProperties getElementProperties();
}
