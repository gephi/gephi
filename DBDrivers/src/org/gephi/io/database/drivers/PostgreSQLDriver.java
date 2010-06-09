/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.database.drivers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Mathieu Bastian
 */
public class PostgreSQLDriver implements SQLDriver {

    @Override
    public Connection getConnection(String connectionUrl, String username, String passwd) throws SQLException {
        return DriverManager.getConnection(connectionUrl, username, passwd);
    }

    @Override
    public String getPrefix() {
        return "postgresql";
    }

    @Override
    public String toString() {
        return "PostgreSQL";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PostgreSQLDriver) {
            return ((PostgreSQLDriver) obj).getPrefix().equals(getPrefix());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getPrefix().hashCode();
    }
}
