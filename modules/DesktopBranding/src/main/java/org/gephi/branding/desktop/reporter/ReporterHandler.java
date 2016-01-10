/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.branding.desktop.reporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ReporterHandler extends java.util.logging.Handler implements Callable<JButton>, ActionListener {

    private Throwable throwable;
    private String MEMORY_ERROR;
    public ReporterHandler() {
        MEMORY_ERROR = NbBundle.getMessage(ReporterHandler.class, "OutOfMemoryError.message");
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getThrown() == null) {
            return;
        }
        throwable = record.getThrown();
        if (throwable != null && throwable instanceof OutOfMemoryError) {
            Handler[] handlers = Logger.getLogger("").getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                Handler h = handlers[i];
                h.close();
            }
            NotifyDescriptor nd = new NotifyDescriptor.Message(MEMORY_ERROR, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            LifecycleManager.getDefault().exit();
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        throwable = null;
    }

    @Override
    public JButton call() throws Exception {
        JButton btn = new JButton(NbBundle.getMessage(ReporterHandler.class, "ReportHandler.button"));
        btn.addActionListener(this);
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Report report = new Report();
        report.setThrowable(throwable);
        report.setSummary(createMessage(throwable));
        ReportPanel panel = new ReportPanel(report);
        panel.showDialog();
    }

    protected static String createMessage(Throwable thr) {
        //ignore causes with empty stacktraces -> they are just annotations
        while ((thr.getCause() != null) && (thr.getCause().getStackTrace().length != 0)) {
            thr = thr.getCause();
        }
        String message = thr.toString();
        if (message.startsWith("java.lang.")) {
            message = message.substring(10);
        }
        int indexClassName = message.indexOf(':');
        if (indexClassName == -1) { // there is no message after className
            if (thr.getStackTrace().length != 0) {
                StackTraceElement elem = thr.getStackTrace()[0];
                return message + " at " + elem.getClassName() + "." + elem.getMethodName();
            }
        }
        return message;
    }
}
