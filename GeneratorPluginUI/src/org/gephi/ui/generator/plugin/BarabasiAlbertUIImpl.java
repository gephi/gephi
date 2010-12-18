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
import org.gephi.io.generator.plugin.BarabasiAlbert;
import org.gephi.io.generator.plugin.BarabasiAlbertUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = BarabasiAlbertUI.class)
public class BarabasiAlbertUIImpl implements BarabasiAlbertUI {
	private BarabasiAlbertPanel panel;
	private BarabasiAlbert barabasiAlbert;

	public BarabasiAlbertUIImpl() { }

	public JPanel getPanel() {
		if (panel == null)
			panel = new BarabasiAlbertPanel();
		return BarabasiAlbertPanel.createValidationPanel(panel);
	}

	public void setup(Generator generator) {
		this.barabasiAlbert = (BarabasiAlbert)generator;

		if (panel == null)
			panel = new BarabasiAlbertPanel();

		panel.NField.setText(String.valueOf(barabasiAlbert.getN()));
		panel.m0Field.setText(String.valueOf(barabasiAlbert.getm0()));
		panel.MField.setText(String.valueOf(barabasiAlbert.getM()));
	}

	public void unsetup() {
		barabasiAlbert.setN(Integer.parseInt(panel.NField.getText()));
		barabasiAlbert.setm0(Integer.parseInt(panel.m0Field.getText()));
		barabasiAlbert.setM(Integer.parseInt(panel.MField.getText()));
		panel = null;
	}
}
