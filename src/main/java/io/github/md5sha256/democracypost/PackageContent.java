/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 *
 * Could not load the following classes:
 *  org.bukkit.inventory.ItemStack
 */
package io.github.md5sha256.democracypost;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PackageContent(@Nonnull UUID sender,
                             @Nonnull UUID receiver,
                             @Nonnull List<ItemStack> items) {

    @Nonnull
    public PackageContent deepCopy() {
        List<ItemStack> copy = new ArrayList<>();
        this.items.forEach(item -> copy.add(item.clone()));
        return new PackageContent(this.sender, this.receiver, copy);
    }

}

