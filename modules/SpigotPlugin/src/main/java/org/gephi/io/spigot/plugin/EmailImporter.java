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
package org.gephi.io.spigot.plugin;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.SpigotImporter;
import org.gephi.io.spigot.plugin.email.EmailDataType;
import org.gephi.io.spigot.plugin.email.Utilities;
import org.gephi.io.spigot.plugin.email.spi.EmailFilesFilter;
import org.gephi.io.spigot.plugin.email.spi.EmailFilter;
import org.gephi.io.spigot.plugin.email.spi.EmailFilterFactory;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Yi Du <duyi001@gmail.com>
 */
public class EmailImporter extends EmailDataType implements SpigotImporter, LongTask {

//    public static final String IMPORT_TYPE_EMAIL = "emails";
    private ContainerLoader container;
    private Report report;
    private boolean cancel = false;
    private ProgressTicket progress;
    //EmailDataType datatype;//TODO need to add set in the controller

    @Override
    public boolean execute(ContainerLoader loader) {
        this.container = loader;
        this.report = new Report();
        //datatype = this;

        //if(datatype == null){
//            cancel();
//            return false;
//        }
        Progress.start(progress);
        doImport();
        Progress.finish(progress);
        return !cancel;
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    private void doImport() {
        if (isFromLocalFile()) {
            importFromLocalFile(getFiles());
        } else {
            Progress.setDisplayName(progress, "Connect to email server");
            Store store = connectToMailService();
            if (store != null) {
                importEmail(store);
            }
        }
    }

    /**
     * connect to the email server
     * @return
     */
    private Store connectToMailService() {
        Properties property = System.getProperties();
        Session session = Session.getInstance(property, null);
        Store store = null;

        try {
            if (getServerType().equals(EmailDataType.SERVER_TYPE_POP3) && isUseSSL()) {
                store = session.getStore("pop3s");
            } else if (getServerType().equals(EmailDataType.SERVER_TYPE_POP3) && !isUseSSL()) {
                store = session.getStore("pop3");
            } else if (getServerType().equals(EmailDataType.SERVER_TYPE_IMAP) && !isUseSSL()) {
                store = session.getStore("imap");
            } else if (getServerType().equals(EmailDataType.SERVER_TYPE_IMAP) && isUseSSL()) {
                store = session.getStore("imaps");
            } else {
                return null;
            }
            store.connect(getServerURL(), getPort(), getUserName(), getUserPsw());

            return store;
        } catch (NoSuchProviderException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Impossible to connect to the mail server, please"
                    + " check your configuration", "Connection error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            cancel = true;
            return null;
        }
    }

    private boolean importEmail(Store store) {
        try {
            Folder folder = null;
            //get the folder of inbox
            folder = store.getDefaultFolder();
            if (folder == null) {
                return false;
            }
            //if it's inbox
            folder = folder.getFolder("INBOX");
            if (folder == null) {
                return false;
            }
            folder.open(Folder.READ_ONLY);
            //get mail list
            Message[] msgs = folder.getMessages();
            Progress.switchToDeterminate(progress, msgs.length);
            Progress.setDisplayName(progress, "Download " + msgs.length + " emails");
            //show progress bar
//            showProgressBar(msgs.length);
            int index = 0;
            for (Message msg : msgs) {
                index++;
//                setProgressBar(index);
                filterOneEmail(msg);
                Progress.progress(progress);
                if (cancel) {
                    break;
                }
            }
            folder.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        } catch (MessagingException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        return true;
    }

    /**
     * deal with one email
     * @param msg
     */
    private void filterOneEmail(Message msg) {
        HashMap<String, String> filters = getFilter();
        EmailFilterFactory factory = Lookup.getDefault().lookup(EmailFilterFactory.class);

        //do the filter operation, if the message isn't filtered; go on to parse it
        for (String filter : filters.keySet()) {
            EmailFilter emailFilter = factory.createEmailFilter(filter);
            if (emailFilter == null) {
                report.log("no this kind of email filter:" + filter);
            } else {
                if (emailFilter.filterEmail(msg, getFilterProperty(filter), report)) {
                    break;
                } else {
                    return;
                }
            }
        }

        //process after filter
        NodeDraft sourceNode = null, targetNode = null;
        //construct the source node
        InternetAddress fromAddress = null;
        try {
            Address[] froms = msg.getFrom();
            if (froms == null || froms.length == 0) {
                report.log("message " + msg + "don't have from address");
                return;
            }
            fromAddress = (InternetAddress) froms[0];
        } catch (MessagingException e) {
            try {
                fromAddress = constructFromAddress(msg);
            } catch (MessagingException ex) {
                report.log("Can't parse message :" + msg.toString());
                return;
            }
        }

        //address string
        if (fromAddress == null) {
            report.log("From address of message " + msg + " is null.");
            return;
        }
        if (fromAddress.getAddress() == null || fromAddress.getAddress().isEmpty()) {
            report.log("Can't parse from message " + msg + ".");
            return;
        }
        if (fromAddress.getPersonal() == null || fromAddress.getPersonal().isEmpty()) {
            try {
                fromAddress.setPersonal(fromAddress.getAddress());
            } catch (UnsupportedEncodingException ex) {
                report.log("message " + msg + " cann't be parsed.");
                return;
            }
        }

        //get the codec type
        String codecType = null;
        String contentType = null;
        try {
            contentType = msg.getContentType();
        } catch (MessagingException ex) {
            report.log("message:" + msg + ",can't get the content type of the email");
            return;
            //log
        }
        StringTokenizer s = new StringTokenizer(contentType, ";");
        while (s.hasMoreTokens()) {
            String temp = s.nextToken();
            if (temp.contains("charset")) {
                codecType = temp.substring(9, temp.length());
            }
        }
        if (contentType == null || contentType.isEmpty()) {
            contentType = "UTF-8";
        }
        if (codecType == null || codecType.isEmpty()) {
            codecType = "UTF-8";
        }

        if (!container.nodeExists(fromAddress.getAddress())) {
            //whether use one node to display the same display name
            boolean exist = false;
            if (isUseOneNodeIfSameDisplayName()) {
                if (container instanceof ContainerUnloader) {
                    ContainerUnloader con = (ContainerUnloader) container;
                    Collection<? extends NodeDraftGetter> allNodes = con.getNodes();
                    for (NodeDraftGetter node : allNodes) {
                        if (node.getLabel() == null || node.getLabel().isEmpty()) {
                            continue;
                        }
                        if (node.getLabel().equals(fromAddress.getPersonal())) {
                            sourceNode = container.getNode(node.getId());
                            exist = true;
                            break;
                        }
                    }
                }
            }
            if (!exist || !isUseOneNodeIfSameDisplayName()) {
                sourceNode = container.factory().newNodeDraft();
                sourceNode.setId(Utilities.codecTranslate(codecType, fromAddress.getAddress()));
                sourceNode.setLabel(Utilities.codecTranslate(codecType, fromAddress.getPersonal()));
                container.addNode(sourceNode);
            }
        } else {
            sourceNode = container.getNode(fromAddress.getAddress());
        }
        //construct the target node
        Address[] recipietsTo = null;
        try {
            recipietsTo = msg.getRecipients(RecipientType.TO);
        } catch (MessagingException ex) {
            report.log("message:" + msg + ",can't get the To adress of the email");
            return;//log
        }
        if (recipietsTo != null) {
            for (Address addr : recipietsTo) {
                InternetAddress addrTo = (InternetAddress) addr;
                if (!container.nodeExists(addrTo.getAddress())) {
                    //whether use one node to display the same display name
                    boolean exist = false;
                    if (isUseOneNodeIfSameDisplayName()) {
                        if (container instanceof ContainerUnloader) {
                            ContainerUnloader con = (ContainerUnloader) container;
                            Collection<? extends NodeDraftGetter> allNodes = con.getNodes();
                            for (NodeDraftGetter node : allNodes) {
                                if (node.getLabel() == null || node.getLabel().isEmpty()) {
                                    continue;
                                }
                                if (node.getLabel().equals(fromAddress.getPersonal())) {
                                    targetNode = container.getNode(node.getId());
                                    exist = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!exist) {
                        targetNode = container.factory().newNodeDraft();
                        targetNode.setId(Utilities.codecTranslate(codecType, addrTo.getAddress()));
                        targetNode.setLabel(Utilities.codecTranslate(codecType, addrTo.getPersonal()));
                        container.addNode(targetNode);
                    }
                } else {
                    targetNode = container.getNode(addrTo.getAddress());
                }
                //add an edge
                EdgeDraft edge = container.getEdge(sourceNode, targetNode);
                if (edge == null) {
                    edge = container.factory().newEdgeDraft();
                    edge.setSource(sourceNode);
                    edge.setTarget(targetNode);
                    edge.setWeight(1f);
                    container.addEdge(edge);
                } else {
                    edge.setWeight(edge.getWeight() + 1f);
                }
            }
        }
        // cc or bcc as weight
        if (hasCcAsWeight()) {
            //construct the target node of cc
            Address[] recipietsCc = null;
            try {
                recipietsCc = msg.getRecipients(RecipientType.CC);
            } catch (MessagingException ex) {
                report.log("message:" + msg + ",can't get the Cc of the email");
                return;
                //log
            }
            if (recipietsCc != null) {
                for (Address addr : recipietsCc) {
                    InternetAddress addrCc = (InternetAddress) addr;
                    if (!container.nodeExists(addrCc.getAddress())) {
                        //whether use one node to display the same display name
                        boolean exist = false;
                        if (isUseOneNodeIfSameDisplayName()) {
                            if (container instanceof ContainerUnloader) {
                                ContainerUnloader con = (ContainerUnloader) container;
                                Collection<? extends NodeDraftGetter> allNodes = con.getNodes();
                                for (NodeDraftGetter node : allNodes) {
                                    if (node.getLabel() == null || node.getLabel().isEmpty()) {
                                        continue;
                                    }
                                    if (node.getLabel().equalsIgnoreCase(fromAddress.getPersonal())) {
                                        targetNode = container.getNode(node.getId());
                                        exist = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!exist || !isUseOneNodeIfSameDisplayName()) {
                            targetNode = container.factory().newNodeDraft();
                            targetNode.setId(Utilities.codecTranslate(codecType, addrCc.getAddress()));
                            targetNode.setLabel(Utilities.codecTranslate(codecType, addrCc.getPersonal()));
                            container.addNode(targetNode);
                        }

                    } else {
                        targetNode = container.getNode(addr.toString());
                    }
                    //if use cc as weight, add an edge between cc
                    EdgeDraft edge = container.getEdge(sourceNode, targetNode);
                    if (edge == null) {
                        edge = container.factory().newEdgeDraft();
                        edge.setSource(sourceNode);
                        edge.setTarget(targetNode);
                        container.addEdge(edge);
                        edge.setWeight(1f);
                    } else {
                        edge.setWeight(edge.getWeight() + 1f);
                    }
                }
            }
        }
        if (hasBccAsWeight()) {
            //construct the target node of bcc
            Address[] recipietsBcc = null;
            try {
                recipietsBcc = msg.getRecipients(RecipientType.BCC);
            } catch (MessagingException ex) {
                report.log("message:" + msg + ",can't get the Bcc of the email");
                return;
                //TODO log
            }
            if (recipietsBcc != null) {
                for (Address addr : recipietsBcc) {
                    InternetAddress addrBcc = (InternetAddress) addr;
                    if (!container.nodeExists(addrBcc.getAddress())) {
                        //whether use one node to display the same display name
                        boolean exist = false;
                        if (isUseOneNodeIfSameDisplayName()) {
                            if (container instanceof ContainerUnloader) {
                                ContainerUnloader con = (ContainerUnloader) container;
                                Collection<? extends NodeDraftGetter> allNodes = con.getNodes();
                                for (NodeDraftGetter node : allNodes) {
                                    if (node.getLabel() == null || node.getLabel().isEmpty()) {
                                        continue;
                                    }
                                    if (node.getLabel().equals(fromAddress.getPersonal())) {
                                        targetNode = container.getNode(node.getId());
                                        exist = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!exist || !isUseOneNodeIfSameDisplayName()) {
                            targetNode = container.factory().newNodeDraft();
                            targetNode.setId(Utilities.codecTranslate(codecType, addrBcc.getAddress()));
                            targetNode.setLabel(Utilities.codecTranslate(codecType, addrBcc.getPersonal()));
                            container.addNode(targetNode);
                        }
                    } else {
                        targetNode = container.getNode(addr.toString());
                    }
                    //if use cc as weight, add an edge between cc
                    EdgeDraft edge = container.getEdge(sourceNode, targetNode);
                    if (edge == null) {
                        edge = container.factory().newEdgeDraft();
                        edge.setSource(sourceNode);
                        edge.setTarget(targetNode);
                        container.addEdge(edge);
                        edge.setWeight(1f);
                    } else {
                        edge.setWeight(edge.getWeight() + 1f);
                    }
                }
            }
        }
    }

    /**
     * construct a address by message
     * @param msg
     */
    private InternetAddress constructFromAddress(Message msg) throws MessagingException {
        InternetAddress address = new InternetAddress();
        if (msg instanceof MimeMessage) {
            String fromHeader = msg.getHeader("From")[0];
            if (fromHeader.contains("<") && fromHeader.contains(">")) {
                address.setAddress(fromHeader.substring(fromHeader.lastIndexOf('<') + 1, fromHeader.lastIndexOf('>')));
            } else {
                report.log("Can't parse mime message :" + msg.toString());
                return null;
            }
        } else {
            report.log("Can't parse message :" + msg.toString());
            return null;
        }
        return address;
    }

    /**
     * import from local files
     * @param files
     */
    private void importFromLocalFile(File[] files) {
        if (files == null) {
            return;
        }
        EmailFilesFilter[] filters =
                Lookup.getDefault().lookupAll(EmailFilesFilter.class).toArray(new EmailFilesFilter[0]);
        int totalNumOfEmails = getNumOfLocalEmailFile(files);
        progress.switchToDeterminate(totalNumOfEmails * filters.length);
        for (EmailFilesFilter f : filters) {
            if (!getFileFilterType().equals(f.getDisplayName())) {
                progress.progress(totalNumOfEmails);
            } else {
                for (File file : files) {
                    if (cancel) {
                        return;
                    }
                    if (!file.isDirectory()) {
                        progress.progress();
                        MimeMessage message = f.parseFile(file, report);
                        if (message == null) {
                            report.log("file " + file.getName() + "can't be parsed");
                            return;
                        } else {
                            filterOneEmail(message);
                        }
                    } else if (file.isDirectory()) {
                        importFromLocalFile(file.listFiles());
                    } else {
                        continue;
                    }
                }
            }
        }
    }

    private int getNumOfLocalEmailFile(File[] files) {
        int totalNum = 0;
        if (files == null) {
            return 0;
        }
        for (File f : files) {
            totalNum += getNumOfOneFile(f);
        }
        return totalNum;
    }

    private int getNumOfOneFile(File f) {
        int temp = 0;
        if (!f.isDirectory()) {
            temp = 1;
        } else {
            temp = temp + getNumOfLocalEmailFile(f.listFiles());
        }
        return temp;
    }
}
