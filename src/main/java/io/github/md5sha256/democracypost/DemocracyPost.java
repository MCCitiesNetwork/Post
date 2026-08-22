package io.github.md5sha256.democracypost;

import io.github.md5sha256.democracypost.command.PostCommand;
import io.github.md5sha256.democracypost.database.DatabaseAdapter;
import io.github.md5sha256.democracypost.heads.HeadDatabaseListener;
import io.github.md5sha256.democracypost.localization.MessageContainer;
import io.github.md5sha256.democracypost.model.PostalPackageFactory;
import io.github.md5sha256.democracypost.notification.EssentialsMailService;
import io.github.md5sha256.democracypost.notification.NoopParcelNotificationService;
import io.github.md5sha256.democracypost.notification.NotificationBackend;
import io.github.md5sha256.democracypost.notification.ParcelDataTypes;
import io.github.md5sha256.democracypost.notification.ParcelNotificationBackends;
import io.github.md5sha256.democracypost.notification.ParcelNotificationRenderer;
import io.github.md5sha256.democracypost.notification.ParcelNotificationService;
import io.github.md5sha256.democracypost.notification.PlayerNotificationsService;
import io.github.md5sha256.playernotifications.api.NotificationService;
import io.github.md5sha256.democracypost.model.SimplePostalPackageFactory;
import io.github.md5sha256.democracypost.serializer.Serializers;
import io.github.md5sha256.democracypost.ui.PostOfficeMenu;
import io.github.md5sha256.democracypost.ui.UiItemFactory;
import io.papermc.paper.util.Tick;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
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
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

public final class DemocracyPost extends JavaPlugin {

    private DatabaseAdapter databaseAdapter;
    private PostalPackageFactory postalPackageFactory;
    private PostOfficeMenu postOfficeMenu;
    private MessageContainer messageContainer;
    private ParcelNotificationService mailService;
    private NotificationService notificationService;
    private Settings settings;
    private UiItemFactory itemFactory;

    @Override
    public void onLoad() {
        try {
            initDataFolder();
            this.messageContainer = loadMessages();
            this.settings = loadSettings();
            this.databaseAdapter = initDatabase();
        } catch (IOException ex) {
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
        if (this.databaseAdapter == null) {
            getLogger().severe("Database was not initialized (onLoad failed). Plugin will not enable.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Missing Vault!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Optional<Economy> economy = getEconomy();
        if (economy.isEmpty()) {
            getLogger().severe("Missing economy provider!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.mailService = initNotificationService();
        this.postalPackageFactory = initPostalPackageFactory();
        this.itemFactory = new UiItemFactory(this.settings.uiSettings());
        this.postOfficeMenu = new PostOfficeMenu(
                this,
                this.databaseAdapter,
                this.postalPackageFactory,
                this.messageContainer,
                this.itemFactory,
                this.settings.postSettings(),
                economy.get()
        );
        if (getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
            // Listen for the database load event to re-cache the heads in the item factory
            getServer().getPluginManager().registerEvents(new HeadDatabaseListener(this.itemFactory), this);
        }
        // Plugin startup logic
        int saveDurationTicks = Tick.tick().fromDuration(this.settings.savePeriodDuration());
        Duration returnPackageExpiryDuration = this.settings.postSettings().returnPackageExpiryDuration();
        getServer().getScheduler().runTaskTimerAsynchronously(
                this,
                () -> {
                    if (this.databaseAdapter == null) {
                        return;
                    }
                    getLogger().fine("Transferring expired packages...");
                    try {
                        this.databaseAdapter.transferExpiredPackages(returnPackageExpiryDuration);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    getLogger().fine("Changes saved!");
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
                this.mailService,
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
        if (this.notificationService != null) {
            // Leaving the renderer registered would strand it on this class loader across a reload.
            ParcelDataTypes.unregisterAll(this.notificationService);
            this.notificationService = null;
        }
        if (this.databaseAdapter != null) {
            this.databaseAdapter.close();
        }
    }

    /**
     * Builds the parcel notification backend the config asks for, gated on what is installed.
     *
     * <p>PlayerNotifications API classes are only touched inside the branch that has already
     * confirmed the service is registered: they are {@code compileOnly}, so naming one on a server
     * without the plugin would fail to link.
     */
    @Nonnull
    private ParcelNotificationService initNotificationService() {
        NotificationBackend configured = this.settings.postSettings().notificationBackendOrDefault();
        RegisteredServiceProvider<NotificationService> provider =
                getServer().getServicesManager().getRegistration(NotificationService.class);
        boolean essentialsAvailable = getServer().getPluginManager().isPluginEnabled("Essentials");
        ParcelNotificationBackends.Choice choice =
                ParcelNotificationBackends.choose(configured, provider != null, essentialsAvailable);
        switch (choice) {
            case PLAYER_NOTIFICATIONS -> {
                NotificationService service = provider.getProvider();
                ParcelDataTypes.registerAll(service, new ParcelNotificationRenderer(this.messageContainer));
                this.notificationService = service;
                getLogger().info("Delivering parcel notices through PlayerNotifications.");
                return new PlayerNotificationsService(service::enqueueNotification);
            }
            case ESSENTIALS -> {
                getLogger().info("Delivering parcel notices as Essentials mail.");
                return new EssentialsMailService(this.messageContainer);
            }
            default -> {
                getLogger().severe("notification-backend is '" + configured.name().toLowerCase(Locale.ROOT)
                        + "' but that plugin is not available; parcel notices are disabled.");
                return new NoopParcelNotificationService(getLogger());
            }
        }
    }

    @Nonnull
    private DatabaseAdapter initDatabase() throws IOException {
        DatabaseAdapter adapter = new DatabaseAdapter(this.settings.databaseSettings(), getLogger(), this.settings.postSettings().skipUndeserializableItems());
        try {
            adapter.init();
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
        return adapter;
    }

    private void initDataFolder() throws IOException {
        File dataFolder = getDataFolder();
        if (!dataFolder.isDirectory()) {
            Files.createDirectory(dataFolder.toPath());
        }
    }

    private Optional<Economy> getEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        return Optional.ofNullable(rsp).map(RegisteredServiceProvider::getProvider);
    }
}
