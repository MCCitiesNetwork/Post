package io.github.md5sha256.democracypost.notification;

import net.kyori.adventure.text.Component;

import javax.annotation.Nonnull;

/**
 * A parcel notice rendered to a medium-neutral title and body.
 *
 * <p>Mirrors PlayerNotifications' {@code RenderableNotification} without naming it. The
 * PlayerNotifications API is a {@code compileOnly} dependency, so a class whose signature mentions
 * it cannot be loaded on a server without the plugin installed; keeping the rendering itself
 * PN-free means only {@link PlayerNotificationsService} — constructed only behind the availability
 * gate — ever touches those types.
 *
 * @param title the notice's title
 * @param body  the notice's message
 */
public record RenderedParcel(@Nonnull Component title, @Nonnull Component body) {
}
