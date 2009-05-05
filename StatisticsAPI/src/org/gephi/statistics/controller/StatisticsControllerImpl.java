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
package org.gephi.statistics.controller;

import java.util.ArrayList;
import org.gephi.statistics.api.*;
import java.util.List;
import org.gephi.data.network.api.DhnsController;
import org.gephi.data.network.api.SyncReader;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class StatisticsControllerImpl implements StatisticsController {

    private List<Statistics> statistics;

    public StatisticsControllerImpl() {
        statistics = new ArrayList<Statistics>(Lookup.getDefault().lookupAll(Statistics.class));
    }

    public void execute(Statistics statistics) {
        DhnsController dhnsController = Lookup.getDefault().lookup(DhnsController.class);

        SyncReader reader = dhnsController.getSyncReader();

        reader.lock();
        statistics.execute(reader);
        reader.unlock();
    }

    public List<Statistics> getStatistics() {
        return statistics;
    }
}
