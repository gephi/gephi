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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.io.importer.api.Database;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andre Panisson
 */
@ServiceProvider(service = DatabaseManager.class)
public class DatabaseManager {

    private static Logger logger =  Logger.getLogger(DatabaseManager.class.getName());

    private FileObject databaseConfigurations;
    private List<Database> databases = new ArrayList<Database>();

    public DatabaseManager() {
        load();
    }

    public Collection<Database> getDatabases() {
        return databases;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<String>();
        for (Database db: databases) {
            names.add(db.getName());
        }
        return names;
    }

    public void addDatabase(Database db) {
        databases.add(db);
    }

    public boolean removeDatabase(Database db) {
        return databases.remove(db);
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
                    databases = unserialized;

            } catch (EOFException eofe) {
                // Empty configuration: do nothing
            } catch (IOException e) {
                logger.log(Level.WARNING, "Exception loading database configuration", e);
            } catch (ClassNotFoundException e) {
                logger.log(Level.WARNING, "Exception loading database configuration", e);
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
            ois.writeObject(databases);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error persisting database configuration", e);
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
