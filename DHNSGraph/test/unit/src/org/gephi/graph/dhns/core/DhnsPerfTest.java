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
package org.gephi.graph.dhns.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.gephi.graph.api.Graph;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.node.AbstractNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Mathieu Bastian
 */
public class DhnsPerfTest {

    private Dhns dhns;
    private AbstractNode singleNode;
    private Graph graph;
    private SimpleLock simpleLock;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        DhnsGraphController graphController = new DhnsGraphController();
        dhns = new Dhns(graphController, null);

        singleNode = dhns.factory().newNode();
        dhns.getDirectedGraph().addNode(singleNode);
        graph = dhns.getDirectedGraph();
        simpleLock = new SimpleLock();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testWithLocking() {
        ReentrantReadWriteLock readWriteLock = dhns.getReadWriteLock();
        Lock lock = readWriteLock.readLock();

        for (int i = 0; i < 100000; i++) {
            lock.lock();
            int degree = singleNode.getEdgesInTree().getCount() + singleNode.getEdgesOutTree().getCount();
            lock.unlock();
        }
    }

    @Test
    public void testWithGlobalLocking() {
        ReentrantReadWriteLock readWriteLock = dhns.getReadWriteLock();
        Lock lock = readWriteLock.readLock();

        lock.lock();
        for (int i = 0; i < 100000; i++) {
            lock.lock();
            int degree = singleNode.getEdgesInTree().getCount() + singleNode.getEdgesOutTree().getCount();
            lock.unlock();
        }
        lock.unlock();
    }

    @Test
    public void testWithSimpleLocking() {

        simpleLock.readLock();
        for (int i = 0; i < 100000; i++) {
            simpleLock.readLock();
            int degree = singleNode.getEdgesInTree().getCount() + singleNode.getEdgesOutTree().getCount();
            simpleLock.readUnlock();
        }
        simpleLock.readUnlock();
    }

    @Test
    public void testWithoutLocking() {
        for (int i = 0; i < 100000; i++) {
            int degree = singleNode.getEdgesInTree().getCount() + singleNode.getEdgesOutTree().getCount();
        }
    }
}
