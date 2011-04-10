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

import java.util.StringTokenizer;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.spigot.plugin.email.spi.EmailFilter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yi Du
 */

@ServiceProvider(service = EmailFilter.class)
public class EmailFilterAddressFrom implements EmailFilter{

    @Override
    public boolean filterEmail(Message message, String filter,Report report) {
        //filter mail address, contains from, to ,cc ,bcc
        StringTokenizer token;
        String splitString = Character.toString(EmailDataType.SPLIT_CHAR);
        //filter from email address
        InternetAddress curMsg = null;
        try {
            Address[] addresses = message.getFrom();
            if(addresses == null){
                report.logIssue(new Issue("Can't get the from address of message "+message+",ignore this filter", Issue.Level.INFO));
                return true;
            }
            if(addresses.length == 0)
                return true;
            curMsg = (InternetAddress) message.getFrom()[0];
        } catch (MessagingException ex) {
           report.logIssue(new Issue(ex.getMessage(), Issue.Level.WARNING));
           return true;
        }
        token = new StringTokenizer(filter, splitString);
        while (token.hasMoreTokens()) {
            String temp = token.nextToken();
            if(!temp.isEmpty()&&
                    curMsg != null &&
                    curMsg.getAddress() != null &&
                    curMsg.getAddress().toLowerCase().matches(temp.trim().toLowerCase())){
                return true;
            }
        }
        return false;
    }

    @Override
    public String getFilterType() {
        return EmailDataType.FILTER_EMAIL_ADDRESS_FROM;
    }
    
}
