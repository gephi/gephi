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

/**
 * Issue are logged and classified by <code>Report</code> to describe a problem
 * encountered during import process.
 * <p>
 * Issues have a level of severity based on {@link Level}. The
 * <code>CRITICAL</code> level is by default configured in {@link Report} to
 * throw an exception and stop the import process. Other levels are logged and
 * presented to the user.
 *
 * @author Mathieu Bastian
 * @see Report
 */
public final class Issue {

    public enum Level {

        INFO(100),
        WARNING(200),
        SEVERE(500),
        CRITICAL(1000);
        private final int levelInt;

        Level(int levelInt) {
            this.levelInt = levelInt;
        }

        public int toInteger() {
            return levelInt;
        }
    }
    private final Throwable throwable;
    private final String message;
    private final Level level;

    /**
     * Constructs a new issue with a throwable and a level.
     * <p>
     * The message is set based on throwable.
     *
     * @param throwable throwable
     * @param level level
     */
    public Issue(Throwable throwable, Level level) {
        this.throwable = throwable;
        this.level = level;
        this.message = throwable.getMessage();
    }

    /**
     * Constructs a new issue with a message, level and throwable.
     *
     * @param message message
     * @param level level
     * @param throwable throwable
     */
    public Issue(String message, Level level, Throwable throwable) {
        this.throwable = throwable;
        this.level = level;
        this.message = message;
    }

    /**
     * Constructs a new issue with a message and a level.
     *
     * @param message message
     * @param level level
     */
    public Issue(String message, Level level) {
        this.message = message;
        this.level = level;
        this.throwable = null;
    }

    /**
     * Returns this issue's message.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns this issue's level.
     *
     * @return level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Returns this issue's throwable.
     *
     * @return throwable or null if unset
     */
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "Issue{" + "message=" + message + ", level=" + level + '}';
    }
}
