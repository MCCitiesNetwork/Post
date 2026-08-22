package io.github.md5sha256.democracypost.notification;

import io.github.md5sha256.playernotifications.api.TypedNotification;

import javax.annotation.Nonnull;

/**
 * The single operation {@link PlayerNotificationsService} needs from PlayerNotifications'
 * {@code NotificationService}.
 *
 * <p>Narrowing that ~20-method service to one call is what lets the backend be tested against a
 * short recording fake instead of a mock server.
 */
@FunctionalInterface
public interface ParcelEnqueuer {

    void enqueue(@Nonnull TypedNotification<ParcelPayload> notification, boolean overwriteAllowed);
}
