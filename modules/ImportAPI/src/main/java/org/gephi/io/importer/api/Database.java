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

package org.gephi.io.importer.api;

import java.io.Serializable;
import org.gephi.io.database.drivers.SQLDriver;

/**
 * Database description and connection details.
 *
 * @author Mathieu Bastian
 */
public interface Database extends Serializable {

    /**
     * Returns the name of this database connection configuration.
     *
     * @return connection name
     */
    String getName();

    /**
     * Sets the name of this database connection configuration.
     *
     * @param name connection name
     */
    void setName(String name);

    /**
     * Returns the SQL driver used for this database connection.
     *
     * @return SQL driver
     */
    SQLDriver getSQLDriver();

    /**
     * Sets the SQL driver used for this database connection.
     *
     * @param driver SQL driver
     */
    void setSQLDriver(SQLDriver driver);

    /**
     * Returns the database server host name or IP address.
     *
     * @return host
     */
    String getHost();

    /**
     * Sets the database server host name or IP address.
     *
     * @param host host name or IP address
     */
    void setHost(String host);

    /**
     * Returns the database server port number.
     *
     * @return port number
     */
    int getPort();

    /**
     * Sets the database server port number.
     *
     * @param port port number
     */
    void setPort(int port);

    /**
     * Returns the username used for authentication.
     *
     * @return username
     */
    String getUsername();

    /**
     * Sets the username used for authentication.
     *
     * @param username username
     */
    void setUsername(String username);

    /**
     * Returns the password used for authentication.
     *
     * @return password
     */
    String getPasswd();

    /**
     * Sets the password used for authentication.
     *
     * @param passwd password
     */
    void setPasswd(String passwd);

    /**
     * Returns the name of the target database (schema) on the server.
     *
     * @return database name
     */
    String getDBName();

    /**
     * Sets the name of the target database (schema) on the server.
     *
     * @param dbName database name
     */
    void setDBName(String dbName);

    /**
     * Returns the column-to-property associations configured for this database.
     *
     * @return properties associations
     */
    PropertiesAssociations getPropertiesAssociations();
}
