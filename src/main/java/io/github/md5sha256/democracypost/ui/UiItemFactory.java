package io.github.md5sha256.democracypost.ui;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UiItemFactory {

    private final UiSettings settings;

    private ItemStack showPackagesButton;
    private ItemStack packagesIcon;
    private ItemStack collectPackageIcon;
    private ItemStack sendPackageIcon;

    public UiItemFactory(@Nonnull UiSettings settings) {
        this.settings = settings;
        cacheHeads(null);
    }

    public void cacheHeads(@Nullable HeadDatabaseAPI api) {
        this.showPackagesButton = getOrDefault(api, this.settings.showPackagesHeadId(), Material.PLAYER_HEAD);
        this.packagesIcon = getOrDefault(api, this.settings.packagesIconHeadId(), Material.PLAYER_HEAD);
        this.collectPackageIcon = getOrDefault(api, this.settings.collectPackageHeadId(), Material.CHEST);
        this.sendPackageIcon = getOrDefault(api, this.settings.sendPackageHeadId(), Material.CHEST);
    }

    private ItemStack getOrDefault(@Nullable HeadDatabaseAPI api, @Nonnull String id, Material def) {
        if (api == null) {
            return new ItemStack(def);
        }
        ItemStack ret = api.getItemHead(id);
        return ret == null ? new ItemStack(def) : ret;
    }

    public ItemStack createExitButton() {
        return new ItemStack(this.settings.exitButtonMaterial());
    }

    public ItemStack createBackButton() {
        return new ItemStack(this.settings.backButtonMaterial());
    }

    public ItemStack createNextButton() {
        return new ItemStack(this.settings.nextButtonMaterial());
    }

    public ItemStack createPreviousButton() {
        return new ItemStack(this.settings.previousButtonMaterial());
    }

    public ItemStack createShowPackagesButton() {
        return this.showPackagesButton.clone();
    }

    public ItemStack createPackagesIcon() {
        return this.packagesIcon.clone();
    }

    public ItemStack createCollectPackageIcon() {
        return this.collectPackageIcon.clone();
    }

    public ItemStack createSendPackageButton() {
        return this.sendPackageIcon.clone();
    }


}
