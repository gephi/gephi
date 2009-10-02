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
package org.gephi.desktop.ranking;

import org.gephi.ranking.*;
import org.gephi.workspace.api.WorkspaceDataKey;
import org.gephi.workspace.api.WorkspaceDataProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingUIWorkspaceDataProvider implements WorkspaceDataProvider<RankingUIModel> {

    private WorkspaceDataKey<RankingUIModel> key;

    public Element writeXML(Document document) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RankingUIModel readXML(Element element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isPersistent() {
        return true;
    }

    public String getName() {
        return "RankingUI";
    }

    public RankingUIModel getDefaultData() {
        return new RankingUIModel();
    }

    public void setWorkspaceDataKey(WorkspaceDataKey<RankingUIModel> key) {
        this.key = key;
    }

    public WorkspaceDataKey<RankingUIModel> getWorkspaceDataKey() {
        return key;
    }
}
