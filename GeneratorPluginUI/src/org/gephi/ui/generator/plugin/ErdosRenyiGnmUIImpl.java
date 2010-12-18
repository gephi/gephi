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
import org.gephi.io.generator.plugin.ErdosRenyiGnm;
import org.gephi.io.generator.plugin.ErdosRenyiGnmUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = ErdosRenyiGnmUI.class)
public class ErdosRenyiGnmUIImpl implements ErdosRenyiGnmUI {
	private ErdosRenyiGnmPanel panel;
	private ErdosRenyiGnm erdosRenyiGnm;

	public ErdosRenyiGnmUIImpl() { }

	public JPanel getPanel() {
		if (panel == null)
			panel = new ErdosRenyiGnmPanel();
		return ErdosRenyiGnmPanel.createValidationPanel(panel);
	}

	public void setup(Generator generator) {
		this.erdosRenyiGnm = (ErdosRenyiGnm)generator;

		if (panel == null)
			panel = new ErdosRenyiGnmPanel();

		panel.nField.setText(String.valueOf(erdosRenyiGnm.getn()));
		panel.mField.setText(String.valueOf(erdosRenyiGnm.getm()));
	}

	public void unsetup() {
		erdosRenyiGnm.setn(Integer.parseInt(panel.nField.getText()));
		erdosRenyiGnm.setm(Integer.parseInt(panel.mField.getText()));
		panel = null;
	}
}
