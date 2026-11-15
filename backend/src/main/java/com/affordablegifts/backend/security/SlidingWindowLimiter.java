package com.affordablegifts.backend.security;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory "at most N events per window" counter, keyed by client. */
class SlidingWindowLimiter {

    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final int limit;
    private final long windowNanos;
    private final ConcurrentHashMap<String, Deque<Long>> events = new ConcurrentHashMap<>();

    SlidingWindowLimiter(int limit, Duration window) {
        this.limit = limit;
        this.windowNanos = window.toNanos();
    }

    /** True while the client is still under the limit (does not record anything). */
    boolean isAllowed(String client) {
        Deque<Long> times = events.get(client);
        if (times == null) {
            return true;
        }
        synchronized (times) {
            prune(times);
            return times.size() < limit;
        }
    }

    void record(String client) {
        if (events.size() > MAX_TRACKED_CLIENTS) {
            events.values().removeIf(times -> {
                synchronized (times) {
                    prune(times);
                    return times.isEmpty();
                }
            });
        }
        Deque<Long> times = events.computeIfAbsent(client, key -> new ArrayDeque<>());
        synchronized (times) {
            prune(times);
            times.addLast(System.nanoTime());
        }
    }

    private void prune(Deque<Long> times) {
        long cutoff = System.nanoTime() - windowNanos;
        while (!times.isEmpty() && times.peekFirst() < cutoff) {
            times.pollFirst();
        }
    }
}
