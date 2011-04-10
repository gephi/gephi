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

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.spigot.plugin.email.spi.EmailFilter;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class filter email for whether an attachment has
 * @author Yi Du
 */
@ServiceProvider(service = EmailFilter.class)
public class EmailFilterHasAttachment implements EmailFilter{

    @Override
    public boolean filterEmail(Message message, String filter, Report report) {
        try {
            //condition is message contains an atachment
            if (Boolean.parseBoolean(filter)) {
                if (isContainAttach(message)) {
                    return true;
                } else {
                    return false;
                }
            }
            //condition is message doesn't contain an atachment
            else {
                if (isContainAttach(message)) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            report.logIssue(new Issue(e.getMessage(), Issue.Level.WARNING));
            return true;
        }
    }


    /**
     * @param part
     * @return true if contains attachment
     * @throws Exception
     */
    private boolean isContainAttach(Part part) throws Exception {
        boolean attachflag = false;
//        String contentType = part.getContentType();
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mpart = mp.getBodyPart(i);
                String disposition = mpart.getDisposition();
                if ((disposition != null)
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE)))) {
                    attachflag = true;
                } else if (mpart.isMimeType("multipart/*")) {
                    attachflag = isContainAttach((Part) mpart);
                } else {
                    String contype = mpart.getContentType();
                    if (contype.toLowerCase().indexOf("application") != -1) {
                        attachflag = true;
                    }
                    if (contype.toLowerCase().indexOf("name") != -1) {
                        attachflag = true;
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {//TODO codec!
            attachflag = isContainAttach((Part) part.getContent());
        }
        return attachflag;
    }

    @Override
    public String getFilterType() {
        return EmailDataType.FILTER_ATTACHMENT;
    }
}
