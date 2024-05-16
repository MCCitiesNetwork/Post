package io.github.md5sha256.democracypost.model;

import io.github.md5sha256.democracypost.database.UserDataStore;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SimplePostalPackageFactory implements PostalPackageFactory {

    private final UserDataStore dataStore;
    private Duration expiryDuration;
    private Duration returnPackageExpiryDuration;

    public SimplePostalPackageFactory(
            @Nonnull UserDataStore dataStore,
            @Nonnull Duration expiryDuration,
            @Nonnull Duration returnPackageExpiryDuration
    ) {
        this.dataStore = dataStore;
        this.expiryDuration = expiryDuration;
        this.returnPackageExpiryDuration = returnPackageExpiryDuration;
    }

    @NotNull
    @Override
    public PostalPackage createPackage(@NotNull UUID sender,
                                       @NotNull UUID recipient,
                                       @NotNull List<ItemStack> contents,
                                       boolean isReturnPackage) {
        PackageContent content = new PackageContent(sender, recipient, contents);
        Duration duration = isReturnPackage ? this.returnPackageExpiryDuration : this.expiryDuration;
        Date expiry = Date.from(Instant.now().plus(duration));
        return new PostalPackage(expiry, content, isReturnPackage);
    }

    @Override
    public void createAndPostPackage(@NotNull UUID sender,
                                     @NotNull UUID recipient,
                                     @NotNull List<ItemStack> contents,
                                     boolean isReturnPackage) {
        this.dataStore.getOrCreateUserState(sender)
                .addPackage(createPackage(sender, recipient, contents, isReturnPackage));
    }
}
