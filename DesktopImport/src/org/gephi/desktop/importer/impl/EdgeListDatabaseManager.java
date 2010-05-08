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
package org.gephi.desktop.importer.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.gephi.io.importer.api.Database;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;
import org.gephi.io.importer.api.EdgeListDatabase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andre Panisson
 */
@ServiceProvider(service = EdgeListDatabaseManager.class)
public class EdgeListDatabaseManager {

    private FileObject databaseConfigurations;
    private List<Database> edgeListDatabases = new ArrayList<Database>();
//    private Map<String, EdgeListDatabase> nameToInstance = new HashMap<String, EdgeListDatabase>();

    public EdgeListDatabaseManager() {
        load();
    }

    public Collection<Database> getEdgeListDatabases() {
        return edgeListDatabases;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<String>();
        for (Database db: edgeListDatabases) {
            names.add(db.getName());
        }
        return names;
    }

    public void addDatabase(EdgeListDatabase db) {
        edgeListDatabases.add(db);
    }

    public boolean removeDatabase(EdgeListDatabase db) {
        return edgeListDatabases.remove(db);
    }

    public void persist() {
        doPersist();
    }

    private void load() {
       if (databaseConfigurations == null) {
            databaseConfigurations =
                FileUtil.getConfigFile("EdgeListDatabase");
        }

        if (databaseConfigurations != null) {
            InputStream is = null;

            try {
                is = databaseConfigurations.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                List<Database> unserialized =
                        (List<Database>) ois.readObject();
                if (unserialized!=null)
                    edgeListDatabases = unserialized;

            } catch (EOFException eofe) {
                // Empty configuration: do nothing
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (is != null)
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
            }
        }
    }

    private void doPersist() {
        FileLock lock = null;
        ObjectOutputStream ois = null;

        try {
            if (databaseConfigurations != null)
                databaseConfigurations.delete();

            databaseConfigurations = FileUtil.getConfigRoot().createData("EdgeListDatabase");
            lock = databaseConfigurations.lock();

            ois = new ObjectOutputStream(databaseConfigurations.getOutputStream(lock));
            ois.writeObject(edgeListDatabases);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {}
            }
            if (lock != null)
                lock.releaseLock();
        }
    }

}
