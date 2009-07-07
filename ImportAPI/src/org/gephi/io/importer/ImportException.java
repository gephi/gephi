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
package org.gephi.io.importer;

import org.openide.util.NbBundle;

/**
 * Localized exceptions thrown by importers. Messages returns following details about the nature and
 * localization of exception in the code:
 * <ul><li>Exception</li>
 * <li>Class</li>
 * <li>Code line number</li></ul>
 * <p>Returns the following localized message:
 * <em>The importer class %s failed to import the file.\n\nException: %s : %s\nClass: %s\nLine: %d</em>
 *
 * @author Mathieu Bastian
 */
public final class ImportException extends RuntimeException {

    private final Throwable cause;
    private final Importer source;
    private final String message;

    public ImportException(Importer source, Throwable cause) {
        super(cause);
        this.cause = cause;
        this.source = source;
        this.message = "";
    }

    public ImportException(String message) {
        super(message);
        this.cause = null;
        this.source = null;
        this.message = message;
    }

    @Override
    public String getMessage() {
        if (this.cause == null && this.source == null) {
            return message;
        }
        return getLocalizedMessage();
    }

    @Override
    public String getLocalizedMessage() {
        if (this.cause == null && this.source == null) {
            return message;
        }
        String sourceName = "";
        sourceName = source.getClass().getName();

        Object[] params = new Object[5];
        params[0] = sourceName;
        params[1] = cause.getClass().getSimpleName();
        params[2] = cause.getLocalizedMessage();
        params[3] = cause.getStackTrace()[0].getClassName();
        params[4] = cause.getStackTrace()[0].getLineNumber();
        String msg = String.format(NbBundle.getMessage(getClass(), "importException_message"), params);
        return msg;
    }
}
