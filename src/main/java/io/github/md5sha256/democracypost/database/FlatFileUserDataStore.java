package io.github.md5sha256.democracypost.database;

import io.github.md5sha256.democracypost.model.PostalPackage;
import io.github.md5sha256.democracypost.model.PostalPackageFactory;
import io.github.md5sha256.democracypost.serializer.Serializers;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class FlatFileUserDataStore implements UserDataStore {

    private final Path root;
    private final BukkitScheduler scheduler;
    private final Plugin plugin;
    private final Server server;
    private final PostalPackageFactory postalPackageFactory;
    private final Map<UUID, UserState> cache = new ConcurrentHashMap<>();


    public FlatFileUserDataStore(
            @Nonnull Plugin plugin,
            @Nonnull PostalPackageFactory postalPackageFactory,
            @Nonnull Path root
    ) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
        this.server = plugin.getServer();
        this.postalPackageFactory = postalPackageFactory;
        this.root = root;
    }

    @Override
    public void init() throws IOException {
        try (Stream<Path> stream = Files.walk(this.root, 1)) {
            stream.filter(path -> path.getFileName().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            loadUser(path.toFile());
                        } catch (IOException ex) {
                            this.plugin.getLogger().warning("Failed to load data from file: " + path.getFileName());
                        }
                    });
        }
    }

    @Override
    public void shutdown() {
        flushAllUsers();
    }

    @NotNull
    @Override
    public Collection<UserState> users() {
        return Collections.unmodifiableCollection(this.cache.values());
    }

    @NotNull
    @Override
    public UserState getOrCreateUserState(@NotNull UUID player) {
        return this.cache.computeIfAbsent(player, UserState::new);
    }

    @Nonnull
    private File fileFor(@Nonnull UUID uuid) {
        return this.root.resolve(uuid + ".json").toFile();
    }

    @Nullable
    private UserState loadUser(@Nonnull UUID uuid) throws IOException {
        return loadUser(fileFor(uuid));
    }

    private GsonConfigurationLoader.Builder loader() {
        return GsonConfigurationLoader.builder()
                .defaultOptions(options -> options.serializers(Serializers.defaults()));
    }

    @Nullable
    private UserState loadUser(@Nonnull File file) throws IOException {
        if (!file.isFile()) {
            return null;
        }
        var loader = loader().file(file).build();
        ConfigurationNode node = loader.load();
        return node.<UserState>get(UserState.class, () -> null);
    }

    private void saveUser(@Nonnull UUID player, @Nonnull UserState userState) throws IOException {
        File file = fileFor(player);
        var loader = loader().file(file).build();
        ConfigurationNode node = loader.createNode();
        node.set(userState);
        loader.save(node);
    }

    @Nonnull
    private CompletableFuture<Void> saveUserAsync(@Nonnull UUID player, @Nonnull UserState userState) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.scheduler.runTask(this.plugin, () -> {
            try {
                saveUser(player, userState);
            } catch (IOException ex) {
                ex.printStackTrace();
                this.plugin.getLogger().warning("Failed to save user data for player: " + player);
            }
            future.complete(null);
        });
        return future;
    }

    @NotNull
    @Override
    public Optional<UserState> userState(@NotNull UUID player) {
        return Optional.ofNullable(this.cache.get(player));
    }

    @Override
    public void flushUser(@NotNull UUID player) {
        UserState userState = this.cache.get(player);
        if (userState == null) {
            return;
        }
        try {
            saveUser(player, userState);
        } catch (IOException ex) {
            ex.printStackTrace();
            this.plugin.getLogger().warning("Failed to save user data for player: " + player);
        }
        if (this.server.getPlayer(player) == null) {
            this.cache.remove(player);
        }
    }

    @Override
    public CompletableFuture<Void> flushUserAsync(@NotNull UUID player) {
        UserState userState = this.cache.get(player);
        if (userState == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (this.server.getPlayer(player) == null) {
            this.cache.remove(player);
        }
        UserState copy = userState.deepCopy();
        return saveUserAsync(player, copy);
    }

    @Override
    public CompletableFuture<Void> flushAllUsersAsync() {
        Map<UUID, UserState> copy = new HashMap<>(this.cache);
        for (Map.Entry<UUID, UserState> entry : this.cache.entrySet()) {
            entry.setValue(entry.getValue().deepCopy());
        }
        for (UUID player : copy.keySet()) {
            if (this.server.getPlayer(player) == null) {
                this.cache.remove(player);
            }
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.scheduler.runTask(this.plugin, () -> {
            for (Map.Entry<UUID, UserState> entry : copy.entrySet()) {
                try {
                    saveUser(entry.getKey(), entry.getValue());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    this.plugin.getLogger().warning("Failed to save user data for player: " + entry.getKey());
                }
            }
            future.complete(null);
        });
        return future;
    }

    @Override
    public void flushAllUsers() {
        for (Map.Entry<UUID, UserState> entry : this.cache.entrySet()) {
            try {
                saveUser(entry.getKey(), entry.getValue());
            } catch (IOException ex) {
                ex.printStackTrace();
                this.plugin.getLogger().warning("Failed to save user data for player: " + entry.getKey());
            }
        }
        for (UUID player : this.cache.keySet()) {
            if (this.server.getPlayer(player) == null) {
                this.cache.remove(player);
            }
        }
    }

    @Override
    public void deleteUser(@NotNull UUID player) {
        this.cache.remove(player);
        try {
            Files.delete(fileFor(player).toPath());
        } catch (IOException ex) {
            ex.printStackTrace();
            this.plugin.getLogger().warning(() -> "Failed to delete user data for player: " + player);
        }
    }

    @Override
    public void transferExpiredPackages() {
        for (Map.Entry<UUID, UserState> entry : this.cache.entrySet()) {
            UUID uuid = entry.getKey();
            UserState userState = entry.getValue();
            Collection<PostalPackage> expiredPackages = userState.removeExpiredPackages();
            for (PostalPackage expiredPackage : expiredPackages) {
                if (expiredPackage.content().sender().equals(uuid)) {
                    // Drop parcel if this is already a return package
                    continue;
                }
                this.postalPackageFactory.createAndPostPackage(uuid, uuid, expiredPackage.content().items(), true);
            }
        }
    }
}
