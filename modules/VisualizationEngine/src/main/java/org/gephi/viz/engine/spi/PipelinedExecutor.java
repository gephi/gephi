package org.gephi.viz.engine.spi;

/**
 *
 * @param <R>
 * @author Eduardo Ramos
 */
public interface PipelinedExecutor<R extends RenderingTarget> {

    String getCategory();

    int getPreferenceInCategory();

    String getName();

    default boolean isAvailable(R target) {
        return true;
    }

    void init(R target);

    default void dispose(R target) {
        //NOOP
    }

    int getOrder();

    class Comparator implements java.util.Comparator<PipelinedExecutor> {

        @Override
        public int compare(PipelinedExecutor o1, PipelinedExecutor o2) {
            return o1.getOrder() - o2.getOrder();
        }
    }
}
