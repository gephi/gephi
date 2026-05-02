package org.gephi.preview.utils;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;
import org.gephi.preview.spi.ItemBuilder;

public class MockBuilderB implements ItemBuilder {

    public static final MockItem MOCK_ITEM_1 = new MockItem(MockBuilderA.SOURCE_1, MockBuilderA.TYPE, new HashMap<>(Map.of("bar", "foo", "number", 999)));
    public static final MockItem MOCK_ITEM_2 = new MockItem(new Object(), MockBuilderA.TYPE, new HashMap<>());

    @Override
    public Item[] getItems(Graph graph) {
        return new Item[] { MOCK_ITEM_1, MOCK_ITEM_2 };
    }

    @Override
    public String getType() {
        return MockBuilderA.TYPE;
    }
}
