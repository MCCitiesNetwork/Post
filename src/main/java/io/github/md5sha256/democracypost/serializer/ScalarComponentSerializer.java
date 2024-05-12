package io.github.md5sha256.democracypost.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.function.Predicate;

public class ScalarComponentSerializer extends ScalarSerializer<Component> {

    private static final ScalarComponentSerializer LEGACY_AMPERSAND =
            new ScalarComponentSerializer(LegacyComponentSerializer.legacyAmpersand());

    private static final ScalarComponentSerializer MINI_MESSAGE =
            new ScalarComponentSerializer(MiniMessage.miniMessage());


    private final ComponentSerializer<Component, ? extends Component, String> serializer;

    public ScalarComponentSerializer(@Nonnull ComponentSerializer<Component, ? extends Component, String> serializer) {
        super(Component.class);
        this.serializer = serializer;
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
    public Component deserialize(Type type, Object obj) throws SerializationException {
        if (obj == null) {
            return Component.empty();
        }
        if (obj instanceof String s) {
            try {
                return this.serializer.deserialize(s);
            } catch (Exception ex) {
                throw new SerializationException(ex);
            }
        }
        throw new SerializationException("Unsupported type: " + type.getTypeName());
    }

    @Override
    protected Object serialize(Component item, Predicate<Class<?>> typeSupported) {
        return this.serializer.serialize(item);
    }
}
