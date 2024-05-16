package io.github.md5sha256.democracypost.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public class ScalarComponentSerializer implements TypeSerializer<Component> {

    private static final ScalarComponentSerializer LEGACY_AMPERSAND =
            new ScalarComponentSerializer(LegacyComponentSerializer.legacyAmpersand());

    private static final ScalarComponentSerializer MINI_MESSAGE =
            new ScalarComponentSerializer(MiniMessage.miniMessage());


    private final ComponentSerializer<Component, ? extends Component, String> serializer;

    public ScalarComponentSerializer(@Nonnull ComponentSerializer<Component, ? extends Component, String> serializer) {
        this.serializer = serializer;
    }

    @Override
    public Component deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String s = node.getString();
        if (s == null) {
            return Component.empty();
        }
        try {
            return this.serializer.deserialize(s);
        } catch (Exception ex) {
            throw new SerializationException(ex);
        }
    }

    @Override
    public void serialize(Type type, @Nullable Component obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(String.class, null);
            return;
        }
        node.set(String.class, this.serializer.serialize(obj));
    }

    @Nonnull
    public static ScalarComponentSerializer forMiniMessage() {
        return MINI_MESSAGE;
    }

    @Nonnull
    public static ScalarComponentSerializer forLegacyAmpersand() {
        return LEGACY_AMPERSAND;
    }

    @Override
    public @Nullable Component emptyValue(Type specificType, ConfigurationOptions options) {
        return Component.empty();
    }
}
