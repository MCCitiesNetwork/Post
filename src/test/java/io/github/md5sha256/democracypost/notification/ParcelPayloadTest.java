package io.github.md5sha256.democracypost.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ParcelPayloadTest {

    private static final UUID SENDER = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID PARCEL = UUID.fromString("00000000-0000-0000-0000-00000000000c");

    @Test
    @DisplayName("a blank sender name is rejected at construction rather than persisted")
    void blankSenderNameRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ParcelPayload(SENDER, "   ", PARCEL));
    }

    @Test
    @DisplayName("a null sender name is rejected at construction")
    void nullSenderNameRejected() {
        assertThrows(NullPointerException.class,
                () -> new ParcelPayload(SENDER, null, PARCEL));
    }
}
