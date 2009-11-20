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
package org.gephi.io.database.standard;

import org.gephi.ui.database.standard.EdgeListDatabaseUI;
import org.gephi.io.database.Database;
import org.gephi.io.database.DatabaseType;
import org.gephi.io.database.EdgeListDatabase;
import org.gephi.ui.database.DatabaseTypeUI;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = DatabaseType.class)
public class EdgeList implements DatabaseType {

    public String getName() {
        return "Edge List...";
    }

    public DatabaseTypeUI getUI() {
        return Lookup.getDefault().lookup(EdgeListDatabaseUI.class);
    }

    public Class<? extends Database> getDatabaseClass() {
        return EdgeListDatabase.class;
    }

    public Database createDatabase() {
        return new EdgeListDatabaseImpl();
    }
}
