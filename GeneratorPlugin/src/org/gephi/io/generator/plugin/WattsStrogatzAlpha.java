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
package org.gephi.io.generator.plugin;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * http://en.wikipedia.org/wiki/Watts_and_Strogatz_model
 * http://tam.cornell.edu/tam/cms/manage/upload/SS_nature_smallworld.pdf
 * http://www.bsos.umd.edu/socy/alan/stats/network-grad/summaries/Watts-Six%20Degrees-Ghosh.pdf
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = Generator.class)
public class WattsStrogatzAlpha implements Generator {
	private boolean cancel = false;
	private ProgressTicket progressTicket;

	public void generate(ContainerLoader container) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getName() {
		return "Watts-Strogatz Small World model Alpha";
	}

	public GeneratorUI getUI() {
		return Lookup.getDefault().lookup(WattsStrogatzAlphaUI.class);
	}

	public boolean cancel() {
		cancel = true;
		return true;
	}

	public void setProgressTicket(ProgressTicket progressTicket) {
		this.progressTicket = progressTicket;
	}
}
