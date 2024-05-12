package io.github.md5sha256.democracypost.model;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public interface PostalPackageFactory {

    @Nonnull
    PostalPackage createPackage(@Nonnull UUID sender, @Nonnull UUID recipient, @Nonnull List<ItemStack> contents);

    void createAndPostPackage(@Nonnull UUID sender, @Nonnull UUID recipient, @Nonnull List<ItemStack> contents);

}
