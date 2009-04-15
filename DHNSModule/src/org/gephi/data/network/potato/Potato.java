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
package org.gephi.data.network.potato;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.network.node.PreNode;

/**
 *
 * @author Mathieu Bastian
 */
public class Potato {

    private PreNode node;
    private List<PreNode> content;

    //Display

    public Potato() {
        content = new ArrayList<PreNode>();
    }

    public void setNode(PreNode node) {
        this.node = node;
    }

    public void addContent(PreNode content) {
        this.content.add(content);
    }

    public PreNode getNode()
    {
        return node;
    }

    public List<PreNode> getContent()
    {
        return content;
    }

   
}
