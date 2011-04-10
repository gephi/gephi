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

import java.io.IOException;
import java.io.InputStream;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.spigot.plugin.email.spi.EmailFilter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yi Du
 */
@ServiceProvider(service = EmailFilter.class)
public class EmailFilterMessageInclude implements EmailFilter {

    @Override
    public boolean filterEmail(Message message, String filter, Report report) {
        try {
            if (getMailContent(message).contains(filter)) {
                return true;
            } else {
                return false;
            }
        } catch (MessagingException ex) {
            report.logIssue(new Issue("message include("+message+"):"+ex.getMessage(), Issue.Level.WARNING));
            return true;
        } catch (IOException ex) {
            report.logIssue(new Issue("message include("+message+"):"+ex.getMessage(), Issue.Level.WARNING));
            return true;
        } catch(Exception ex){
            report.logIssue(new Issue("message include("+message+"):"+ex.getMessage(), Issue.Level.WARNING));
            return true;
        }
    }

    /**
     *
     * @param part
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    public String getMailContent(Part part) throws MessagingException, IOException,Exception {
        StringBuffer bodytext = new StringBuffer();

        String contenttype = part.getContentType();
        int nameindex = contenttype.indexOf("name");
        boolean conname = false;
        if (nameindex != -1) {
            conname = true;
        }
//        System.err.println("CONTENTTYPE: " + contenttype);
        if (part.isMimeType("text/plain") && !conname) {
            bodytext.append((String) part.getContent());
//            System.err.println("CONTENT PLAIN:"+bodytext);
        } else if (part.isMimeType("text/html") && !conname) {
            bodytext.append((String) part.getContent());
//            System.err.println("CONTENT html:"+bodytext);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                bodytext.append(getMailContent(multipart.getBodyPart(i)));
            }
//            System.err.println("CONTENT multi:"+bodytext);
        } else if (part.isMimeType("message/rfc822")) {//TODO codec ?
            bodytext.append(getMailContent((Part) part.getContent()));
//            System.err.println("CONTENT rcf:"+bodytext);
        } else {
            Object o = part.getContent();
            if (o instanceof String) {
                bodytext.append(o.toString());
//                System.err.println("CONTENT string:"+bodytext);
            } else if (o instanceof InputStream) {
                InputStream is = (InputStream) o;
                int c;
                while ((c = is.read()) != -1) {
                    bodytext.append(c);
                }
//                System.err.println("CONTENT stream:"+bodytext);
            } else {
                bodytext.append(o.toString());
//                System.err.println("CONTENT unknown:"+bodytext);
            }
	}

        return bodytext.toString();
    }

    @Override
    public String getFilterType() {
        return EmailDataType.FILTER_message;
    }
}
