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
package org.gephi.ui.ranking;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.gephi.ranking.RankingController;
import org.gephi.ranking.RankingResult;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Mathieu Bastian
 */
public class ResultListPanel extends JScrollPane implements LookupListener {

    private Lookup.Result<RankingResult> result;

    public ResultListPanel() {
        RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
        Lookup eventBus = rankingController.getEventBus();
        result = eventBus.lookupResult(RankingResult.class);
        result.addLookupListener(this);
    }

    public void resultChanged(LookupEvent ev) {
        Lookup.Result<RankingResult> r = (Lookup.Result<RankingResult>) ev.getSource();
        RankingResult[] res = r.allInstances().toArray(new RankingResult[0]);
        if (res.length > 0) {
            RankingResult lastResult = res[0];
            System.out.println(lastResult.getResults().length + " results arrived");
        }
    }
}
