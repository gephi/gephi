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
package org.gephi.utils.longtask;

import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.progress.ProgressTicket;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public class LongTaskExecutorTest {

    public LongTaskExecutorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testExecutor() {
        final LongTaskExecutor longTaskExecutor = new LongTaskExecutor(true);
        LongTaskListener longTaskListener = new LongTaskListener() {

            final int limit = 100;
            int count;

            public void taskFinished(LongTask task) {
                System.out.println("Finished" + (++count));
                if (count == limit) {
                    return;
                }
                LongTaskTest l = new LongTaskTest();
                longTaskExecutor.execute(l, l);
            }
        };
        longTaskExecutor.setLongTaskListener(longTaskListener);
        LongTaskTest longTaskTest = new LongTaskTest();
        longTaskExecutor.execute(longTaskTest, longTaskTest);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static class LongTaskTest implements LongTask, Runnable {

        public void run() {
            long sleep = (long) (Math.random() * 10);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        public boolean cancel() {
            return true;
        }

        public void setProgressTicket(ProgressTicket progressTicket) {
        }
    }

    @org.junit.Test
    public void testExecute_4args() {
    }

    @org.junit.Test
    public void testExecute_LongTask_Runnable() {
    }

    @org.junit.Test
    public void testCancel() {
    }

    @org.junit.Test
    public void testIsRunning() {
    }

    @org.junit.Test
    public void testSetLongTaskListener() {
    }

    @org.junit.Test
    public void testSetDefaultErrorHandler() {
    }
}