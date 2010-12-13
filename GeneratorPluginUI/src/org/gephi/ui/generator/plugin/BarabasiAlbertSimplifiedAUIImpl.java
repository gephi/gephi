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
import org.gephi.io.generator.plugin.BarabasiAlbertSimplifiedA;
import org.gephi.io.generator.plugin.BarabasiAlbertSimplifiedAUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = BarabasiAlbertSimplifiedAUI.class)
public class BarabasiAlbertSimplifiedAUIImpl implements BarabasiAlbertSimplifiedAUI {
	private BarabasiAlbertSimplifiedAPanel panel;
	private BarabasiAlbertSimplifiedA barabasiAlbertSimplifiedA;

	public BarabasiAlbertSimplifiedAUIImpl() { }

	public JPanel getPanel() {
		if (panel == null)
			panel = new BarabasiAlbertSimplifiedAPanel();
		return BarabasiAlbertSimplifiedAPanel.createValidationPanel(panel);
	}

	public void setup(Generator generator) {
		this.barabasiAlbertSimplifiedA = (BarabasiAlbertSimplifiedA)generator;

		if (panel == null)
			panel = new BarabasiAlbertSimplifiedAPanel();

		panel.NField.setText(String.valueOf(barabasiAlbertSimplifiedA.getN()));
		panel.m0Field.setText(String.valueOf(barabasiAlbertSimplifiedA.getm0()));
		panel.MField.setText(String.valueOf(barabasiAlbertSimplifiedA.getM()));
	}

	public void unsetup() {
		barabasiAlbertSimplifiedA.setN(Integer.parseInt(panel.NField.getText()));
		barabasiAlbertSimplifiedA.setm0(Integer.parseInt(panel.m0Field.getText()));
		barabasiAlbertSimplifiedA.setM(Integer.parseInt(panel.MField.getText()));
		panel = null;
	}
}
