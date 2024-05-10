package io.github.md5sha256.democracypost.serializer;

import io.github.md5sha256.democracypost.PostalPackage;
import io.github.md5sha256.democracypost.database.UserState;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import javax.annotation.Nonnull;

public class Serializers {

    private Serializers() {

    }

    @Nonnull
    public static TypeSerializerCollection defaults() {
        return TypeSerializerCollection.defaults().childBuilder()
                .registerExact(ItemStack.class, new ItemStackSerializer())
                .registerExact(PostalPackage.class, new PostalPackageSerializer())
                .registerExact(UserState.class, new UserStateSerializer())
                .build();
    }

}
