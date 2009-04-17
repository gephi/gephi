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
import org.gephi.data.network.api.FlatImporter;
import org.gephi.data.network.api.FreeModifier;
import org.gephi.data.network.api.AsyncReader;
import org.gephi.data.network.api.DhnsController;
import org.gephi.data.network.api.SightManager;
import org.gephi.graph.api.Sight;
import org.gephi.data.network.api.SyncReader;
import org.gephi.data.network.reader.AsyncReaderImpl;
import org.gephi.data.network.reader.SyncReaderImpl;
import org.gephi.data.network.sight.SightImpl;
import org.gephi.data.network.tree.importer.FlatImporterImpl;


/**
 *
 * @author Mathieu Bastian
 */
public class DhnsControllerImpl implements DhnsController {

    //Architecture
    private Dhns dhns;


    public DhnsControllerImpl() {
        dhns = new Dhns();
    }

    public SightManager getSightManager() {
        return dhns.getSightManager();
    }

    public AsyncReader getAsyncReader(Sight sight)
    {
        return new AsyncReaderImpl((SightImpl)sight);
    }

    public SyncReader getSyncReader(Sight sight)
    {
        return new SyncReaderImpl(dhns, (SightImpl)sight);
    }

    public FreeModifier getFreeModifier()
    {
        return dhns.getFreeModifier();
    }

    public FlatImporter getFlatImport() {
        return new FlatImporterImpl(dhns);
    }
    
}
