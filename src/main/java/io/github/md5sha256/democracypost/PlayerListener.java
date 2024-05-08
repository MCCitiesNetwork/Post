package io.github.md5sha256.democracypost;

import io.github.md5sha256.democracypost.database.UserDataStore;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import javax.annotation.Nonnull;

public class PlayerListener implements Listener {

    private final Server server;
    private final UserDataStore dataStore;

    public PlayerListener(@Nonnull Server server, @Nonnull UserDataStore dataStore) {
        this.server = server;
        this.dataStore = dataStore;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        this.dataStore.cacheUserState(event.getPlayer().getUniqueId());
    }

}
