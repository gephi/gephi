/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

