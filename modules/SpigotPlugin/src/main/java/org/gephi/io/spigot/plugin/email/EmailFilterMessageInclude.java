/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
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
