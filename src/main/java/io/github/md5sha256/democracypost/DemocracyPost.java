package io.github.md5sha256.democracypost;

import io.github.md5sha256.democracypost.command.PostCommand;
import io.github.md5sha256.democracypost.database.DatabaseAdapter;
import io.github.md5sha256.democracypost.heads.HeadDatabaseListener;
import io.github.md5sha256.democracypost.localization.MessageContainer;
import io.github.md5sha256.democracypost.model.PostalPackageFactory;
import io.github.md5sha256.democracypost.model.SimplePostalPackageFactory;
import io.github.md5sha256.democracypost.serializer.Serializers;
import io.github.md5sha256.democracypost.ui.PostOfficeMenu;
import io.github.md5sha256.democracypost.ui.UiItemFactory;
import io.papermc.paper.util.Tick;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.sql.SQLException;

public final class DemocracyPost extends JavaPlugin {

    private DatabaseAdapter databaseAdapter;
    private PostalPackageFactory postalPackageFactory;
    private PostOfficeMenu postOfficeMenu;
    private MessageContainer messageContainer;
    private Settings settings;
    private UiItemFactory itemFactory;

    @Override
    public void onLoad() {
        try {
            initDataFolder();
            this.messageContainer = loadMessages();
            this.settings = loadSettings();
            this.postalPackageFactory = initPostalPackageFactory();
            this.databaseAdapter = initDatabase();
            this.databaseAdapter.init();
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
            getLogger().severe("Failed to initialize!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
        if (!isEnabled()) {
            return;
        }
        this.itemFactory = new UiItemFactory(this.settings.uiSettings());
        this.postOfficeMenu = new PostOfficeMenu(
                this,
                this.databaseAdapter,
                this.postalPackageFactory,
                this.messageContainer,
                this.itemFactory
        );
        if (getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
            // Listen for the database load event to re-cache the heads in the item factory
            getServer().getPluginManager().registerEvents(new HeadDatabaseListener(this.itemFactory), this);
        }
        // Plugin startup logic
        int saveDurationTicks = Tick.tick().fromDuration(this.settings.savePeriodDuration());
        getServer().getScheduler().runTaskTimer(
                this,
                () -> {
                    getLogger().info("Transferring expired packages...");
                    try {
                        this.databaseAdapter.transferExpiredPackages();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    getLogger().info("Changes saved!");
                },
                0,
                saveDurationTicks
        );
        new PostCommand(this, this.postOfficeMenu);
    }

    private PostalPackageFactory initPostalPackageFactory() {
        PostSettings postSettings = this.settings.postSettings();
        return new SimplePostalPackageFactory(
                this,
                this.databaseAdapter,
                postSettings.packageExpiryDuration(),
                postSettings.returnPackageExpiryDuration()
        );
    }

    private ConfigurationNode copyDefaultsYaml(@Nonnull String resourceName) throws IOException {
        String fileName = resourceName + ".yml";
        YamlConfigurationLoader defaultLoader = yamlLoader()
                .source(() -> {
                    Reader reader = getTextResource(fileName);
                    if (reader == null) {
                        throw new IllegalStateException("Could not find text resource: " + fileName);
                    }
                    return new BufferedReader(reader);
                })
                .build();
        YamlConfigurationLoader existingLoader = yamlLoader()
                .file(new File(getDataFolder(), fileName))
                .build();
        ConfigurationNode defaults = defaultLoader.load();
        ConfigurationNode existing = existingLoader.load();
        existing.mergeFrom(defaults);
        existingLoader.save(existing);
        return existing;
    }

    private YamlConfigurationLoader.Builder yamlLoader() {
        return YamlConfigurationLoader.builder()
                .defaultOptions(options -> options.serializers(Serializers.defaults()))
                .nodeStyle(NodeStyle.BLOCK);
    }

    @Nonnull
    private MessageContainer loadMessages() throws IOException {
        MessageContainer container = new MessageContainer();
        ConfigurationNode existing = copyDefaultsYaml("en");
        container.load(existing);
        return container;
    }

    private Settings loadSettings() throws IOException {
        ConfigurationNode settingsRoot = copyDefaultsYaml("settings");
        return settingsRoot.get(Settings.class);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (this.databaseAdapter != null) {
            this.databaseAdapter.close();
        }
    }

    @Nonnull
    private DatabaseAdapter initDatabase() throws IOException {
        return new DatabaseAdapter(this.settings.databaseSettings());
    }

    private void initDataFolder() throws IOException {
        File dataFolder = getDataFolder();
        if (!dataFolder.isDirectory()) {
            Files.createDirectory(dataFolder.toPath());
        }
    }
}
