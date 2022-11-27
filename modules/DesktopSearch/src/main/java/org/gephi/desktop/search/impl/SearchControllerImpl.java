package org.gephi.desktop.search.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.gephi.desktop.search.api.SearchController;
import org.gephi.desktop.search.api.SearchListener;
import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.api.SearchResult;
import org.gephi.desktop.search.spi.SearchProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SearchController.class)
public class SearchControllerImpl implements SearchController {

    private final ExecutorService pool;
    private final List<Future<Void>> currentSearch = new ArrayList<>();
    private SearchSession currentSession;
    private SearchListener currentListener;
    private final static int MAX_RESULTS = 10;

    public SearchControllerImpl() {
        pool = Executors.newCachedThreadPool();
    }

    protected void shutdown() {
        pool.shutdown();
    }

    @Override
    public <T> List<SearchResult<T>> search(SearchRequest request, Class<T> typeFilter) {
        SearchSession<T> session = new SearchSession<>(request, Collections.singleton(typeFilter));

        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        getProviderTasks(request, session).stream().map(commonPool::submit).forEach(ForkJoinTask::join);

        return session.getResults();
    }

    @Override
    public void search(SearchRequest request, SearchListener listener) {
        synchronized (currentSearch) {
            // Cancel current search if exists
            currentSearch.forEach(f -> f.cancel(false));
            currentSearch.clear();
            if (currentSession != null) {
                if (currentSession.markObsolete()) {
                    currentListener.cancelled();
                }
            }

            // Create new search
            currentSession = new SearchSession<>(request);
            currentListener = listener;
            currentListener.started(request);

            // Submit provider tasks
            getProviderTasks(request, currentSession).stream()
                .map(r -> pool.submit((Runnable) r)).forEach(f -> currentSearch.add((Future<Void>) f));

            // Join task
            final List<Future<Void>> providerTasks = currentSearch;
            final SearchSession session = currentSession;
            pool.submit(() -> {
                for (Future<Void> f : providerTasks) {
                    try {
                        f.get();
                    } catch (CancellationException | InterruptedException ex) {
                        // ignore
                    } catch (ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                if (!session.isObsolete()) {
                    listener.finished(session.request, session.getResults());
                }
            });
        }
    }

    protected <T> List<Runnable> getProviderTasks(SearchRequest request, SearchSession<T> session) {
        List<Runnable> tasks = new ArrayList<>();
        int position = 0;
        for (SearchProvider<T> provider : Lookup.getDefault().lookupAll(SearchProvider.class)) {
            final int providerPosition = position++;
            tasks.add(() -> {
                SearchResultsBuilderImpl<T> resultsBuilder =
                    new SearchResultsBuilderImpl<>(provider, providerPosition, MAX_RESULTS);
                session.addBuilder(resultsBuilder);
                provider.search(request, resultsBuilder);

                session.addResult(resultsBuilder.getResults());
            });
        }
        return tasks;
    }

    private static class SearchSession<T> {

        final SearchRequest request;
        final Set<Class<T>> classFilters;
        final Map<T, SearchResultImpl<T>> resultSet;
        final Queue<SearchResultsBuilderImpl<T>> builders;
        volatile boolean obsolete;
        volatile boolean finished;

        public SearchSession(SearchRequest request) {
            this(request, Collections.emptySet());
        }

        public SearchSession(SearchRequest request, Set<Class<T>> classFilters) {
            this.request = request;
            this.classFilters = classFilters;
            this.resultSet = new ConcurrentHashMap<>();
            this.builders = new ConcurrentLinkedQueue<>();
        }

        protected void addBuilder(SearchResultsBuilderImpl<T> builder) {
            builders.add(builder);
        }

        protected boolean markObsolete() {
            this.obsolete = true;
            SearchResultsBuilderImpl<T> builder;
            while ((builder = builders.poll()) != null) {
                builder.markObsolete();
            }
            return !finished;
        }

        public boolean isObsolete() {
            return obsolete;
        }

        protected void addResult(List<SearchResultImpl<T>> results) {
            results.stream().filter(r -> passClassFilters(r.getResult()))
                .forEach(key -> resultSet.merge(key.getResult(), key, (oldValue, newValue) -> {
                    if (newValue.getPosition() < oldValue.getPosition()) {
                        return newValue;
                    } else {
                        return oldValue;
                    }
                }));
        }

        protected List<SearchResult<T>> getResults() {
            this.finished = true;
            return resultSet.values().stream().sorted().collect(Collectors.toList());
        }

        protected boolean passClassFilters(T result) {
            if (classFilters.isEmpty()) {
                return true;
            }
            for (Class<T> cls : classFilters) {
                if (cls.isAssignableFrom(result.getClass())) {
                    return true;
                }
            }
            return false;
        }
    }
}
