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

package org.gephi.io.importer.impl;

import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.junit.Assert;
import org.junit.Test;

public class ImportContainerImplTest {

    @Test
    public void testRemoveEdge() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        generateTinyGraph(importContainer);
        importContainer.removeEdge(importContainer.getEdge("1"));

        Assert.assertTrue(importContainer.verify());
        Assert.assertEquals(1, importContainer.getUnloader().getEdgeCount());
    }

    private void generateTinyGraph(ImportContainerImpl container) {
        NodeDraft node1 = container.factory().newNodeDraft("1");
        NodeDraft node2 = container.factory().newNodeDraft("2");
        EdgeDraft edge1 = container.factory().newEdgeDraft("1");
        edge1.setDirection(EdgeDirection.DIRECTED);
        edge1.setSource(node1);
        edge1.setTarget(node2);
        EdgeDraft edge2 = container.factory().newEdgeDraft("2");
        edge2.setDirection(EdgeDirection.DIRECTED);
        edge2.setSource(node2);
        edge2.setTarget(node1);

        container.addNode(node1);
        container.addNode(node2);
        container.addEdge(edge1);
        container.addEdge(edge2);
    }
}
