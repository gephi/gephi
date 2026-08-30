package org.gephi.filters.plugin.partition;

import org.junit.Assert;
import org.junit.Test;

public class PartitionBuilderTest {

    @Test
    public void testSetPartsWithNullFallsBackToEmptySet() {
        PartitionBuilder.PartitionFilter filter = new PartitionBuilder.NodePartitionFilter(null, null);
        filter.setParts(null);
        Assert.assertNotNull(filter.getParts());
        Assert.assertTrue(filter.getParts().isEmpty());
    }
}
