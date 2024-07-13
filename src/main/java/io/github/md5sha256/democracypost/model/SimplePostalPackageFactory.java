package io.github.md5sha256.democracypost.model;

import io.github.md5sha256.democracypost.EssentialsMailService;
import io.github.md5sha256.democracypost.database.DatabaseAdapter;
import io.github.md5sha256.democracypost.database.UserDataStore;
import io.github.md5sha256.democracypost.localization.MessageContainer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class SimplePostalPackageFactory implements PostalPackageFactory {

    private final Plugin plugin;
    private final DatabaseAdapter adapter;
    private final Duration expiryDuration;
    private final Duration returnPackageExpiryDuration;
    private final EssentialsMailService mailService;

    public SimplePostalPackageFactory(
            @Nonnull Plugin plugin,
            @Nonnull EssentialsMailService mailService,
            @Nonnull DatabaseAdapter adapter,
            @Nonnull Duration expiryDuration,
            @Nonnull Duration returnPackageExpiryDuration
    ) {
        this.plugin = plugin;
        this.adapter = adapter;
        this.expiryDuration = expiryDuration;
        this.returnPackageExpiryDuration = returnPackageExpiryDuration;
        this.mailService = mailService;
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
        Logger logger = this.plugin.getLogger();
        BukkitScheduler scheduler = this.plugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(this.plugin, () -> {
            try {
                PostalPackage postalPackage = createPackage(sender, recipient, contents, isReturnPackage);
                logger.fine("Posting package: " + postalPackage.id() + " sender: " + sender + " receiver: " + recipient);
                this.adapter.addPackage(postalPackage);
                scheduler.runTask(this.plugin, () -> {
                    OfflinePlayer senderPlayer = this.plugin.getServer().getOfflinePlayer(recipient);
                    this.mailService.notifyNewParcel(recipient, senderPlayer.getName());
                });
            } catch (SQLException ex) {
                logger.warning("Failed to post package!");
                ex.printStackTrace();
            }
        });
    }
}
