package io.github.md5sha256.democracypost.notification;

import io.github.md5sha256.democracypost.localization.MessageContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParcelNotificationRendererTest {

    private static final UUID SENDER = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID RECIPIENT = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID PARCEL = UUID.fromString("00000000-0000-0000-0000-00000000000c");

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    @DisplayName("the body substitutes the sender's name into the configured new-parcel message")
    void bodySubstitutesSenderName() {
        MessageContainer messages = new MessageContainer();
        messages.setMessage("messages.new-parcel", Component.text("%player% sent you a new parcel!"));
        ParcelNotificationRenderer renderer = new ParcelNotificationRenderer(messages);

        RenderedParcel rendered = renderer.render(
                new ParcelPayload(SENDER, "Alice", PARCEL), RECIPIENT);

        assertEquals("Alice sent you a new parcel!", plain(rendered.body()));
    }

    @Test
    @DisplayName("the title comes from the configured new-parcel-title message")
    void titleComesFromConfiguredMessage() {
        MessageContainer messages = new MessageContainer();
        messages.setMessage("messages.new-parcel-title", Component.text("New parcel"));
        ParcelNotificationRenderer renderer = new ParcelNotificationRenderer(messages);

        RenderedParcel rendered = renderer.render(
                new ParcelPayload(SENDER, "Alice", PARCEL), RECIPIENT);

        assertEquals("New parcel", plain(rendered.title()));
    }

    @Test
    @DisplayName("a message with no placeholder renders unchanged rather than gaining the sender's name")
    void messageWithoutPlaceholderIsLeftAlone() {
        MessageContainer messages = new MessageContainer();
        messages.setMessage("messages.new-parcel", Component.text("You have a new parcel!"));
        ParcelNotificationRenderer renderer = new ParcelNotificationRenderer(messages);

        RenderedParcel rendered = renderer.render(
                new ParcelPayload(SENDER, "Alice", PARCEL), RECIPIENT);

        assertEquals("You have a new parcel!", plain(rendered.body()));
    }
}
