/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.database.drivers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mbastian
 */
public class SQLiteDriver implements SQLDriver {
    
    public SQLiteDriver() {
        try {
            // load the sqlite-JDBC driver using the current class loader
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SQLiteDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public Connection getConnection(String connectionUrl, String username, String passwd) throws SQLException {
        String url = connectionUrl.substring(connectionUrl.indexOf("//") + 2);
        connectionUrl = "jdbc:sqlite:" + url.substring(0, url.length() - 2);
        return DriverManager.getConnection(connectionUrl, username, passwd);
    }
    
    @Override
    public String getPrefix() {
        return "sqlite";
    }
    
    @Override
    public String toString() {
        return "SQLite";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SQLiteDriver) {
            return ((SQLiteDriver) obj).getPrefix().equals(getPrefix());
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return getPrefix().hashCode();
    }
}
