package io.github.md5sha256.democracypost.serializer;

import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ItemStackSerializer implements TypeSerializer<ItemStack> {

    @Override
    public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
        byte[] bytes = node.get(byte[].class);
        if (bytes == null) {
            return null;
        }
        try {
            return ItemStack.deserializeBytes(bytes);
        } catch (Exception ex) {
            throw new SerializationException(ex);
        }
    }

    @Override
    public void serialize(Type type, @Nullable ItemStack obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(byte[].class, null);
            return;
        }
        node.set(byte[].class, obj.serializeAsBytes());
    }
}
