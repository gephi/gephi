/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
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
package org.gephi.preview.propertyeditors;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.GroupLayout;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.gephi.preview.api.Colorizer;
import org.gephi.preview.api.ColorizerFactory;

/**
 *
 * @author jeremy
 */
abstract class ColorModePanelDecorator extends ColorModePanel {

    protected final ColorModePanel decoratedPanel;
    protected final ColorizerFactory factory;

    public ColorModePanelDecorator(AbstractColorizerPropertyEditor propertyEditor, ColorModePanel decoratedPanel) {
        super(propertyEditor);
        this.decoratedPanel = decoratedPanel;
        factory = propertyEditor.getColorizerFactory();

        setPanelContent();
    }

    @Override
    public void addRadioButton(JRadioButton radioButton) {
        decoratedPanel.addRadioButton(radioButton);
    }

    protected void setPanelContent() {
        // radio button
        JRadioButton radioButton = new JRadioButton();
        radioButton.setText(getRadioButtonLabel());
        addRadioButton(radioButton);

        // initialization
        radioButton.setSelected(isSelectedRadioButton());

        // listener
        radioButton.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    propertyEditor.setValue(createColorizer());
                }
            }
        });

        // panel layout
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup().
                addGroup(layout.createParallelGroup().
                addComponent(radioButton).
                addComponent(decoratedPanel)));
        layout.setVerticalGroup(layout.createSequentialGroup().
                addComponent(radioButton).
                addPreferredGap(ComponentPlacement.RELATED).
                addComponent(decoratedPanel));
    }

    protected abstract String getRadioButtonLabel();

    protected abstract boolean isSelectedRadioButton();

    protected abstract Colorizer createColorizer();
}
