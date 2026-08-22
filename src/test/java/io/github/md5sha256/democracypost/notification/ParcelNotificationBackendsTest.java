package io.github.md5sha256.democracypost.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.md5sha256.democracypost.notification.ParcelNotificationBackends.Choice;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParcelNotificationBackendsTest {

    @Test
    @DisplayName("auto prefers PlayerNotifications when its service is registered")
    void autoPrefersPlayerNotifications() {
        assertEquals(Choice.PLAYER_NOTIFICATIONS,
                ParcelNotificationBackends.choose(NotificationBackend.AUTO, true, true));
    }

    @Test
    @DisplayName("auto falls back to Essentials when PlayerNotifications is absent")
    void autoFallsBackToEssentials() {
        assertEquals(Choice.ESSENTIALS,
                ParcelNotificationBackends.choose(NotificationBackend.AUTO, false, true));
    }

    @Test
    @DisplayName("auto yields no backend when neither plugin is present")
    void autoYieldsNoneWhenNothingAvailable() {
        assertEquals(Choice.NONE,
                ParcelNotificationBackends.choose(NotificationBackend.AUTO, false, false));
    }

    @Test
    @DisplayName("an explicit Essentials choice is honoured even when PlayerNotifications is available")
    void explicitEssentialsWinsOverAvailablePlayerNotifications() {
        assertEquals(Choice.ESSENTIALS,
                ParcelNotificationBackends.choose(NotificationBackend.ESSENTIALS, true, true));
    }

    @Test
    @DisplayName("an explicit choice whose backend is missing yields no backend rather than silently substituting")
    void explicitChoiceIsNotSilentlySubstituted() {
        assertEquals(Choice.NONE,
                ParcelNotificationBackends.choose(NotificationBackend.PLAYER_NOTIFICATIONS, false, true));
        assertEquals(Choice.NONE,
                ParcelNotificationBackends.choose(NotificationBackend.ESSENTIALS, true, false));
    }
}
