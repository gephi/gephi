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
package org.gephi.project.spi;

import org.gephi.project.api.Workspace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface modules implement to notify the system they can read/write part
 * of the .gephi project file to serialize states and data.
 * <h3>How saving a project works</h3>
 * <ol><li>The saving task is looking for all implementations of this interface and
 * asks to return an XML element that represents data for each workspace.</li>
 * <li>All of these elements are written in the .gephi project file.</li></ol>
 * <h3>How loading a project works</h3>
 * <ol><li>The loading task is looking for all implementations of this interface and
 * asks for the identifier returned by <code>getIdentifier()</code>.</li>
 * <li>When traversing the gephi project XML document it tries to match markups with
 * identifiers. When match, call this provider <code>readXML()</code> method
 * with the XML element.</li></ol>
 * Thus this interface allows any module to serialize and deserialize its data
 * to gephi project files.
 * 
 * @author Mathieu Bastian
 * @see Workspace
 */
public interface WorkspacePersistenceProvider {

    public Element writeXML(Document document, Workspace workspace);

    public void readXML(Element element, Workspace workspace);

    public String getIdentifier();
}
