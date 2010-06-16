package org.gephi.data.attributes;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;
import org.gephi.data.properties.PropertiesColumn;
import org.openide.util.Lookup;


public class PropertyColumnToAttributeValueDelegateProviderMapper {
    private static PropertyColumnToAttributeValueDelegateProviderMapper instance;

    private final Map<PropertiesColumn, AttributeValueDelegateProvider> mapper;


    private PropertyColumnToAttributeValueDelegateProviderMapper() {
        mapper = new HashMap<PropertiesColumn, AttributeValueDelegateProvider>();

        Collection<? extends AttributeValueDelegateProvider> providers =
                Lookup.getDefault().lookupAll(AttributeValueDelegateProvider.class);

        for (AttributeValueDelegateProvider provider : providers)
            mapper.put(provider.getDelegateIdColumn(), provider);
    }


    public static PropertyColumnToAttributeValueDelegateProviderMapper getInstance() {
        if (instance == null)
            instance = new PropertyColumnToAttributeValueDelegateProviderMapper();

        return instance;
    }

    public AttributeValueDelegateProvider get(PropertiesColumn propertiesColumn) {
        return mapper.get(propertiesColumn);
    }
}
