package org.gephi.releasetests;

import java.time.Duration;
import org.junit.Test;

public class SmokeIT {

    @Test
    public void testSmoke() throws Exception {
        GephiRun.run("smoke", Duration.ofMinutes(5)).assertPassed();
    }
}
