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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.Message;
import javax.mail.MessagingException;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.spigot.plugin.email.spi.EmailFilter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yi Du
 */
@ServiceProvider(service = EmailFilter.class)
public class EmailFilterDateAfter implements EmailFilter{

    @Override
    public boolean filterEmail(Message message, String filter, Report report) {
        Date receivedDate = null;
        try {
            receivedDate = message.getReceivedDate();
        } catch (MessagingException ex) {
            report.logIssue(new Issue(ex.getMessage(), Issue.Level.WARNING));
            return true;
        }
        if(receivedDate == null){
            report.logIssue(new Issue("Can't get the receive date of message "+message+",ignore this filter", Issue.Level.INFO));
            return true;
        }
        SimpleDateFormat format = new SimpleDateFormat(EmailDataType.DATEFORMAT);

        Date dateFilter = null;
        try {
            dateFilter = format.parse(filter);
        } catch (ParseException ex) {
            report.logIssue(new Issue(ex.getMessage(), Issue.Level.WARNING));
            return true;
        }
        if(receivedDate.after(dateFilter))
            return true;
        else
            return false;
    }

    @Override
    public String getFilterType() {
        return EmailDataType.FILTER_DATERANGE_AFTER;
    }

}
