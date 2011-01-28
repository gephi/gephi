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

import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *
 * @author Mathieu Bastian
 */
class JRichTooltipPanel extends JPanel {

    protected RichTooltip tooltipInfo;
    /**
     * @see #getUIClassID
     */
    public static final String uiClassID = "RichTooltipPanelUI";

    public JRichTooltipPanel(RichTooltip tooltipInfo) {
        this.tooltipInfo = tooltipInfo;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JPanel#getUI()
     */
    @Override
    public RichTooltipPanelUI getUI() {
        return (RichTooltipPanelUI) ui;
    }

    /**
     * Sets the look and feel (L&F) object that renders this component.
     *
     * @param ui
     *            The UI delegate.
     */
    protected void setUI(RichTooltipPanelUI ui) {
        super.setUI(ui);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JPanel#getUIClassID()
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JPanel#updateUI()
     */
    @Override
    public void updateUI() {
        if (UIManager.get(getUIClassID()) != null) {
            setUI((RichTooltipPanelUI) UIManager.getUI(this));
        } else {
            setUI(BasicRichTooltipPanelUI.createUI(this));
        }
    }

    public RichTooltip getTooltipInfo() {
        return tooltipInfo;
    }
}

