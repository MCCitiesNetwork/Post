package io.github.md5sha256.democracypost;

import io.github.md5sha256.democracypost.database.UserDataStore;
import io.papermc.paper.util.Tick;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.time.Duration;

public class PlayerListener implements Listener {

    private final Plugin plugin;
    private final PostOfficeMenu menu;


    public PlayerListener(@Nonnull JavaPlugin plugin, @Nonnull UserDataStore dataStore) {
        this.plugin = plugin;
        this.menu = new PostOfficeMenu(plugin, dataStore);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        this.plugin.getLogger().info("login!");
        int delay = Tick.tick().fromDuration(Duration.ofSeconds(5));
        this.plugin.getServer().getScheduler().runTaskLater(
                this.plugin,
                () -> {
                    Player player = event.getPlayer();
                    this.menu.createPostUi(player.getUniqueId()).show(player);
                },
                delay);
    }

}
