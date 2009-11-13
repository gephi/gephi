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
package org.gephi.visualization;

import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class TextDataPersistenceProvider implements WorkspacePersistenceProvider {

    private static final String ELEMENT_NODEDATA_TEXTDATA = "textdata";
    private static final String ELEMENT_NODEDATA_TEXTDATA_COLOR = "color";
    private static final String ELEMENT_NODEDATA_TEXTDATA_SIZE = "size";

    public Element writeXML(Document document, Workspace workspace) {

        throw new UnsupportedOperationException("Not supported yet.");

//        Element textDataE = document.createElement(ELEMENT_NODEDATA_TEXTDATA);
//        TextData textData = nodeData.getTextData();
//        if(textData!=null) {
//            if(textData.getR()> 0) {
//                Element textColorE = document.createElement(ELEMENT_NODEDATA_TEXTDATA_COLOR);
//                textColorE.setAttribute("r", String.valueOf(textData.getR()));
//                textColorE.setAttribute("g", String.valueOf(textData.getG()));
//                textColorE.setAttribute("b", String.valueOf(textData.getB()));
//                textColorE.setAttribute("a", String.valueOf(textData.getAlpha()));
//                textDataE.appendChild(textColorE);
//            }
//            Element textSizeE = document.createElement(ELEMENT_NODEDATA_TEXTDATA_SIZE);
//            textSizeE.setAttribute("value", String.valueOf(textData.getSize()));
//            textDataE.appendChild(textSizeE);
//
//        }

    }

    public void readXML(Element element, Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getIdentifier() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
