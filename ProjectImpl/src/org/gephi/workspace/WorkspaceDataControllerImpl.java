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
package org.gephi.workspace;

import java.util.HashMap;
import java.util.Map;
import org.gephi.workspace.api.WorkspaceDataController;
import org.gephi.workspace.api.WorkspaceDataKey;
import org.gephi.workspace.api.WorkspaceDataProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class WorkspaceDataControllerImpl implements WorkspaceDataController {

    private Map<String, WorkspaceDataKey> keyMap;
    private WorkspaceDataProvider[] providers;

    public WorkspaceDataControllerImpl() {
        keyMap = new HashMap<String, WorkspaceDataKey>();
        providers = Lookup.getDefault().lookupAll(WorkspaceDataProvider.class).toArray(new WorkspaceDataProvider[0]);
        for (int index = 0; index < providers.length; index++) {
            String name = providers[index].getName();
            if (name == null) {
                name = providers[index].getClass().getName();
            }
            WorkspaceDataKeyImpl key = new WorkspaceDataKeyImpl();
            key.setIndex(index);
            keyMap.put(name, key);
            providers[index].setWorkspaceDataKey(key);
        }
    }

    public Object[] getDefaultData() {
        Object[] data = new Object[providers.length];
        for (int index = 0; index < providers.length; index++) {
            data[index] = providers[index].getDefaultData();
        }
        return data;
    }

    public WorkspaceDataKey getKey(String name) {
        return keyMap.get(name);
    }
}
