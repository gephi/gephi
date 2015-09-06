/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.ranking;

import java.util.Timer;
import java.util.TimerTask;
import org.gephi.graph.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.ranking.api.RankingEvent;
import org.openide.util.Lookup;

/**
 *
 * @author mbastian
 */
public class ColumnObserver extends TimerTask {

    private static final int INTERVAL = 1000;
    private final Timer timer;
    private final RankingModelImpl model;
    private final AttributeModel attributeModel;
    //Hashcodes
    private int nodeTableHash;
    private int edgeTableHash;

    public ColumnObserver(RankingModelImpl rankingModel) {
        timer = new Timer("RankingColumnObserver", true);
        model = rankingModel;

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        attributeModel = graphController.getAttributeModel(rankingModel.getWorkspace());
        nodeTableHash = attributeModel.getNodeTable().hashCode();
        edgeTableHash = attributeModel.getEdgeTable().hashCode();
    }

    @Override
    public void run() {
        boolean changed = false;
        int nodeHash = attributeModel.getNodeTable().hashCode();
        if (nodeHash != nodeTableHash) {
            changed = true;
        }
        nodeTableHash = nodeHash;

        int edgeHash = attributeModel.getEdgeTable().hashCode();
        if (edgeHash != edgeTableHash) {
            changed = true;
        }
        edgeTableHash = edgeHash;

        if (changed) {
            RankingEvent rankingEvent = new RankingEventImpl(RankingEvent.EventType.REFRESH_RANKING, model);
            model.fireRankingListener(rankingEvent);
        }
    }

    public void start() {
        timer.schedule(this, INTERVAL, INTERVAL);
    }

    public void stop() {
        timer.cancel();
    }
}
