package io.github.thebesteric.framework.agile.commons.util;

import java.util.List;
import java.util.UUID;

/**
 * TransactionUtils
 *
 * @author Eric Joe
 * @since 1.0
 */
public class TransactionUtils extends AbstractUtils {

    public static final List<String> TRACK_ID_NAMES = List.of("track-id", "x-track-id", "trans-id", "x-trans-id", "trace-id", "x-trace-id", "transaction-id", "x-transaction-id");

    private static ThreadLocal<String> trackIdThreadLocal = new InheritableThreadLocal<>();

    static {
        initialize();
    }

    public static ThreadLocal<String> create() {
        return create(UUID.randomUUID().toString());
    }

    public static ThreadLocal<String> create(String trackId) {
        if (trackIdThreadLocal != null) {
            trackIdThreadLocal = null;
        }
        return ThreadLocal.withInitial(() -> trackId);
    }

    public static void createIfNecessary() {
        if (trackIdThreadLocal == null) {
            initialize();
        }
    }

    public static String get() {
        createIfNecessary();
        return trackIdThreadLocal.get();
    }

    public static void set(String trackId) {
        createIfNecessary();
        trackIdThreadLocal.set(trackId);
    }

    public static void clear() {
        if (trackIdThreadLocal != null) {
            trackIdThreadLocal.remove();
        }
        trackIdThreadLocal = null;
    }

    public static void initialize() {
        trackIdThreadLocal = create();
    }

    public static void initialize(String trackId) {
        trackIdThreadLocal = create(trackId);
    }

    public static boolean hasTrackIdInRequestHeader(String headerName) {
        for (String name : TRACK_ID_NAMES) {
            if (headerName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
