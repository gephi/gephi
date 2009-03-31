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

package org.gephi.project.filetype.io;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.gephi.project.api.Project;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu
 */
public class GephiReader {
    
    public Project readAll(Element root) throws Exception
    {
        //XPath
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		//Calculate the task max
		readCore(xpath, root);

		//Project
		XPathExpression exp = xpath.compile("./project[@version=\"1.0\"]");

        return new Project();
    }

    public void readCore(XPath xpath, Element root) throws Exception
	{
		XPathExpression exp = xpath.compile("./core[@version=\"1.0\"]");
		Element coreE = (Element)exp.evaluate(root,XPathConstants.NODE);
        int max = Integer.parseInt(coreE.getAttribute("tasks"));
        System.out.println(max);
	}
}
