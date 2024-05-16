package io.github.md5sha256.democracypost.serializer;

import io.github.md5sha256.democracypost.model.PostalPackage;
import io.github.md5sha256.democracypost.database.UserState;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import javax.annotation.Nonnull;
import java.util.Date;

public class Serializers {

    private Serializers() {

    }

    @Nonnull
    public static TypeSerializerCollection defaults() {
        return TypeSerializerCollection.defaults().childBuilder()
                .register(Component.class, ScalarComponentSerializer.forMiniMessage())
                .registerExact(Date.class, new DateSerializer())
                .registerExact(ItemStack.class, new ItemStackSerializer())
                .registerExact(PostalPackage.class, new PostalPackageSerializer())
                .registerExact(UserState.class, new UserStateSerializer())
                .build();
    }

}
