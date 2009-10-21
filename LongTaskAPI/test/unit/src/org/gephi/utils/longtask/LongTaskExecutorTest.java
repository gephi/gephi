/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.utils.longtask;

import org.gephi.utils.progress.ProgressTicket;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
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
}