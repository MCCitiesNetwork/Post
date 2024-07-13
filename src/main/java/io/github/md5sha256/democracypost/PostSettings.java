package io.github.md5sha256.democracypost;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import javax.annotation.Nonnull;
import java.time.Duration;

@ConfigSerializable
public record PostSettings(
        @Setting @Required long packageExpirySeconds,
        @Setting @Required long returnPackageExpirySeconds,
        @Setting @Required long expiryNotificationExpiryThresholdSeconds,
        @Setting @Required double postPrice
) {

    @Nonnull
    public Duration packageExpiryDuration() {
        return Duration.ofSeconds(this.packageExpirySeconds);
    }

    @Nonnull
    public Duration returnPackageExpiryDuration() {
        return Duration.ofSeconds(this.returnPackageExpirySeconds);
    }

}
