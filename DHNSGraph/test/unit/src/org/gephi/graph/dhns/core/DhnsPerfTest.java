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
