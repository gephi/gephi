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

import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.database.drivers.SQLUtils;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractDatabase implements Database {

    //Database attributes
    protected String name;
    protected String driver;
    protected String host;
    protected int port;
    protected String username;
    protected String passwd;
    protected String DBName;
    //PropertiesAssociations
    protected PropertiesAssociations properties = new PropertiesAssociations();

    @Override
    public String getDBName() {
        return DBName;
    }

    @Override
    public void setDBName(String DBName) {
        this.DBName = DBName;
    }

    @Override
    public SQLDriver getSQLDriver() {
        return SQLUtils.getDriver(driver);
    }

    @Override
    public void setSQLDriver(SQLDriver driver) {
        if (driver != null) {
            this.driver = driver.getPrefix();
        }
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPasswd() {
        return passwd;
    }

    @Override
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public PropertiesAssociations getPropertiesAssociations() {
        return properties;
    }
}
