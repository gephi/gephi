/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ui.importer.plugin;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andre Panisson
 */
public class EdgeListDatabaseManager {

    private FileObject databaseConfigurations;
    private List<Database> edgeListDatabases = new ArrayList<>();
//    private Map<String, EdgeListDatabase> nameToInstance = new HashMap<String, EdgeListDatabase>();

    public EdgeListDatabaseManager() {
        load();
    }

    public List<Database> getEdgeListDatabases() {
        return edgeListDatabases;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (Database db : edgeListDatabases) {
            names.add(db.getName());
        }
        return names;
    }

    public void addDatabase(EdgeListDatabaseImpl db) {
        edgeListDatabases.add(db);
    }

    public boolean removeDatabase(EdgeListDatabaseImpl db) {
        return edgeListDatabases.remove(db);
    }

    public void persist() {
        doPersist();
    }

    private void load() {
        if (databaseConfigurations == null) {
            databaseConfigurations
                    = FileUtil.getConfigFile("EdgeListDatabase");
        }

        if (databaseConfigurations != null) {
            InputStream is = null;

            try {
                is = databaseConfigurations.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                List<Database> unserialized = (List<Database>) ois.readObject();
                if (unserialized != null) {
                    edgeListDatabases = unserialized;
                }
            } catch (java.io.InvalidClassException e) {
            } catch (EOFException eofe) {
                // Empty configuration: do nothing
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    private void doPersist() {
        FileLock lock = null;
        ObjectOutputStream ois = null;

        try {
            if (databaseConfigurations != null) {
                databaseConfigurations.delete();
            }

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
                } catch (IOException e) {
                }
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
}
