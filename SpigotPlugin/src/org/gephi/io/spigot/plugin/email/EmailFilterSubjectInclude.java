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

import java.io.UnsupportedEncodingException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.spigot.plugin.email.spi.EmailFilter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yi Du
 */
@ServiceProvider(service = EmailFilter.class)
public class EmailFilterSubjectInclude implements EmailFilter{

    @Override
    public boolean filterEmail(Message message, String filter, Report report) {
        try {
            String subject = getSubject(message);
            if(subject == null){
                report.logIssue(new Issue("can't load subject of message "+ message, Issue.Level.WARNING));
                return true;
            }
            if (subject.contains(filter)) {
                return true;
            } else {
                return false;
            }
        } catch (MessagingException ex) {
            report.logIssue(new Issue(message+":"+ex.getMessage(), Issue.Level.WARNING));
            return true;
        } catch (UnsupportedEncodingException ex) {
            report.logIssue(new Issue(message+":"+ex.getMessage(), Issue.Level.WARNING));
            return true;
        }
    }

    /**
     * get subject of a message
     * @param message
     * @return subject of a message
     * @throws MessagingException
     */
    public String getSubject(Message message) throws MessagingException, UnsupportedEncodingException {
        if(message == null)
            return null;
        String subject = "";
        String msgSubject = message.getSubject();
        if(msgSubject == null)
            return null;
        subject = MimeUtility.decodeText(msgSubject);
        if (subject == null) {
            subject = "";
        }
        return subject;
    }

    @Override
    public String getFilterType() {
        return EmailDataType.FILTER_SUBJECT;
    }
}
