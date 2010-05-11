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

import org.gephi.io.database.drivers.SQLDriver;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractDatabase implements Database {

    //Database attributes
    protected String name;
    protected SQLDriver SQLDriver;
    protected String host;
    protected int port;
    protected String username;
    protected String passwd;
    protected String DBName;

    //PropertiesAssociations
    protected PropertiesAssociations properties = new PropertiesAssociations();

    public String getDBName() {
        return DBName;
    }

    public void setDBName(String DBName) {
        this.DBName = DBName;
    }

    public SQLDriver getSQLDriver() {
        return SQLDriver;
    }

    public void setSQLDriver(SQLDriver SQLDriver) {
        this.SQLDriver = SQLDriver;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PropertiesAssociations getPropertiesAssociations() {
        return properties;
    }
}
