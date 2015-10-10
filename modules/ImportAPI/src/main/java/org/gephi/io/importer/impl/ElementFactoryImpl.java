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

import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.Issue;
import org.openide.util.NbBundle;

public class ElementFactoryImpl implements ElementDraft.Factory {

    protected final ImportContainerImpl container;
    protected final static AtomicInteger NODE_IDS = new AtomicInteger();
    protected final static AtomicInteger EDGE_IDS = new AtomicInteger();

    public ElementFactoryImpl(ImportContainerImpl container) {
        this.container = container;
    }

    @Override
    public NodeDraftImpl newNodeDraft() {
        NodeDraftImpl node = new NodeDraftImpl(container, "n" + NODE_IDS.getAndIncrement());
        return node;
    }

    @Override
    public NodeDraftImpl newNodeDraft(String id) {
        if (id == null) {
            String message = NbBundle.getMessage(ElementFactoryImpl.class, "ElementFactoryException_NullNodeId");
            container.getReport().logIssue(new Issue(message, Issue.Level.CRITICAL));
        }
        NodeDraftImpl node = new NodeDraftImpl(container, id);
        return node;
    }

    @Override
    public EdgeDraftImpl newEdgeDraft() {
        EdgeDraftImpl edge = new EdgeDraftImpl(container, "e" + EDGE_IDS.getAndIncrement());
        return edge;
    }

    @Override
    public EdgeDraftImpl newEdgeDraft(String id) {
        if (id == null) {
            String message = NbBundle.getMessage(ElementFactoryImpl.class, "ElementFactoryException_NullEdgeId");
            container.getReport().logIssue(new Issue(message, Issue.Level.CRITICAL));
        }
        EdgeDraftImpl edge = new EdgeDraftImpl(container, id);
        return edge;
    }
}
