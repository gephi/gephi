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

/**
 *
 * @author Mathieu Bastian
 */
public final class Issue {

    public enum Level {

        INFO, WARNING, SEVERE, CRITICAL
    };
    private final Throwable throwable;
    private final String message;
    private final Level level;

    public Issue(Throwable throwable, Level level) {
        this.throwable = throwable;
        this.level = level;
        this.message = throwable.getMessage();
    }

    public Issue(String message, Level level, Throwable throwable) {
        this.throwable = throwable;
        this.level = level;
        this.message = message;
    }

    public Issue(String message, Level level) {
        this.message = message;
        this.level = level;
        this.throwable = null;
    }

    public String getMessage() {
        return message;
    }

    public Level getLevel() {
        return level;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
