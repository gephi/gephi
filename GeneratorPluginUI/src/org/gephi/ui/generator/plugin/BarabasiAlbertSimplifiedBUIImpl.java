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
import org.gephi.io.generator.plugin.BarabasiAlbertSimplifiedB;
import org.gephi.io.generator.plugin.BarabasiAlbertSimplifiedBUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = BarabasiAlbertSimplifiedBUI.class)
public class BarabasiAlbertSimplifiedBUIImpl implements BarabasiAlbertSimplifiedBUI {
	private BarabasiAlbertSimplifiedBPanel panel;
	private BarabasiAlbertSimplifiedB barabasiAlbertSimplifiedB;

	public BarabasiAlbertSimplifiedBUIImpl() { }

	public JPanel getPanel() {
		if (panel == null)
			panel = new BarabasiAlbertSimplifiedBPanel();
		return BarabasiAlbertSimplifiedBPanel.createValidationPanel(panel);
	}

	public void setup(Generator generator) {
		this.barabasiAlbertSimplifiedB = (BarabasiAlbertSimplifiedB)generator;

		if (panel == null)
			panel = new BarabasiAlbertSimplifiedBPanel();

		panel.NField.setText(String.valueOf(barabasiAlbertSimplifiedB.getN()));
		panel.MField.setText(String.valueOf(barabasiAlbertSimplifiedB.getM()));
	}

	public void unsetup() {
		barabasiAlbertSimplifiedB.setN(Integer.parseInt(panel.NField.getText()));
		barabasiAlbertSimplifiedB.setM(Integer.parseInt(panel.MField.getText()));
		panel = null;
	}
}
