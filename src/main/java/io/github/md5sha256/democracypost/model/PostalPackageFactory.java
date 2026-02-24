package io.github.md5sha256.democracypost.model;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PostalPackageFactory {

    enum PostResult {
        SUCCESS,
        TOO_LARGE,
        UNKNOWN_ERROR,
    }

    @Nonnull
    PostalPackage createPackage(@Nonnull UUID sender, @Nonnull UUID recipient, @Nonnull List<ItemStack> contents, boolean isReturn);

    @Nonnull
    CompletableFuture<PostResult> createAndPostPackage(@Nonnull UUID sender, @Nonnull UUID recipient, @Nonnull List<ItemStack> contents, boolean isReturn);

}
