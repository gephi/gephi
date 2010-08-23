/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.ui.components.richtooltip;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;

/*
 * Copyright (c) 2005-2009 Flamingo Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Rich tooltip for command buttons.
 *
 * <p>
 * In its most basic form, the rich tooltip has a title and one (possible
 * multiline) description text:
 * </p>
 *
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * |        Some description text   |
 * +--------------------------------+
 * </pre>
 *
 * <p>
 * The {@link #addDescriptionSection(String)} can be used to add multiple
 * sections to the description:
 * </p>
 *
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * |        First multiline         |
 * |        description section     |
 * |                                |
 * |        Second multiline        |
 * |        description section     |
 * |                                |
 * |        Third multiline         |
 * |        description section     |
 * +--------------------------------+
 * </pre>
 *
 * <p>
 * The {@link #setMainImage(Image)} can be used to place an image below the
 * title and to the left of the description sections:
 * </p>
 *
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * | *******  First multiline       |
 * | *image*  description section   |
 * | *******                        |
 * |          Second multiline      |
 * |          description section   |
 * +--------------------------------+
 * </pre>
 *
 * <p>
 * The {@link #addFooterSection(String)} can be used to add (possibly) multiple
 * footer sections that will be shown below a horizontal separator:
 * </p>
 *
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * |        First multiline         |
 * |        description section     |
 * |                                |
 * |        Second multiline        |
 * |        description section     |
 * |--------------------------------|
 * | A multiline footer section     |
 * | placed below a separator       |
 * +--------------------------------+
 * </pre>
 *
 * <p>
 * The {@link #setFooterImage(Image)} can be used to place an image to the left
 * of the footer sections:
 * </p>
 *
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * |        First multiline         |
 * |        description section     |
 * |                                |
 * |        Second multiline        |
 * |        description section     |
 * |--------------------------------|
 * | *******  A multiline           |
 * | *image*  footer section        |
 * | *******                        |
 * +--------------------------------+
 * </pre>
 *
 * <p>
 * Here is a fully fledged rich tooltip that shows all these APIs in action:
 * </p>
 *
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * | *******  First multiline       |
 * | *image*  description section   |
 * | *******                        |
 * |          Second multiline      |
 * |          description section   |
 * |--------------------------------|
 * | *******  First multiline       |
 * | *image*  footer section        |
 * | *******                        |
 * |          Second multiline      |
 * |          footer section        |
 * +--------------------------------+
 * </pre>
 *
 * @author Kirill Grouchnikov
 */
public class RichTooltip {

    /**
     * The main title of this tooltip.
     *
     * @see #RichTooltip(String, String)
     * @see #setTitle(String)
     * @see #getTitle()
     */
    protected String title;
    /**
     * The main image of this tooltip. Can be <code>null</code>.
     *
     * @see #getMainImage()
     * @see #setMainImage(Image)
     */
    protected Image mainImage;
    /**
     * The description sections of this tooltip.
     *
     * @see #RichTooltip(String, String)
     * @see #addDescriptionSection(String)
     * @see #getDescriptionSections()
     */
    protected List<String> descriptionSections;
    /**
     * The footer image of this tooltip. Can be <code>null</code>.
     *
     * @see #getFooterImage()
     * @see #setFooterImage(Image)
     */
    protected Image footerImage;
    /**
     * The footer sections of this tooltip. Can be empty.
     *
     * @see #addFooterSection(String)
     * @see #getFooterSections()
     */
    protected List<String> footerSections;

    /**
     * Creates an empty tooltip.
     */
    public RichTooltip() {
    }

    /**
     * Creates a tooltip with the specified title and description section.
     *
     * @param title
     *            Tooltip title.
     * @param descriptionSection
     *            Tooltip main description section.
     */
    public RichTooltip(String title, String descriptionSection) {
        this.setTitle(title);
        this.addDescriptionSection(descriptionSection);
    }
    private Popup tipWindow;
    private boolean tipShowing = false;

    public void showTooltip(JComponent component) {
        if (component == null || !component.isShowing()) {
            return;
        }
        Dimension size;
        Point screenLocation = component.getLocationOnScreen();
        Point location = new Point();
        GraphicsConfiguration gc;
        gc = component.getGraphicsConfiguration();
        Rectangle sBounds = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        // Take into account screen insets, decrease viewport
        sBounds.x += screenInsets.left;
        sBounds.y += screenInsets.top;
        sBounds.width -= (screenInsets.left + screenInsets.right);
        sBounds.height -= (screenInsets.top + screenInsets.bottom);

        hideTooltip();

        JRichTooltipPanel tip = new JRichTooltipPanel(this);
        size = tip.getPreferredSize();


        // display directly below or above it
        location.x = screenLocation.x;
        location.y = screenLocation.y + component.getHeight();
        if ((location.y + size.height) > (sBounds.y + sBounds.height)) {
            location.y = screenLocation.y - size.height;
        }


        // Tweak the X location to not overflow the screen
        if (location.x < sBounds.x) {
            location.x = sBounds.x;
        } else if (location.x - sBounds.x + size.width > sBounds.width) {
            location.x = sBounds.x + Math.max(0, sBounds.width - size.width);
        }

        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        tipWindow = popupFactory.getPopup(component, tip, location.x, location.y);
        tipWindow.show();
        tipShowing = true;
    }

    public void hideTooltip() {
        if (tipWindow != null) {
            tipWindow.hide();
            tipWindow = null;
            tipShowing = false;
        }
    }

    /**
     * Sets the title for this tooltip.
     *
     * @param title
     *            The new tooltip title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the main image for this tooltip.
     *
     * @param image
     *            The main image for this tooltip.
     * @see #getMainImage()
     * @see #addDescriptionSection(String)
     */
    public void setMainImage(Image image) {
        this.mainImage = image;
    }

    /**
     * Adds the specified description section to this tooltip.
     *
     * @param section
     *            The description section to add.
     * @see #getDescriptionSections()
     * @see #setMainImage(Image)
     * @see #setTitle(String)
     */
    public void addDescriptionSection(String section) {
        if (this.descriptionSections == null) {
            this.descriptionSections = new LinkedList<String>();
        }
        this.descriptionSections.add(section);
    }

    /**
     * Sets the footer image for this tooltip.
     *
     * @param image
     *            The footer image for this tooltip.
     * @see #getFooterImage()
     * @see #addFooterSection(String)
     */
    public void setFooterImage(Image image) {
        this.footerImage = image;
    }

    /**
     * Adds the specified footer section to this tooltip.
     *
     * @param section
     *            The footer section to add.
     * @see #getFooterSections()
     * @see #setFooterImage(Image)
     */
    public void addFooterSection(String section) {
        if (this.footerSections == null) {
            this.footerSections = new LinkedList<String>();
        }
        this.footerSections.add(section);
    }

    /**
     * Returns the main title of this tooltip.
     *
     * @return The main title of this tooltip.
     * @see #RichTooltip(String, String)
     * @see #setTitle(String)
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns the main image of this tooltip. Can return <code>null</code>.
     *
     * @return The main image of this tooltip.
     * @see #setMainImage(Image)
     * @see #getDescriptionSections()
     */
    public Image getMainImage() {
        return this.mainImage;
    }

    /**
     * Returns an unmodifiable list of description sections of this tooltip.
     * Guaranteed to return a non-<code>null</code> list.
     *
     * @return An unmodifiable list of description sections of this tooltip.
     * @see #RichTooltip(String, String)
     * @see #addDescriptionSection(String)
     * @see #getTitle()
     * @see #getMainImage()
     */
    public List<String> getDescriptionSections() {
        if (this.descriptionSections == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(this.descriptionSections);
    }

    /**
     * Returns the footer image of this tooltip. Can return <code>null</code>.
     *
     * @return The footer image of this tooltip.
     * @see #setFooterImage(Image)
     * @see #getFooterSections()
     */
    public Image getFooterImage() {
        return this.footerImage;
    }

    /**
     * Returns an unmodifiable list of footer sections of this tooltip.
     * Guaranteed to return a non-<code>null</code> list.
     *
     * @return An unmodifiable list of footer sections of this tooltip.
     * @see #addFooterSection(String)
     * @see #getFooterImage()
     */
    public List<String> getFooterSections() {
        if (this.footerSections == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(this.footerSections);
    }
}


