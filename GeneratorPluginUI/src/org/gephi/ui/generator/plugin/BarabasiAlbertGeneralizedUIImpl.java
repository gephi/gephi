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
import org.gephi.io.generator.plugin.BarabasiAlbertGeneralized;
import org.gephi.io.generator.plugin.BarabasiAlbertGeneralizedUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = BarabasiAlbertGeneralizedUI.class)
public class BarabasiAlbertGeneralizedUIImpl implements BarabasiAlbertGeneralizedUI {
	private BarabasiAlbertGeneralizedPanel panel;
	private BarabasiAlbertGeneralized barabasiAlbertGeneralized;

	public BarabasiAlbertGeneralizedUIImpl() { }

	public JPanel getPanel() {
		if (panel == null)
			panel = new BarabasiAlbertGeneralizedPanel();
		return BarabasiAlbertGeneralizedPanel.createValidationPanel(panel);
	}

	public void setup(Generator generator) {
		this.barabasiAlbertGeneralized = (BarabasiAlbertGeneralized)generator;

		if (panel == null)
			panel = new BarabasiAlbertGeneralizedPanel();

		panel.NField.setText(String.valueOf(barabasiAlbertGeneralized.getN()));
		panel.m0Field.setText(String.valueOf(barabasiAlbertGeneralized.getm0()));
		panel.MField.setText(String.valueOf(barabasiAlbertGeneralized.getM()));
		panel.pField.setText(String.valueOf(barabasiAlbertGeneralized.getp()));
		panel.qField.setText(String.valueOf(barabasiAlbertGeneralized.getq()));
	}

	public void unsetup() {
		barabasiAlbertGeneralized.setN(Integer.parseInt(panel.NField.getText()));
		barabasiAlbertGeneralized.setm0(Integer.parseInt(panel.m0Field.getText()));
		barabasiAlbertGeneralized.setM(Integer.parseInt(panel.MField.getText()));
		barabasiAlbertGeneralized.setp(Double.parseDouble(panel.pField.getText()));
		barabasiAlbertGeneralized.setq(Double.parseDouble(panel.qField.getText()));
		panel = null;
	}
}
