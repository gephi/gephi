/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.branding.desktop.reporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JButton;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ReporterHandler extends java.util.logging.Handler implements Callable<JButton>, ActionListener {

    private Throwable throwable;

    @Override
    public void publish(LogRecord record) {
        if (record.getThrown() == null) {
            return;
        }
        throwable = record.getThrown();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        throwable = null;
    }

    public JButton call() throws Exception {
        JButton btn = new JButton(NbBundle.getMessage(ReporterHandler.class, "ReportHandler.button"));
        btn.addActionListener(this);
        return btn;
    }

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
