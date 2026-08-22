package com.minidmart.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory brute-force guard: after MAX_ATTEMPTS failed logins for an email,
 * further attempts are locked out for LOCKOUT_MILLIS. Deliberately simple
 * (no persistence/clustering) — adequate for a single-instance demo deployment.
 */
public final class LoginAttemptTracker {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MILLIS = 5 * 60 * 1000L;

    private static final ConcurrentHashMap<String, AtomicInteger> FAILURES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> LOCKED_UNTIL = new ConcurrentHashMap<>();

    private LoginAttemptTracker() {
    }

    public static boolean isLocked(String email) {
        Long until = LOCKED_UNTIL.get(key(email));
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            LOCKED_UNTIL.remove(key(email));
            FAILURES.remove(key(email));
            return false;
        }
        return true;
    }

    public static void recordFailure(String email) {
        String k = key(email);
        int count = FAILURES.computeIfAbsent(k, x -> new AtomicInteger()).incrementAndGet();
        if (count >= MAX_ATTEMPTS) {
            LOCKED_UNTIL.put(k, System.currentTimeMillis() + LOCKOUT_MILLIS);
        }
    }

    public static void recordSuccess(String email) {
        String k = key(email);
        FAILURES.remove(k);
        LOCKED_UNTIL.remove(k);
    }

    public static int remainingLockoutSeconds(String email) {
        Long until = LOCKED_UNTIL.get(key(email));
        if (until == null) return 0;
        return (int) Math.max(0, (until - System.currentTimeMillis()) / 1000);
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
