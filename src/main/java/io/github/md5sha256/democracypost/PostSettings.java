package io.github.md5sha256.democracypost;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;

@ConfigSerializable
public record PostSettings(
        @Setting @Required long packageExpirySeconds,
        @Setting @Required long returnPackageExpirySeconds,
        @Setting @Required long expiryNotificationExpiryThresholdSeconds,
        @Setting @Required double postPrice,
        @Setting @Nullable String priceFormatPattern
) {

    /**
     * Pattern used when none is configured. Any literal prefix/suffix in the pattern is used
     * verbatim, so the currency symbol can be replaced by editing the pattern.
     */
    public static final String DEFAULT_PRICE_FORMAT_PATTERN = "$#,##0.00";

    /**
     * Formats the given amount using the configured {@link #priceFormatPattern()}.
     */
    @Nonnull
    public String formatPrice(double amount) {
        String pattern = this.priceFormatPattern == null || this.priceFormatPattern.isEmpty()
                ? DEFAULT_PRICE_FORMAT_PATTERN
                : this.priceFormatPattern;
        DecimalFormat format = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.ROOT));
        return format.format(amount);
    }

    @Nonnull
    public Duration packageExpiryDuration() {
        return Duration.ofSeconds(this.packageExpirySeconds);
    }

    @Nonnull
    public Duration returnPackageExpiryDuration() {
        return Duration.ofSeconds(this.returnPackageExpirySeconds);
    }

}
