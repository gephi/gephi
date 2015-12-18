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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import org.gephi.io.importer.api.Issue.Level;
import org.openide.util.Exceptions;

/**
 * Report is a log and issue container. Filled with information, details, minor
 * or major issues, it is stored in an issue list and can be retrieved to
 * present issues to end-users. Behavior is the same as a simple logging
 * library.
 *
 * @author Mathieu Bastian
 */
public final class Report {

    private Issue.Level exceptionLevel = Issue.Level.CRITICAL;

    //File
    private final File file;
    private Writer writer;

    public Report() {
        File f = null;
        try {
            f = File.createTempFile("tempreport", Long.toString(System.nanoTime()));
            f.deleteOnExit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            file = f;
        }
    }

    /**
     * Free resources.
     */
    public void clean() {
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Closes writing.
     */
    public void close() {
        if (writer != null) {
            try {
                writer.close();
                writer = null;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Log an information message in the report.
     *
     * @param message the message to write in the report
     * @throws NullPointerException if <code>message</code> is <code>null</code>
     */
    public synchronized void log(String message) {
        try {
            if (writer == null) {
                writer = new Writer(file);
            }
            writer.append(new ReportEntry(message));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Appends all entries in <code>report</code> to this report.
     *
     * @param report report to read entries from
     */
    public synchronized void append(Report report) {
        if (report.writer != null) {
            report.close();
        }
        Reader r = null;
        try {
            if (writer == null) {
                writer = new Writer(file);
            }
            r = new Reader(report.file);
            for (; r.hasNext();) {
                ReportEntry re = r.next();
                writer.append(re);
            }
        } catch (IOException ex) {
            if (r != null) {
                r.close();
            }
            throw new RuntimeException(ex);
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    /**
     * Log an issue in the report.
     *
     * @param issue the issue to write in the report
     * @throws NullPointerException if <code>issue</code> is <code>null</code>
     */
    public synchronized void logIssue(Issue issue) {
        try {
            if (writer == null) {
                writer = new Writer(file);
            }
            writer.append(new ReportEntry(issue));

            if (issue.getLevel().toInteger() >= exceptionLevel.toInteger()) {
                writer.close();
                if (issue.getThrowable() != null) {
                    throw new RuntimeException(issue.getMessage(), issue.getThrowable());
                } else {
                    throw new RuntimeException(issue.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns all issues written in the report.
     *
     * @param limit maximum number of issuers
     * @return a collection of all issues written in the report
     */
    public synchronized Iterator<Issue> getIssues(int limit) {
        if (writer != null) {
            close();
        }
        Reader reader = null;
        try {
            reader = new Reader(file);
            return new IssueIterator(reader, limit);
        } catch (IOException ex) {
            if (reader != null) {
                reader.close();
            }
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns the report logs and issues, presented as basic multi-line text.
     *
     * @return a string of all messages and issues written in the report, one
     * per line
     */
    public synchronized String getText() {
        if (writer != null) {
            close();
        }
        StringBuilder builder = new StringBuilder();
        Reader r = null;
        try {
            r = new Reader(file);
            for (; r.hasNext();) {
                ReportEntry re = r.next();
                if (re.level == null) {
                    builder.append(re.message);
                    builder.append("\n");
                }
            }
        } catch (IOException ex) {
            if (r != null) {
                r.close();
            }
            throw new RuntimeException(ex);
        }
        return builder.toString();
    }

    /**
     * Get the current exception level for the report. Default is
     * <code>Level.CRITICAL</code>.
     *
     * @return the current exception level
     */
    public Level getExceptionLevel() {
        return exceptionLevel;
    }

    /**
     * Set the level of exception for the report. If a reported issue has his
     * level greater or equal as <code>exceptionLevel</code>, an exception is
     * thrown. Default is <code>Level.CRITICAL</code>
     *
     * @param exceptionLevel the exception level where exceptions are to be
     * thrown
     */
    public void setExceptionLevel(Level exceptionLevel) {
        this.exceptionLevel = exceptionLevel;

    }

    /**
     * Inner report entry class.
     */
    private static class ReportEntry {

        private final Level level;
        private final String message;

        public ReportEntry(Issue issue) {
            this.level = issue.getLevel();
            this.message = issue.getMessage();
        }

        public ReportEntry(String message) {
            this.message = message;
            this.level = null;
        }
    }

    /**
     * Writer sub-class.
     */
    private static class Writer {

        private final BufferedWriter writer;

        public Writer(File file) throws IOException {
            FileWriter fileWriter = new FileWriter(file, true);
            writer = new BufferedWriter(fileWriter);
        }

        public void append(ReportEntry entry) throws IOException {
            Level level = entry.level;
            if (level != null) {
                writer.append(level.toString());
            }
            writer.append(";");
            writer.append(entry.message);
            writer.append("\n");
        }

        public void close() throws IOException {
            writer.flush();
            writer.close();
        }
    }

    /**
     * Reader sub-class.
     */
    private static class Reader implements Iterator<ReportEntry> {

        private final BufferedReader reader;
        private String pointer;
        private boolean closed;

        public Reader(File file) throws IOException {
            FileReader fileReader = new FileReader(file);
            reader = new LineNumberReader(fileReader);
        }

        @Override
        public boolean hasNext() {
            if (closed) {
                return false;
            }
            try {
                pointer = reader.readLine();
                if (pointer != null) {
                    return true;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            try {
                reader.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return false;
        }

        @Override
        public ReportEntry next() {
            if (pointer.startsWith(";")) {
                return new ReportEntry(pointer.substring(1, pointer.length()));
            } else {
                int index = pointer.indexOf(";");
                if (index == -1) {
                    return new ReportEntry(pointer);
                } else {
                    String levelStr = pointer.substring(0, index);
                    String message = pointer.substring(index + 1);
                    return new ReportEntry(new Issue(message, Level.valueOf(levelStr)));
                }
            }
        }

        public void close() {
            try {
                reader.close();
            } catch (IOException ex2) {
                throw new RuntimeException(ex2);
            }
            pointer = null;
            closed = true;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class IssueIterator implements Iterator<Issue> {

        private final Reader itr;
        private ReportEntry next;
        private final int limit;
        private int count;

        public IssueIterator(Reader itr, int limit) {
            this.itr = itr;
            this.limit = limit;
        }

        @Override
        public boolean hasNext() {
            while (itr.hasNext()) {
                next = itr.next();
                if (next.level != null) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Issue next() {
            Issue res = new Issue(next.message, next.level);
            if (++count == limit) {
                itr.close();
            }
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
