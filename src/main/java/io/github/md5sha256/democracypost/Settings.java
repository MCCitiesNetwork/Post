package io.github.md5sha256.democracypost;

import io.github.md5sha256.democracypost.ui.UiSettings;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import javax.annotation.Nonnull;
import java.time.Duration;

@ConfigSerializable
public record Settings(
        @Setting @Required @Nonnull UiSettings uiSettings,
        @Setting @Required @Nonnull PostSettings postSettings,
        @Setting @Required @Nonnull DatabaseSettings databaseSettings,
        @Setting @Required long saveDurationSeconds) {

    @Nonnull
    public Duration savePeriodDuration() {
        return Duration.ofSeconds(this.saveDurationSeconds);
    }

}
