package io.github.md5sha256.democracypost.notification;

import javax.annotation.Nonnull;

/**
 * Resolves the configured {@link NotificationBackend} against what is actually installed.
 *
 * <p>Kept apart from the plugin bootstrap as a pure function of the configured value and two
 * availability flags, so the precedence rules can be tested without a running server.
 */
public final class ParcelNotificationBackends {

    private ParcelNotificationBackends() {
    }

    /**
     * The backend to construct.
     */
    public enum Choice {
        PLAYER_NOTIFICATIONS,
        ESSENTIALS,
        /**
         * No backend is usable; the caller substitutes a no-op and warns.
         */
        NONE
    }

    /**
     * Picks a backend.
     *
     * <p>An explicit choice is never silently substituted: asking for PlayerNotifications on a
     * server that does not have it yields {@link Choice#NONE} rather than quietly delivering
     * Essentials mail, because a server owner who configured one medium would not otherwise learn
     * their notices went somewhere else.
     */
    @Nonnull
    public static Choice choose(@Nonnull NotificationBackend configured,
                                boolean playerNotificationsAvailable,
                                boolean essentialsAvailable) {
        return switch (configured) {
            case PLAYER_NOTIFICATIONS -> playerNotificationsAvailable ? Choice.PLAYER_NOTIFICATIONS : Choice.NONE;
            case ESSENTIALS -> essentialsAvailable ? Choice.ESSENTIALS : Choice.NONE;
            case AUTO -> {
                if (playerNotificationsAvailable) {
                    yield Choice.PLAYER_NOTIFICATIONS;
                }
                yield essentialsAvailable ? Choice.ESSENTIALS : Choice.NONE;
            }
        };
    }
}
