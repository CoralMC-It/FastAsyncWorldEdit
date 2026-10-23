package com.fastasyncworldedit.core.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class FoliaSupport {
    private FoliaSupport() {

    }

    private static final boolean IS_FOLIA;
    private static final Class<?> TICK_THREAD_CLASS;
    private static final MethodHandle IS_GLOBAL_TICK_THREAD;
    static {
        boolean isFolia = false;
        try {
            // Assume implementation details are present
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (Exception unused) {

        }
        IS_FOLIA = isFolia;
        Class<?> tickThreadClass = Void.class;
        MethodHandle isGlobalTickThread = null;
        if (IS_FOLIA) {
            try {
                tickThreadClass = Class.forName("ca.spottedleaf.moonrise.common.util.TickThread");
                // core does not depend on Bukkit; resolve the Folia API method reflectively
                isGlobalTickThread = MethodHandles.publicLookup().findStatic(
                        Class.forName("org.bukkit.Bukkit"),
                        "isGlobalTickThread",
                        MethodType.methodType(boolean.class)
                );
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
        TICK_THREAD_CLASS = tickThreadClass;
        IS_GLOBAL_TICK_THREAD = isGlobalTickThread;
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static boolean isTickThread() {
        return TICK_THREAD_CLASS.isInstance(Thread.currentThread());
    }

    /**
     * {@return whether the current thread is ticking Folia's global region}. Always {@code false} when not on Folia.
     */
    public static boolean isGlobalTickThread() {
        if (IS_GLOBAL_TICK_THREAD == null) {
            return false;
        }
        return getRethrowing(() -> (boolean) IS_GLOBAL_TICK_THREAD.invokeExact());
    }


    @FunctionalInterface
    public interface ThrowingSupplier<T> {

        T get() throws Throwable;

    }
    @FunctionalInterface
    public interface ThrowingRunnable {

        void run() throws Throwable;

    }

    public static void runRethrowing(ThrowingRunnable runnable) {
        getRethrowing(() -> {
            runnable.run();
            return null;
        });
    }

    public static <T> T getRethrowing(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
