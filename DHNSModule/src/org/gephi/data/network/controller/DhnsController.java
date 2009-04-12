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
package org.gephi.data.network.controller;

import org.gephi.data.network.*;
import org.gephi.data.network.api.FreeModifier;
import org.gephi.data.network.api.Reader;
import org.gephi.data.network.api.Sight;
import org.gephi.data.network.modifier.FreeModifierImpl;
import org.gephi.data.network.reader.MainReader;

/**
 *
 * @author Mathieu Bastian
 */
public class DhnsController {

    private static DhnsController instance;

    public synchronized static DhnsController getInstance() {
        if (instance == null) {
            instance = new DhnsController();
        }
        return instance;
    }

    //Architecture
    private Dhns dhns;


    private DhnsController() {
        dhns = new Dhns();

    }

    public Sight getMainSight()
    {
        return dhns.getSightManager().getMainSight();
    }

    public Reader getReader()
    {
        return new MainReader(dhns);
    }

    public FreeModifier getFreeModifier()
    {
        return new FreeModifierImpl();
    }

    
}
