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
package org.gephi.io.importer.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.gephi.io.importer.api.Issue.Level;

/**
 * Report is a log and issue container. Filled with information, details, minor or major issues, it is stored in an issue list
 * and can be retrieved to present issues to end-users. Behavior is the same as a simple logging library.
 *
 * @author Mathieu Bastian
 */
public final class Report {

    private final Queue<ReportEntry> entries = new ConcurrentLinkedQueue<ReportEntry>();
    private Issue.Level exceptionLevel = Issue.Level.CRITICAL;

    /**
     * Log an information message in the report.
     * @param message the message to write in the report
     * @throws NullPointerException if <code>message</code> is <code>null</code>
     */
    public void log(String message) {
        entries.add(new ReportEntry(message));
    }

    public void append(Report report) {
        entries.addAll(report.entries);
    }

    /**
     * Log an issue in the report.
     * @param issue the issue to write in the report
     * @throws NullPointerException if <code>issue</code> is <code>null</code>
     */
    public void logIssue(Issue issue) {
        entries.add(new ReportEntry(issue));
        if (issue.getLevel().toInteger() >= exceptionLevel.toInteger()) {
            if (issue.getThrowable() != null) {
                throw new RuntimeException(issue.getMessage(), issue.getThrowable());
            } else {
                throw new RuntimeException(issue.getMessage());
            }
        }
    }

    /**
     * Returns all issues written in the report.
     * @return a collection of all issues written in the report
     */
    public List<Issue> getIssues() {
        List<Issue> res = new ArrayList<Issue>();
        for (ReportEntry re : entries) {
            if (re.issue != null) {
                res.add(re.issue);
            }
        }
        return res;
    }

    /**
     * Returns the report logs and issues, presented as <b>HTML</b> code.
     * @return a string of HTML code where all messages and issues are written
     */
    public String getHtml() {
        StringBuilder builder = new StringBuilder();
        for (ReportEntry re : entries) {
            if (re.issue != null) {
                builder.append(re.issue.getMessage());
                builder.append("<br>");
            } else {
                builder.append(re.message);
                builder.append("<br>");
            }
        }
        return builder.toString();
    }

    /**
     * Returns the report logs and issues, presented as basic multi-line text.
     * @return a string of all messages and issues written in the report, one per line
     */
    public String getText() {
        StringBuilder builder = new StringBuilder();
        for (ReportEntry re : entries) {
            if (re.issue != null) {
                builder.append(re.issue.getMessage());
                builder.append("\n");
            } else {
                builder.append(re.message);
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * Get the current exception level for the report. Default is <code>Level.CRITICAL</code>.
     * @return the current exception level
     */
    public Level getExceptionLevel() {
        return exceptionLevel;
    }

    /**
     * Set the level of exception for the report. If a reported issue has his level greater or equal
     * as <code>exceptionLevel</code>, an exception is thrown. Default is <code>Level.CRITICAL</code>
     * @param exceptionLevel the exception level where exceptions are to be thrown
     */
    public void setExceptionLevel(Level exceptionLevel) {
        this.exceptionLevel = exceptionLevel;
    }

    private class ReportEntry {

        private final Issue issue;
        private final String message;

        public ReportEntry(Issue issue) {
            this.issue = issue;
            this.message = null;
        }

        public ReportEntry(String message) {
            this.message = message;
            this.issue = null;
        }
    }

    public void pruneReport(int limit) {
        if (entries.size() > limit) {
            int step = 0;
            while (entries.size() > limit && step < 3) {
                if (step == 0) {
                    ReportEntry lastIssue = null;
                    for (Iterator<ReportEntry> itr = entries.iterator(); itr.hasNext();) {
                        ReportEntry issue = itr.next();
                        if (issue.issue != null && issue.issue.getLevel().equals(Issue.Level.INFO)) {
                            lastIssue = issue;
                            itr.remove();
                        }
                    }
                    if (lastIssue != null) {
                        entries.add(lastIssue);
                        entries.add(new ReportEntry(new Issue("More issues not listed...", Issue.Level.INFO)));
                    }
                    step = 1;
                } else if (step == 1) {
                    ReportEntry lastIssue = null;
                    for (Iterator<ReportEntry> itr = entries.iterator(); itr.hasNext();) {
                        ReportEntry issue = itr.next();
                        if (issue.issue != null && issue.issue.getLevel().equals(Issue.Level.WARNING)) {
                            lastIssue = issue;
                            itr.remove();
                        }
                    }
                    if (lastIssue != null) {
                        entries.add(lastIssue);
                        entries.add(new ReportEntry(new Issue("More issues not listed...", Issue.Level.WARNING)));
                    }
                    step = 2;
                } else if (step == 2) {
                    ReportEntry lastIssue = null;
                    for (Iterator<ReportEntry> itr = entries.iterator(); itr.hasNext();) {
                        ReportEntry issue = itr.next();
                        if (issue.issue != null && issue.issue.getLevel().equals(Issue.Level.INFO)) {
                            lastIssue = issue;
                            itr.remove();
                        }
                    }
                    if (lastIssue != null) {
                        entries.add(lastIssue);
                        entries.add(new ReportEntry(new Issue("More issues not listed...", Issue.Level.INFO)));
                    }
                    step = 3;
                } else if (step == 3) {
                    ReportEntry lastIssue = null;
                    for (Iterator<ReportEntry> itr = entries.iterator(); itr.hasNext();) {
                        ReportEntry issue = itr.next();
                        if (issue.issue == null) {
                            lastIssue = issue;
                            itr.remove();
                        }
                    }
                    if (lastIssue != null) {
                        entries.add(lastIssue);
                        entries.add(new ReportEntry("More messages not listed..."));
                    }
                    step = 4;
                }
            }
        }
    }
}
