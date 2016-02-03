package com.excelian.mache.vertx;

/**
 * The timer service provides a mechanism to run a task after a given period.
 */
@FunctionalInterface
public interface TimerService {

    /**
     * Run the specified task after the given period has elapsed.
     *
     * @param timeMs The time to wait in milliseconds
     * @param action The action to run
     */
    void runAfterPeriod(long timeMs, Runnable action);
}
