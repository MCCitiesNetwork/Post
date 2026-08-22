package io.github.md5sha256.democracypost.notification;

import io.github.md5sha256.playernotifications.api.TypedNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PlayerNotificationsServiceTest {

    private static final UUID SENDER = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID RECIPIENT = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID PARCEL = UUID.fromString("00000000-0000-0000-0000-00000000000c");
    private static final Instant EXPIRY = Instant.parse("2030-01-01T00:00:00Z");

    /** Records what was enqueued, so the service is testable without a running server. */
    private static final class RecordingEnqueuer implements ParcelEnqueuer {

        private final List<TypedNotification<ParcelPayload>> enqueued = new ArrayList<>();
        private final List<Boolean> overwriteFlags = new ArrayList<>();

        @Override
        public void enqueue(TypedNotification<ParcelPayload> notification, boolean overwriteAllowed) {
            this.enqueued.add(notification);
            this.overwriteFlags.add(overwriteAllowed);
        }

        private TypedNotification<ParcelPayload> only() {
            assertEquals(1, this.enqueued.size(), "expected exactly one enqueued notification");
            return this.enqueued.getFirst();
        }
    }

    private static RecordingEnqueuer notifyOnce() {
        RecordingEnqueuer enqueuer = new RecordingEnqueuer();
        new PlayerNotificationsService(enqueuer)
                .notifyNewParcel(RECIPIENT, SENDER, "Alice", PARCEL, EXPIRY);
        return enqueuer;
    }

    @Test
    @DisplayName("the notification targets only the recipient, under the parcel data type")
    void targetsRecipientUnderParcelDataType() {
        TypedNotification<ParcelPayload> notification = notifyOnce().only();

        assertEquals(List.of(RECIPIENT), notification.notifTarget().playerUUIDs());
        assertEquals(ParcelPayload.DATA_TYPE, notification.notifPayloadType());
    }

    @Test
    @DisplayName("the payload carries the sender and the parcel it announces")
    void payloadCarriesSenderAndParcel() {
        TypedNotification<ParcelPayload> notification = notifyOnce().only();

        assertEquals(new ParcelPayload(SENDER, "Alice", PARCEL), notification.notifPayload());
    }

    @Test
    @DisplayName("the notification expires with the parcel it announces")
    void expiresWithTheParcel() {
        assertEquals(EXPIRY, notifyOnce().only().notifExpiryTime());
    }

    @Test
    @DisplayName("the key is derived from the parcel id, so two parcels never share one inbox entry")
    void keyIsDerivedFromTheParcelId() {
        RecordingEnqueuer enqueuer = new RecordingEnqueuer();
        PlayerNotificationsService service = new PlayerNotificationsService(enqueuer);
        UUID otherParcel = UUID.fromString("00000000-0000-0000-0000-00000000000d");

        service.notifyNewParcel(RECIPIENT, SENDER, "Alice", PARCEL, EXPIRY);
        service.notifyNewParcel(RECIPIENT, SENDER, "Alice", otherParcel, EXPIRY);

        String first = enqueuer.enqueued.get(0).notifKey();
        String second = enqueuer.enqueued.get(1).notifKey();
        assertEquals(ParcelPayload.DATA_TYPE + ":" + PARCEL, first);
        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("notices never overwrite an earlier one: a second parcel is a second event")
    void noticesNeverOverwrite() {
        assertFalse(notifyOnce().overwriteFlags.getFirst());
    }
}
