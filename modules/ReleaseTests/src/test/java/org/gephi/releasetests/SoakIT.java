package org.gephi.releasetests;

import java.time.Duration;
import org.junit.Test;

public class SoakIT {

    @Test
    public void testSoak() throws Exception {
        GephiRun.run("soak", SoakSuite.DURATION.plus(Duration.ofMinutes(4))).assertPassed();
    }
}
