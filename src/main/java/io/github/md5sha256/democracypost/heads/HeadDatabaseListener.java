package io.github.md5sha256.democracypost.heads;

import io.github.md5sha256.democracypost.ui.UiItemFactory;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;

public class HeadDatabaseListener implements Listener {

    private final UiItemFactory itemFactory;

    public HeadDatabaseListener(@Nonnull UiItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDatabaseLoad(DatabaseLoadEvent event) {
        this.itemFactory.cacheHeads(new HeadDatabaseAPI());
    }

}
