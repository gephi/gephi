/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.WattsStrogatzBeta;
import org.gephi.io.generator.plugin.WattsStrogatzBetaUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = WattsStrogatzBetaUI.class)
public class WattsStrogatzBetaUIImpl implements WattsStrogatzBetaUI {
	private WattsStrogatzBetaPanel panel;
	private WattsStrogatzBeta wattsStrogatzBeta;

	public WattsStrogatzBetaUIImpl() { }

	public JPanel getPanel() {
		if (panel == null)
			panel = new WattsStrogatzBetaPanel();
		return WattsStrogatzBetaPanel.createValidationPanel(panel);
	}

	public void setup(Generator generator) {
		this.wattsStrogatzBeta = (WattsStrogatzBeta)generator;

		if (panel == null)
			panel = new WattsStrogatzBetaPanel();

		panel.NField.setText(String.valueOf(wattsStrogatzBeta.getN()));
		panel.KField.setText(String.valueOf(wattsStrogatzBeta.getK()));
		panel.betaField.setText(String.valueOf(wattsStrogatzBeta.getbeta()));
	}

	public void unsetup() {
		wattsStrogatzBeta.setN(Integer.parseInt(panel.NField.getText()));
		wattsStrogatzBeta.setK(Integer.parseInt(panel.KField.getText()));
		wattsStrogatzBeta.setbeta(Double.parseDouble(panel.betaField.getText()));
		panel = null;
	}
}
