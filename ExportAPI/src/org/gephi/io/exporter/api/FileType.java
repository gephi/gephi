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

package org.gephi.io.exporter.api;

import org.gephi.io.exporter.spi.FileExporter;

/**
 * File type definition. A simple class which contains a <b>name</b> and 
 * <b>extension</b> for a file type.
 *
 * @author Mathieu Bastian
 * @see FileExporter
 */
public final class FileType
{
    private final String[] extensions;
    private final String name;

    public FileType(String extension, String name)
    {
        this.extensions = new String[] {extension};
        this.name = name;
    }

    public FileType(String[] extensions, String name)
    {
        this.extensions = extensions;
        this.name = name;
    }

    public String getExtension() {
        return extensions[0];
    }

    public String[] getExtensions() {
        return extensions;
    }

    public String getName() {
        return name;
    }
}