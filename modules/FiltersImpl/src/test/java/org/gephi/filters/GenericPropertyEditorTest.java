package org.gephi.filters;

import java.util.HashSet;
import java.util.Set;
import org.gephi.filters.plugin.partition.PartitionBuilder;
import org.junit.Assert;
import org.junit.Test;

public class GenericPropertyEditorTest {

    @Test
    public void testPartitionFilterWithNullPartRoundTrip() {
        PartitionBuilder.PartitionFilter filter = new PartitionBuilder.NodePartitionFilter(null, null);
        filter.addPart(null);
        filter.addPart("foo");

        GenericPropertyEditor writer = new GenericPropertyEditor();
        writer.setValue(filter.getParts());
        String text = writer.getAsText();
        Assert.assertNotNull(text);
        Assert.assertNotEquals("null", text);

        GenericPropertyEditor reader = new GenericPropertyEditor();
        reader.setAsText(text);
        Assert.assertEquals(filter.getParts(), reader.getValue());
    }

    @Test
    public void testNotSerializableValueIsNotPersisted() {
        Set<Object> parts = new HashSet<>();
        parts.add(new Object());

        GenericPropertyEditor writer = new GenericPropertyEditor();
        writer.setValue(parts);
        Assert.assertNull(writer.getAsText());
    }
}
