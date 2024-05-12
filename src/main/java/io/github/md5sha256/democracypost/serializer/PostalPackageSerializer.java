package io.github.md5sha256.democracypost.serializer;

import io.github.md5sha256.democracypost.model.PackageContent;
import io.github.md5sha256.democracypost.model.PostalPackage;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.UUID;

public class PostalPackageSerializer implements TypeSerializer<PostalPackage> {

    private static final String KEY_ID = "id";
    private static final String KEY_EXPIRY_DATE = "expiry-date";
    private static final String KEY_CONTENT = "content";

    @Override
    public PostalPackage deserialize(Type type, ConfigurationNode node) throws SerializationException {
        UUID uuid = node.node(KEY_ID).get(UUID.class);
        Date expiryDate = node.node(KEY_EXPIRY_DATE).get(Date.class);
        PackageContent content = node.node(KEY_CONTENT).get(PackageContent.class);
        if (uuid == null) {
            throw new SerializationException("Missing uuid");
        }
        if (expiryDate == null) {
            throw new SerializationException("Missing expiry date");
        }
        if (content == null) {
            throw new SerializationException("Missing content");
        }
        return new PostalPackage(uuid, expiryDate, content);
    }

    @Override
    public void serialize(Type type,
                          @Nullable PostalPackage obj,
                          ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.removeChild(KEY_ID);
            node.removeChild(KEY_EXPIRY_DATE);
            node.removeChild(KEY_CONTENT);
            return;
        }
        node.node(KEY_ID).set(obj.id());
        node.node(KEY_EXPIRY_DATE).set(obj.expiryDate());
        node.node(KEY_CONTENT).set(obj.content());
    }
}
