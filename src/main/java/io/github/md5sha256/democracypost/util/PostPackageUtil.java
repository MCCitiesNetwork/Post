package io.github.md5sha256.democracypost.util;

import io.github.md5sha256.democracypost.database.DatabaseAdapter;
import io.github.md5sha256.democracypost.model.PostalPackage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class PostPackageUtil {

    private static final Component DELETION_ERROR_MESSAGE
            = Component.text("Could not open this package, please contact an administrator!", NamedTextColor.RED);

    private PostPackageUtil() {
        throw new IllegalStateException("Cannot instantiate static utility class");
    }

    public static CompletableFuture<Boolean> claimPackage(
            HumanEntity who,
            PostalPackage postalPackage,
            DatabaseAdapter adapter,
            Plugin plugin
    ) {
        if (postalPackage.expired()) {
            who.sendMessage(Component.text("Cannot open expired package!", NamedTextColor.RED));
            return CompletableFuture.completedFuture(false);
        }
        if (!postalPackage.unclaimed()) {
            who.sendMessage(Component.text("Package already claimed!", NamedTextColor.RED));
            return CompletableFuture.completedFuture(false);
        }
        postalPackage.setClaimed(true);
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                adapter.removePackage(postalPackage.id());
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to delete package!");
                who.sendMessage(DELETION_ERROR_MESSAGE);
                ex.printStackTrace();
                postalPackage.setClaimed(false);
                future.complete(false);
                return;
            }
            scheduler.runTask(plugin, () -> {
                InventoryUtil.addItems(who, postalPackage.content().items());
                future.complete(true);
            });
        });
        return future;
    }

}
