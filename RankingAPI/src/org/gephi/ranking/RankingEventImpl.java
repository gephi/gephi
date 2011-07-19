/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ranking;

import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingEvent;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.api.Transformer;

/**
 * Implementation of the <code>RankingEvent</code> interface.
 * 
 * @author Mathieu Bastian
 */
public class RankingEventImpl implements RankingEvent {

    private final EventType eventType;
    private final RankingModel source;
    private final Ranking ranking;
    private final Transformer transformer;

    public RankingEventImpl(EventType eventType, RankingModel source, Ranking ranking, Transformer transformer) {
        this.eventType = eventType;
        this.source = source;
        this.ranking = ranking;
        this.transformer = transformer;
    }

    public RankingEventImpl(EventType eventType, RankingModel source) {
        this(eventType, source, null, null);
    }

    public EventType getEventType() {
        return eventType;
    }

    public RankingModel getSource() {
        return source;
    }

    public Ranking getRanking() {
        return ranking;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public boolean is(EventType... type) {
        for (EventType e : type) {
            if (e.equals(eventType)) {
                return true;
            }
        }
        return false;
    }
}
