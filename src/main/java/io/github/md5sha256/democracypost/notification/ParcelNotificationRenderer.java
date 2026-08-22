package io.github.md5sha256.democracypost.notification;

import io.github.md5sha256.democracypost.localization.MessageContainer;
import net.kyori.adventure.text.Component;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

/**
 * Turns a stored {@link ParcelPayload} back into the title and body a notification sink delivers.
 *
 * <p>Rendering happens on read, not on send, so an operator who edits {@code messages.new-parcel}
 * changes what unread notices say. That is the intent: the message is a template, and the payload
 * stores only the facts it substitutes into one.
 *
 * <p>Neither title nor body may depend on click events to be understood — Essentials mail and
 * Discord sinks flatten components to plain text and are free to drop interaction entirely.
 */
public class ParcelNotificationRenderer {

    /** Message key for the notice body. Shared with the Essentials backend, so both read alike. */
    public static final String BODY_MESSAGE_KEY = "messages.new-parcel";
    /** Message key for the notice title, which only the PlayerNotifications backend shows. */
    public static final String TITLE_MESSAGE_KEY = "messages.new-parcel-title";

    private final MessageContainer messageContainer;

    public ParcelNotificationRenderer(@Nonnull MessageContainer messageContainer) {
        this.messageContainer = Objects.requireNonNull(messageContainer, "messageContainer");
    }

    /**
     * Renders the notice for one recipient. The recipient is accepted because delivery is already
     * per-target and a future per-player personalisation would need it; today's message is the same
     * for everyone it reaches.
     */
    @Nonnull
    public RenderedParcel render(@Nonnull ParcelPayload payload, @Nonnull UUID target) {
        Component body = this.messageContainer.messageFor(BODY_MESSAGE_KEY)
                .replaceText(builder -> builder.matchLiteral("%player%")
                        .replacement(payload.senderName()));
        return new RenderedParcel(this.messageContainer.messageFor(TITLE_MESSAGE_KEY), body);
    }
}
