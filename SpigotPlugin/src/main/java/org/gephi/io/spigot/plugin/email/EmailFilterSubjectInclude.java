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
