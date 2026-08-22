package io.github.md5sha256.democracypost.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoopParcelNotificationServiceTest {

    private static final UUID SENDER = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID RECIPIENT = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID PARCEL = UUID.fromString("00000000-0000-0000-0000-00000000000c");

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

    @Test
    @DisplayName("undelivered notices are warned about once, not once per parcel")
    void warnsOnlyOnce() {
        Logger logger = Logger.getLogger("noop-parcel-test");
        logger.setUseParentHandlers(false);
        RecordingHandler handler = new RecordingHandler();
        logger.addHandler(handler);
        NoopParcelNotificationService service = new NoopParcelNotificationService(logger);

        service.notifyNewParcel(RECIPIENT, SENDER, "Alice", PARCEL, Instant.now());
        service.notifyNewParcel(RECIPIENT, SENDER, "Bob", UUID.randomUUID(), Instant.now());

        assertEquals(1, handler.records.size());
        assertEquals(Level.WARNING, handler.records.getFirst().getLevel());
    }
}
