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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.spigot.plugin.email.spi.EmailFilter;
import org.openide.util.lookup.ServiceProvider;

/**
 * whether an email have Cc
 * @author Yi Du
 */
@ServiceProvider(service = EmailFilter.class)
public class EmailFilterHasCc implements EmailFilter {

    @Override
    public boolean filterEmail(Message message, String filter, Report report) {
        Address[] address = null;
        try {
            address = message.getRecipients(Message.RecipientType.CC);
        } catch (MessagingException ex) {
            report.logIssue(new Issue(ex.getMessage(), Issue.Level.WARNING));
            return true;
        }
        if (Boolean.parseBoolean(filter)) {
            //condition is the message has cc
            if (address == null ||address.length == 0) {
                return false;
            } else {
                return true;
            }
        } else {
            //condition is the message doesn't have cc
            if (address == null ||address.length == 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public String getFilterType() {
        return EmailDataType.FILTER_CC;
    }
}
