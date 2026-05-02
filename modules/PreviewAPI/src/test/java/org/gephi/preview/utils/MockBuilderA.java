package org.gephi.preview.utils;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;
import org.gephi.preview.spi.ItemBuilder;

public class MockBuilderA implements ItemBuilder {

    public static final String TYPE = "mock";
    public static final Object SOURCE_1 = new Object();
    public static final MockItem MOCK_ITEM_1 = new MockItem(SOURCE_1, TYPE, new HashMap<>(Map.of("foo", "bar", "number", 42)));
    public static final MockItem MOCK_ITEM_2 = new MockItem(new Object(), TYPE, new HashMap<>());

    @Override
    public Item[] getItems(Graph graph) {
        return new Item[] { MOCK_ITEM_1, MOCK_ITEM_2 };
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
