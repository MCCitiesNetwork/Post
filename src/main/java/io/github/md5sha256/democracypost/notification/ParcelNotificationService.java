package io.github.md5sha256.democracypost.notification;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.UUID;

/**
 * Tells a player a parcel is waiting for them. The medium — Essentials mail, PlayerNotifications,
 * or nothing at all — is chosen once at startup by {@link ParcelNotificationBackends}; call sites
 * never learn which one they got.
 */
public interface ParcelNotificationService {

    /**
     * Notifies {@code receiver} that {@code sender} has posted them a parcel.
     *
     * @param receiver   the player the parcel is addressed to
     * @param sender     the posting player's UUID
     * @param senderName the posting player's name at the time of posting
     * @param parcelId   the posted parcel's id
     * @param expiry     when the parcel itself expires, so a notice cannot outlive what it announces
     */
    void notifyNewParcel(@Nonnull UUID receiver,
                         @Nonnull UUID sender,
                         @Nonnull String senderName,
                         @Nonnull UUID parcelId,
                         @Nonnull Instant expiry);
}
