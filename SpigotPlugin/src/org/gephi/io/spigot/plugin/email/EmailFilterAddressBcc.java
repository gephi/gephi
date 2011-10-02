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
public class EmailFilterAddressBcc implements EmailFilter {

    @Override
    public boolean filterEmail(Message message, String filter,Report report) {
        //filter mail address, contains from, to ,cc ,bcc
        StringTokenizer token;
        String splitString = Character.toString(EmailDataType.SPLIT_CHAR);
        //filter cc email address
        Address[] addresses = null;
        try {
            addresses = message.getRecipients(Message.RecipientType.BCC);
        } catch (MessagingException ex) {
            report.logIssue(new Issue(ex.getMessage(), Issue.Level.WARNING));
            return true;
        }
        if(addresses == null){
            report.logIssue(new Issue("Can't get the bcc address of message "+message+",ignore this filter", Issue.Level.INFO));
            return true;
        }
        if(addresses.length == 0)
            return true;
        
        for (Address addr : addresses) {
            InternetAddress currentAddr = (InternetAddress) addr;
            token = new StringTokenizer(filter, splitString);
            while (token.hasMoreTokens()) {
                String temp = token.nextToken();
                if(!temp.isEmpty()&&
                    currentAddr != null &&
                    currentAddr.getAddress() != null &&
                    currentAddr.getAddress().toLowerCase().matches(temp.trim().toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getFilterType() {
        return EmailDataType.FILTER_EMAIL_ADDRESS_BCC;
    }
}

