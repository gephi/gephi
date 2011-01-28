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
package org.gephi.graph.dhns.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Mathieu Bastian
 */
public class SimpleLock {

    private ThreadLocal<AtomicInteger> count = new ThreadLocal<AtomicInteger>();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void readLock() {
        if (get() == 0) {
            lock.readLock().lock();
        }
        increment();
    }

    public void readUnlock() {
        if (get() == 0) {
            throw new IllegalMonitorStateException();
        } else if (get() == 1) {
            lock.readLock().unlock();
            remove();
        } else {
            decrement();
        }
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    private void remove() {
        count.remove();
    }

    private int get() {
        AtomicInteger i = count.get();
        if (i == null) {
            return 0;
        } else {
            return i.intValue();
        }
    }

    private void increment() {
        AtomicInteger i = count.get();
        if (i == null) {
            i = new AtomicInteger(0);
            count.set(i);
        }
        i.incrementAndGet();
    }

    private void decrement() {
        AtomicInteger i = count.get();
        if (i == null) {
            // we should never get here...
            throw new IllegalStateException();
        }
        i.decrementAndGet();
    }
}
