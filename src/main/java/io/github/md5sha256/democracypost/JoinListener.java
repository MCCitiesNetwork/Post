package io.github.md5sha256.democracypost;

import io.github.md5sha256.democracypost.database.DatabaseAdapter;
import io.github.md5sha256.democracypost.database.PackageExpiryData;
import io.github.md5sha256.democracypost.localization.MessageContainer;
import io.github.md5sha256.democracypost.util.DateUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.time.Duration;

public class JoinListener implements Listener {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;
    private final DatabaseAdapter databaseAdapter;
    private final Settings settings;
    private final PostSettings postSettings;
    private final MessageContainer messageContainer;

    public JoinListener(
            @Nonnull Plugin plugin,
            @Nonnull DatabaseAdapter databaseAdapter,
            @Nonnull Settings settings,
            @Nonnull MessageContainer messageContainer
            ) {
        this.plugin = plugin;
        this.scheduler = this.plugin.getServer().getScheduler();
        this.databaseAdapter = databaseAdapter;
        this.settings = settings;
        this.postSettings = settings.postSettings();
        this.messageContainer = messageContainer;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        Duration fromExpiry = Duration.ofSeconds(this.postSettings.expiryNotificationExpiryThresholdSeconds());
        this.scheduler.runTaskAsynchronously(this.plugin, () -> {
            PackageExpiryData expiryData;
            try {
                expiryData = this.databaseAdapter.getPackageExpiryData(player.getUniqueId(), fromExpiry);
            } catch (SQLException ex) {
                this.plugin.getLogger().severe("Failed to fetch near-expired packages for player: " + player.getName());
                ex.printStackTrace();
                return;
            }
            long delay = this.settings.joinMessageDelayTicks();
            this.scheduler.runTaskLater(this.plugin, () -> notifyExpiredPackages(player, expiryData), delay);
            notifyExpiredPackages(player, expiryData);
        });
    }

    private void notifyExpiredPackages(@Nonnull Player player, @Nonnull PackageExpiryData expiryData) {
        if (!player.isOnline() || expiryData.numPackagesAboutToExpire() == 0) {
            return;
        }
        String numPackages = String.valueOf(expiryData.numPackagesAboutToExpire());
        String formattedDate = DateUtil.formatDate(expiryData.nearestExpiryDate());
        Component message = this.messageContainer.messageFor("messages.parcel-expiry-notification")
                .replaceText(builder -> builder.matchLiteral("%parcels%").replacement(numPackages))
                .replaceText(builder -> builder.matchLiteral("%expiry%").replacement(formattedDate));
        player.sendMessage(message);
    }

}
