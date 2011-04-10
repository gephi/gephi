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

package org.gephi.io.spigot.plugin.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.spigot.plugin.email.spi.EmailFilesFilter;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


/**
 * This class can parse files to MimeMessage type of java mail api
 * @author Yi Du <duyi001@gmail.com>
 */
@ServiceProvider(service=EmailFilesFilter.class)
public class EmailFilesFilterEML implements EmailFilesFilter{

    @Override
    public String getSupportedFileExtension() {
        return ".eml";
    }

    @Override
    public MimeMessage parseFile(File file, Report report) {
        InputStream is = null;
        Session s = null;
        MimeMessage message = null;
        try {
            is = new FileInputStream(file);
            s = Session.getDefaultInstance(System.getProperties(), null);
            message = new MimeMessage(s, is);
        } catch (MessagingException ex) {
            report.logIssue(new Issue(ex.getMessage(), Issue.Level.WARNING));
            return null;
        } catch (FileNotFoundException ex) {
            report.logIssue(new Issue(ex.getMessage(), Issue.Level.WARNING));
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                report.logIssue(new Issue(ex.getMessage(), Issue.Level.WARNING));
                return null;
            }
        }
        return message;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EmailFilesFilterEML.class, "EmailFilesFilterEML.displayName");
    }

}
