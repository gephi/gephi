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

package org.gephi.importer.api;

import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu
 */
public class ImportException extends Exception {

	private Throwable cause;
	private Importer source;
    private String message;

	public ImportException(Importer source, Throwable cause)
	{
		super(cause);
		this.cause = cause;
		this.source = source;
	}

    public ImportException(String message)
    {
        super(message);
        this.message = message;
    }

	@Override
	public String getMessage() {
        if(this.cause==null && this.source==null)
            return message;
		return getLocalizedMessage();
	}

	@Override
	public String getLocalizedMessage()
	{
        if(this.cause==null && this.source==null)
            return message;
		String sourceName="";
		sourceName = source.getClass().getName();

        Object[] params = new Object[4];
        params[0] = sourceName;
        params[1] = cause.getClass().getSimpleName();
        params[2] = cause.getLocalizedMessage();
        params[3] = cause.getStackTrace()[0].getLineNumber();
        String msg = String.format(NbBundle.getMessage(getClass(), "importException_message"), params);
        return msg;
	}

}
