package io.github.md5sha256.democracypost;

import io.github.md5sha256.democracypost.database.UserDataStore;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PostPrompt extends StringPrompt {

    private final List<ItemStack> contents;
    private final UserDataStore dataStore;
    private final Server server;

    public PostPrompt(@Nonnull List<ItemStack> items, @Nonnull UserDataStore dataStore, @Nonnull Server server) {
        this.contents = List.copyOf(items);
        this.dataStore = dataStore;
        this.server = server;
    }

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {
        return "player";
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
        if (input == null) {
            return this;
        }
        if (!(context.getForWhom() instanceof HumanEntity player)) {
            return END_OF_CONVERSATION;
        }
        OfflinePlayer offlinePlayer = this.server.getOfflinePlayerIfCached(input);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            player.sendMessage("Unknown player: " + input);
            return this;
        }
        // FIXME add a conversation factory
        UUID recipient = offlinePlayer.getUniqueId();
        UUID sender = player.getUniqueId();
        PackageContent content = new PackageContent(sender, recipient, this.contents);
        Date expiry = Date.from(Instant.now().plus(Duration.ofDays(7)));
        PostalPackage postalPackage = new PostalPackage(expiry, content);
        this.dataStore.getOrCreateUserState(sender).addPackage(postalPackage);
        return END_OF_CONVERSATION;
    }
}
