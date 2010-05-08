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
package org.gephi.io.importer.api;

import java.io.Serializable;

import org.gephi.io.database.drivers.SQLDriver;

/**
 * Database description and connexion details.
 *
 * @author Mathieu Bastian
 */
public interface Database extends Serializable {

    public String getName();

    public SQLDriver getSQLDriver();

    public String getHost();

    public int getPort();

    public String getUsername();

    public String getPasswd();

    public String getDBName();

    public void setName(String name);

    public void setSQLDriver(SQLDriver driver);

    public void setHost(String host);

    public void setPort(int port);

    public void setUsername(String username);

    public void setPasswd(String passwd);

    public void setDBName(String dbName);

    public PropertiesAssociations getPropertiesAssociations();
}
