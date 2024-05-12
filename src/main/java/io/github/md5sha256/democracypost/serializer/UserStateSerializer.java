package io.github.md5sha256.democracypost.serializer;

import io.github.md5sha256.democracypost.model.PostalPackage;
import io.github.md5sha256.democracypost.database.UserState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserStateSerializer implements TypeSerializer<UserState> {

    private static final String KEY_UUID = "uuid";
    private static final String KEY_PACKAGES = "packages";

    @Override
    public UserState deserialize(Type type, ConfigurationNode node) throws SerializationException {
        List<PostalPackage> packages = node.node(KEY_PACKAGES).getList(PostalPackage.class);
        UUID uuid = node.node(KEY_UUID).get(UUID.class);
        if (uuid == null) {
            throw new SerializationException("Missing uuid");
        }
        UserState userState = new UserState(uuid);
        if (packages != null) {
            packages.forEach(userState::addPackage);
        }
        return userState;
    }

    @Override
    public void serialize(Type type, @Nullable UserState obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.removeChild(KEY_PACKAGES);
            return;
        }
        node.node(KEY_UUID).set(obj.getUuid());
        node.node(KEY_PACKAGES).setList(PostalPackage.class, new ArrayList<>(obj.packages()));
    }
}
