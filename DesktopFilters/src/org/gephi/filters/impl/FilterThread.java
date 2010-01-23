/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.impl;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.filters.api.PropertyExecutor.Callback;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.FilterProperty;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterThread extends Thread {

    private FilterModelImpl model;
    private AtomicReference<AbstractQueryImpl> rootQuery;
    ConcurrentHashMap<String, PropertyModifier> modifiersMap;
    private boolean running = true;
    private final Object lock = new Object();

    public FilterThread(FilterModelImpl model) {
        super("Filter Thread");
        this.model = model;
        rootQuery = new AtomicReference<AbstractQueryImpl>();
        modifiersMap = new ConcurrentHashMap<String, PropertyModifier>();
    }

    @Override
    public void run() {
        while (running) {
            AbstractQueryImpl q;
            while ((q = rootQuery.getAndSet(null)) == null) {
                try {
                    synchronized (this.lock) {
                        lock.wait();
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            boolean propertyModified = false;
            for (Iterator<PropertyModifier> itr = modifiersMap.values().iterator(); itr.hasNext();) {
                PropertyModifier pm = itr.next();
                itr.remove();
                pm.callback.setValue(pm.value);
                propertyModified = true;
            }
            if (propertyModified) {
                model.updateParameters(q);
            }
            try {
                System.out.println("filter query " + q.getName());
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        //clear map
        Query q = null;
        for (Iterator<PropertyModifier> itr = modifiersMap.values().iterator(); itr.hasNext();) {
            PropertyModifier pm = itr.next();
            pm.callback.setValue(pm.value);
            q = pm.query;
        }
        modifiersMap.clear();
        if (q != null) {
            model.updateParameters(q);
        }
    }

    public void setRootQuery(AbstractQueryImpl rootQuery) {
        this.rootQuery.set(rootQuery);
        synchronized (this.lock) {
            lock.notify();
        }
    }

    public AbstractQueryImpl getRootQuery() {
        return rootQuery.get();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void addModifier(PropertyModifier modifier) {
        modifiersMap.put(modifier.property.getName(), modifier);
    }

    protected static class PropertyModifier {

        protected final Object value;
        protected final Callback callback;
        protected final FilterProperty property;
        protected final Query query;

        public PropertyModifier(Query query, FilterProperty property, Object value, Callback callback) {
            this.query = query;
            this.property = property;
            this.value = value;
            this.callback = callback;
        }
    }
}
