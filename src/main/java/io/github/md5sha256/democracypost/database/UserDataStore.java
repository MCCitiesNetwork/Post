package io.github.md5sha256.democracypost.database;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserDataStore {

    void init() throws IOException;

    void shutdown();

    @Nonnull
    Collection<UserState> users();

    @Nonnull
    UserState getOrCreateUserState(@Nonnull UUID player);

    @Nonnull
    Optional<UserState> userState(@Nonnull UUID player);

    void deleteUser(@Nonnull UUID player);

    void flushUser(@Nonnull UUID player);

    CompletableFuture<Void> flushUserAsync(@Nonnull UUID player);

    void flushAllUsers();

    CompletableFuture<Void> flushAllUsersAsync();

    void transferExpiredPackages();

}
