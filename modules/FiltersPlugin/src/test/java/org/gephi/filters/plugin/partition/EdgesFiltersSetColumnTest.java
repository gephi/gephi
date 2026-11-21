package org.gephi.filters.plugin.partition;

import java.lang.reflect.Proxy;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.graph.api.Column;
import org.junit.Assert;
import org.junit.Test;

public class EdgesFiltersSetColumnTest {

    private static Column column() {
        return (Column) Proxy.newProxyInstance(Column.class.getClassLoader(), new Class<?>[] {Column.class},
            (proxy, method, args) -> method.getName().equals("hashCode") ? System.identityHashCode(proxy) :
                method.getName().equals("equals") ? proxy == args[0] : null);
    }

    private static Partition partition(Column column) {
        return (Partition) Proxy.newProxyInstance(Partition.class.getClassLoader(), new Class<?>[] {Partition.class},
            (proxy, method, args) -> method.getName().equals("getColumn") ? column : null);
    }

    private static AppearanceModel model() {
        return (AppearanceModel) Proxy.newProxyInstance(AppearanceModel.class.getClassLoader(),
            new Class<?>[] {AppearanceModel.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getNodeFunctions":
                        return new Function[0];
                    case "getNodePartition":
                        return partition((Column) args[0]);
                    default:
                        return null;
                }
            });
    }

    @Test
    public void testIntraEdgesSetColumnRebindsPartition() {
        Column first = column();
        Column second = column();
        IntraEdgesBuilder.IntraEdgesFilter filter =
            new IntraEdgesBuilder.IntraEdgesFilter(model(), partition(first));
        filter.setColumn(second);
        Assert.assertSame(second, filter.getColumn());
    }

    @Test
    public void testInterEdgesSetColumnRebindsPartition() {
        Column first = column();
        Column second = column();
        InterEdgesBuilder.InterEdgesFilter filter =
            new InterEdgesBuilder.InterEdgesFilter(model(), partition(first));
        filter.setColumn(second);
        Assert.assertSame(second, filter.getColumn());
    }
}
