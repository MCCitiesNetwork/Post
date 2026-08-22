package io.github.md5sha256.democracypost.notification;

/**
 * Which backend delivers "you have a new parcel" notices, as configured by
 * {@code post-settings.notification-backend}.
 */
public enum NotificationBackend {

    /**
     * Prefer PlayerNotifications when its service is registered, otherwise Essentials mail.
     */
    AUTO,
    /**
     * Always Essentials mail, even on a server also running PlayerNotifications.
     */
    ESSENTIALS,
    /**
     * Always PlayerNotifications.
     */
    PLAYER_NOTIFICATIONS

}
