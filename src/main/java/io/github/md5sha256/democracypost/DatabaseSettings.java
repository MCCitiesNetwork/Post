package io.github.md5sha256.democracypost;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import javax.annotation.Nonnull;

@ConfigSerializable
public record DatabaseSettings(
        @Setting("url")
        @Required
        @Nonnull String url,
        @Setting("username")
        @Nonnull String username,
        @Setting("password")
        @Nonnull String password,
        @Setting("require-auth")
        boolean requireAuth,
        @Setting("initialize-schema")
        boolean initializeSchema
) {

}
