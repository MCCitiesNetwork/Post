package io.github.md5sha256.democracypost.notification;

import io.github.md5sha256.playernotifications.api.NotificationTarget;
import io.github.md5sha256.playernotifications.api.TypedNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveringParcelEnqueuerTest {

    private static final TypedNotification<ParcelPayload> NOTIFICATION = new TypedNotification<>(
            "democracypost:parcel:test",
            Instant.parse("2030-01-01T00:00:00Z"),
            Instant.parse("2030-01-02T00:00:00Z"),
            new NotificationTarget(List.of(UUID.randomUUID())),
            ParcelPayload.DATA_TYPE,
            new ParcelPayload(UUID.randomUUID(), "Alice", UUID.randomUUID()),
            0);

    /** Captures what the enqueuer logs, so a test can see whether a failure was reported. */
    private static final class RecordingHandler extends Handler {

        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            this.records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }

    private static Logger loggerWith(RecordingHandler handler) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        return logger;
    }

    @Test
    @DisplayName("the notification and overwrite flag are handed to the delivery unchanged")
    void passesNotificationThrough() {
        List<TypedNotification<ParcelPayload>> delivered = new ArrayList<>();
        List<Boolean> flags = new ArrayList<>();
        DeliveringParcelEnqueuer enqueuer = new DeliveringParcelEnqueuer((notification, overwrite) -> {
            delivered.add(notification);
            flags.add(overwrite);
            return CompletableFuture.completedFuture(null);
        }, loggerWith(new RecordingHandler()));

        enqueuer.enqueue(NOTIFICATION, false);

        assertEquals(1, delivered.size());
        assertSame(NOTIFICATION, delivered.getFirst());
        assertEquals(List.of(false), flags);
    }

    @Test
    @DisplayName("a delivery that completes normally logs nothing")
    void successLogsNothing() {
        RecordingHandler handler = new RecordingHandler();
        new DeliveringParcelEnqueuer((notification, overwrite) -> CompletableFuture.completedFuture(null),
                loggerWith(handler))
                .enqueue(NOTIFICATION, false);

        assertTrue(handler.records.isEmpty());
    }

    @Test
    @DisplayName("a delivery that fails later is logged as a warning naming the notification")
    void failureIsLogged() {
        RecordingHandler handler = new RecordingHandler();
        CompletableFuture<Void> pending = new CompletableFuture<>();
        new DeliveringParcelEnqueuer((notification, overwrite) -> pending, loggerWith(handler))
                .enqueue(NOTIFICATION, false);

        assertTrue(handler.records.isEmpty(), "nothing is logged before the delivery finishes");
        IllegalStateException cause = new IllegalStateException("discord down");
        pending.completeExceptionally(cause);

        assertEquals(1, handler.records.size());
        LogRecord record = handler.records.getFirst();
        assertEquals(Level.WARNING, record.getLevel());
        assertTrue(record.getMessage().contains(NOTIFICATION.notifKey()), record.getMessage());
        assertSame(cause, record.getThrown());
    }
}
