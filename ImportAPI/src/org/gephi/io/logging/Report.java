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
package org.gephi.io.logging;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mathieu Bastian
 */
public final class Report {

    private final List<ReportEntry> entries = new ArrayList<ReportEntry>();
    private final boolean criticalException = true;

    public void log(String message) {
        entries.add(new ReportEntry(message));
    }

    public void logIssue(Issue issue) {
        entries.add(new ReportEntry(issue));
        if (criticalException && issue.getLevel().equals(Issue.Level.CRITICAL)) {
            if (issue.getThrowable() != null) {
                throw new RuntimeException(issue.getMessage(), issue.getThrowable());
            } else {
                throw new RuntimeException(issue.getMessage());
            }
        }
    }

    public List<Issue> getIssues() {
        List<Issue> res = new ArrayList<Issue>();
        for (ReportEntry re : entries) {
            if (re.issue != null) {
                res.add(re.issue);
            }
        }
        return res;
    }

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
}
