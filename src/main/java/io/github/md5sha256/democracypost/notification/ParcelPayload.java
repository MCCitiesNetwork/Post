package io.github.md5sha256.democracypost.notification;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

/**
 * The persisted form of a "new parcel" notice inside PlayerNotifications.
 *
 * <p>{@code senderName} is stored rather than looked up when the notice is read, following the
 * reasoning behind PlayerNotifications' own mail payload: a notice can sit unread in an inbox long
 * after the parcel was sent, and {@code OfflinePlayer#getName()} is a blocking lookup that returns
 * null for a player this server has never seen. The name the sender had at send time is both
 * cheaper and more truthful.
 *
 * <p>{@code parcelId} is what makes the notification key stable and unique per parcel; it is also
 * what a future click-to-open action would need.
 *
 * @param sender     the sending player's UUID
 * @param senderName the sending player's name at the time the parcel was posted
 * @param parcelId   the posted parcel's id
 */
public record ParcelPayload(@Nonnull UUID sender, @Nonnull String senderName, @Nonnull UUID parcelId) {

    /** The PlayerNotifications data type parcel notices are stored under. */
    public static final String DATA_TYPE = "democracypost.parcel";

    public ParcelPayload {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(senderName, "senderName");
        Objects.requireNonNull(parcelId, "parcelId");
        if (senderName.isBlank()) {
            throw new IllegalArgumentException("senderName must not be blank");
        }
    }
}
