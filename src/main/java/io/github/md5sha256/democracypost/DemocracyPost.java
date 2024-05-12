package io.github.md5sha256.democracypost;

import io.github.md5sha256.democracypost.command.PostCommand;
import io.github.md5sha256.democracypost.database.FlatFileUserDataStore;
import io.github.md5sha256.democracypost.database.UserDataStore;
import io.papermc.paper.util.Tick;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class DemocracyPost extends JavaPlugin {

    private UserDataStore dataStore;
    private PostalPackageFactory postalPackageFactory;
    private PostOfficeMenu postOfficeMenu;

    @Override
    public void onLoad() {
        try {
            initDataFolder();
            this.dataStore = initDataStore();
            this.dataStore.init();
        } catch (IOException ex) {
            ex.printStackTrace();
            getLogger().severe("Failed to initialize the data store!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
        this.postalPackageFactory = new SimplePostalPackageFactory(this.dataStore, Duration.ofMinutes(3));
        this.postOfficeMenu = new PostOfficeMenu(this, this.dataStore, this.postalPackageFactory);
        // Plugin startup logic
        int saveDurationTicks = Tick.tick().fromDuration(
                Duration.of(10, TimeUnit.MINUTES.toChronoUnit()));
        getServer().getScheduler().runTaskTimer(
                this,
                () -> {
                    getLogger().info("Transferring expired packages...");
                    this.dataStore.transferExpiredPackages();
                    this.dataStore.flushAllUsersAsync();
                    getLogger().info("Changes saved!");
                },
                saveDurationTicks,
                saveDurationTicks
        );
        new PostCommand(this, this.postOfficeMenu);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (this.dataStore != null) {
            // Flush all cached data synchronously
            this.dataStore.flushAllUsers();
        }
    }

    @Nonnull
    private UserDataStore initDataStore() throws IOException {
        File dataStoreFolder = new File(getDataFolder(), "user-data");
        if (!dataStoreFolder.isDirectory()) {
            Files.createDirectory(dataStoreFolder.toPath());
        }
        return new FlatFileUserDataStore(this,
                new SimplePostalPackageFactory(this.dataStore, Duration.ofSeconds(30)),
                dataStoreFolder.toPath());
    }

    private void initDataFolder() throws IOException {
        File dataFolder = getDataFolder();
        if (!dataFolder.isDirectory()) {
            Files.createDirectory(dataFolder.toPath());
        }
    }
}
