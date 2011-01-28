/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.project.api;

/**
 * Hosts user data about a project. These information are usually saved to the
 * project file.
 *
 * @author Mathieu Bastian
 */
public interface ProjectMetaData {

    public String getKeywords();

    public String getAuthor();

    public String getDescription();

    public String getTitle();

    public void setAuthor(String author);

    public void setDescription(String description);

    public void setKeywords(String keywords);

    public void setTitle(String title);
}
