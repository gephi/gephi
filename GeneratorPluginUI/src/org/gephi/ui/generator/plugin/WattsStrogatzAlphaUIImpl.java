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
import org.gephi.io.generator.plugin.WattsStrogatzAlpha;
import org.gephi.io.generator.plugin.WattsStrogatzAlphaUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = WattsStrogatzAlphaUI.class)
public class WattsStrogatzAlphaUIImpl implements WattsStrogatzAlphaUI {
	private WattsStrogatzAlphaPanel panel;
	private WattsStrogatzAlpha wattsStrogatzAlpha;

	public WattsStrogatzAlphaUIImpl() { }

	public JPanel getPanel() {
		if (panel == null)
			panel = new WattsStrogatzAlphaPanel();
		return WattsStrogatzAlphaPanel.createValidationPanel(panel);
	}

	public void setup(Generator generator) {
		this.wattsStrogatzAlpha = (WattsStrogatzAlpha)generator;

		if (panel == null)
			panel = new WattsStrogatzAlphaPanel();

		panel.nField.setText(String.valueOf(wattsStrogatzAlpha.getn()));
		panel.kField.setText(String.valueOf(wattsStrogatzAlpha.getk()));
		panel.alphaField.setText(String.valueOf(wattsStrogatzAlpha.getalpha()));
	}

	public void unsetup() {
		wattsStrogatzAlpha.setn(Integer.parseInt(panel.nField.getText()));
		wattsStrogatzAlpha.setk(Integer.parseInt(panel.kField.getText()));
		wattsStrogatzAlpha.setalpha(Double.parseDouble(panel.alphaField.getText()));
		panel = null;
	}
}
