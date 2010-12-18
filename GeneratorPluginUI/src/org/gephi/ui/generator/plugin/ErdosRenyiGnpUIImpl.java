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
import org.gephi.io.generator.plugin.ErdosRenyiGnp;
import org.gephi.io.generator.plugin.ErdosRenyiGnpUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = ErdosRenyiGnpUI.class)
public class ErdosRenyiGnpUIImpl implements ErdosRenyiGnpUI {
	private ErdosRenyiGnpPanel panel;
	private ErdosRenyiGnp erdosRenyiGnp;

	public ErdosRenyiGnpUIImpl() { }

	public JPanel getPanel() {
		if (panel == null)
			panel = new ErdosRenyiGnpPanel();
		return ErdosRenyiGnpPanel.createValidationPanel(panel);
	}

	public void setup(Generator generator) {
		this.erdosRenyiGnp = (ErdosRenyiGnp)generator;

		if (panel == null)
			panel = new ErdosRenyiGnpPanel();

		panel.nField.setText(String.valueOf(erdosRenyiGnp.getn()));
		panel.pField.setText(String.valueOf(erdosRenyiGnp.getp()));
	}

	public void unsetup() {
		erdosRenyiGnp.setn(Integer.parseInt(panel.nField.getText()));
		erdosRenyiGnp.setp(Double.parseDouble(panel.pField.getText()));
		panel = null;
	}
}
