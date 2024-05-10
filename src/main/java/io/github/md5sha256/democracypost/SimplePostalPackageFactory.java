package io.github.md5sha256.democracypost;

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

    public SimplePostalPackageFactory(@Nonnull UserDataStore dataStore, @Nonnull Duration expiryDuration) {
        this.dataStore = dataStore;
        this.expiryDuration = expiryDuration;
    }

    public void setExpiryDuration(@Nonnull Duration duration) {
        this.expiryDuration = duration;
    }

    @NotNull
    @Override
    public PostalPackage createPackage(@NotNull UUID sender,
                                       @NotNull UUID recipient,
                                       @NotNull List<ItemStack> contents) {
        PackageContent content = new PackageContent(sender, recipient, contents);
        Date expiry = Date.from(Instant.now().plus(this.expiryDuration));
        return new PostalPackage(expiry, content);
    }

    @Override
    public void createAndPostPackage(@NotNull UUID sender, @NotNull UUID recipient, @NotNull List<ItemStack> contents) {
        this.dataStore.getOrCreateUserState(sender).addPackage(createPackage(sender, recipient, contents));
    }
}
