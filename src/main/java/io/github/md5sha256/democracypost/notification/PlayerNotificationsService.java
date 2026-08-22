package io.github.md5sha256.democracypost.notification;

import io.github.md5sha256.playernotifications.api.NotificationTarget;
import io.github.md5sha256.playernotifications.api.TypedNotification;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Delivers parcel notices through PlayerNotifications, which fans each one out to whichever media
 * the recipient prefers — chat, dialog, Essentials mail, Discord — and keeps it in their inbox
 * until they dismiss it.
 *
 * <p>This class names PlayerNotifications API types directly, so it must only be constructed once
 * that plugin is known to be present; {@link ParcelNotificationBackends} is what decides that.
 */
public class PlayerNotificationsService implements ParcelNotificationService {

    private final ParcelEnqueuer enqueuer;

    public PlayerNotificationsService(@Nonnull ParcelEnqueuer enqueuer) {
        this.enqueuer = Objects.requireNonNull(enqueuer, "enqueuer");
    }

    @Override
    public void notifyNewParcel(@Nonnull UUID receiver,
                                @Nonnull UUID sender,
                                @Nonnull String senderName,
                                @Nonnull UUID parcelId,
                                @Nonnull Instant expiry) {
        TypedNotification<ParcelPayload> notification = new TypedNotification<>(
                notificationKey(parcelId),
                Instant.now(),
                expiry,
                new NotificationTarget(List.of(receiver)),
                ParcelPayload.DATA_TYPE,
                new ParcelPayload(sender, senderName, parcelId),
                0);
        // overwriteAllowed is false: a second parcel is a second thing that happened, not a
        // correction of the first, so it may never replace an earlier notice in the inbox. The
        // parcel-derived key means re-posting the same parcel is the only way to collide at all.
        this.enqueuer.enqueue(notification, false);
    }

    @Nonnull
    static String notificationKey(@Nonnull UUID parcelId) {
        return ParcelPayload.DATA_TYPE + ":" + parcelId;
    }
}
