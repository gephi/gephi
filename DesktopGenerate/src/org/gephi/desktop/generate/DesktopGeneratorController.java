/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.generate;

import org.gephi.io.container.Container;
import org.gephi.io.generator.Generator;
import org.gephi.io.generator.GeneratorController;
import org.gephi.io.processor.Processor;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DesktopGeneratorController implements GeneratorController {

    public Generator[] getGenerators() {
        return Lookup.getDefault().lookupAll(Generator.class).toArray(new Generator[0]);
    }

    public void generate(Generator generator) {

        Container container = Lookup.getDefault().lookup(Container.class);
        container.setSource("" + generator.getName());
        generator.generate(container.getLoader());

        Lookup.getDefault().lookup(Processor.class).process(container.getUnloader());
    }
}
