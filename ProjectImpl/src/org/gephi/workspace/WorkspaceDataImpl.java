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

import org.gephi.workspace.api.WorkspaceData;
import org.gephi.workspace.api.WorkspaceDataController;
import org.gephi.workspace.api.WorkspaceDataKey;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class WorkspaceDataImpl implements WorkspaceData {

    private Object[] data;

    public WorkspaceDataImpl() {
        WorkspaceDataController wdc = Lookup.getDefault().lookup(WorkspaceDataController.class);
        data = wdc.getDefaultData();
    }

    public Object getData(String key) {
        WorkspaceDataController wdc = Lookup.getDefault().lookup(WorkspaceDataController.class);
        WorkspaceDataKey dataKey = wdc.getKey(key);
        if (dataKey != null) {
            return getData(dataKey);
        }
        return null;
    }

    public void setData(String key, Object object) {
        WorkspaceDataController wdc = Lookup.getDefault().lookup(WorkspaceDataController.class);
        WorkspaceDataKey dataKey = wdc.getKey(key);
        if (dataKey != null) {
            setData(key, object);
        }
    }

    public <T> T getData(WorkspaceDataKey<T> key) {
        if (key.getIndex() < data.length) {
            Object obj = data[key.getIndex()];
            return (T) obj;
        }
        return null;
    }

    public <T> void setData(WorkspaceDataKey<T> key, T data) {
        if (key.getIndex() < this.data.length) {
            this.data[key.getIndex()] = data;
        }
    }
}
