/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gephi.io.spigot.plugin.email.spi;

import java.io.File;
import javax.mail.internet.MimeMessage;
import org.gephi.io.importer.api.Report;

/**
 * user who want to add more support to the current project
 * should implements this interface
 * @author Yi Du<duyi001@gmail.com>
 */
public interface EmailFilesFilter {

    /**
     * 
     * @return display name of ui.
     * It's also an ID of the file filter
     */
    public String getDisplayName();
    
    /**
     * 
     * @return supported file extension of current file filter
     */
    public String getSupportedFileExtension();

    /**
     * 
     * @param files
     * @param report
     * @return parsed files, the format is MimeMessage of java mail api
     */
    public MimeMessage parseFile(File file,Report report);

}
