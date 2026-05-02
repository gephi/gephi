package org.gephi.preview.utils;

import java.util.Map;
import org.gephi.preview.api.Item;

public class MockItem implements Item {

    private final Object source;
    private final String type;
    private final Map<String, Object> data;

    public MockItem(Object source, String type, Map<String, Object> data) {
        this.source = source;
        this.type = type;
        this.data = data;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public <D> D getData(String key) {
        return (D) data.get(key);
    }

    @Override
    public boolean hasData(String key) {
        return data.containsKey(key);
    }

    @Override
    public void setData(String key, Object value) {
        data.put(key, value);
    }

    @Override
    public String[] getKeys() {
        return data.keySet().toArray(new String[0]);
    }
}
