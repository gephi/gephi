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
import org.gephi.io.generator.plugin.Kleinberg;
import org.gephi.io.generator.plugin.KleinbergUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = KleinbergUI.class)
public class KleinbergUIImpl implements KleinbergUI {
	private KleinbergPanel panel;
	private Kleinberg kleinberg;

	public KleinbergUIImpl() { }

	public JPanel getPanel() {
		if (panel == null)
			panel = new KleinbergPanel();
		return KleinbergPanel.createValidationPanel(panel);
	}

	public void setup(Generator generator) {
		this.kleinberg = (Kleinberg)generator;

		if (panel == null)
			panel = new KleinbergPanel();

		panel.nField.setText(String.valueOf(kleinberg.getn()));
		panel.pField.setText(String.valueOf(kleinberg.getp()));
		panel.qField.setText(String.valueOf(kleinberg.getq()));
		panel.rField.setText(String.valueOf(kleinberg.getr()));
		panel.torusCheckBox.setSelected(kleinberg.isTorusBased());
	}

	public void unsetup() {
		kleinberg.setn(Integer.parseInt(panel.nField.getText()));
		kleinberg.setp(Integer.parseInt(panel.pField.getText()));
		kleinberg.setq(Integer.parseInt(panel.qField.getText()));
		kleinberg.setr(Integer.parseInt(panel.rField.getText()));
		kleinberg.setTorusBased(panel.torusCheckBox.isSelected());
		panel = null;
	}
}
