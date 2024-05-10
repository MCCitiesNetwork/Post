package io.github.md5sha256.democracypost;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class PostPrompt extends StringPrompt {

    private final List<ItemStack> contents;
    private final PostalPackageFactory postalPackageFactory;
    private final Server server;

    public PostPrompt(
            @Nonnull List<ItemStack> items,
            @Nonnull PostalPackageFactory postalPackageFactory,
            @Nonnull Server server
    ) {
        this.contents = List.copyOf(items);
        this.postalPackageFactory = postalPackageFactory;
        this.server = server;
    }

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {
        return "Who should this package be mailed to? (or \"cancel\")";
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
        if (input == null) {
            return this;
        }
        Conversable sender = context.getForWhom();
        if (!(context.getForWhom() instanceof HumanEntity player)) {
            return END_OF_CONVERSATION;
        }
        OfflinePlayer offlinePlayer = this.server.getOfflinePlayerIfCached(input);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            sender.sendRawMessage("Unknown player: " + input);
            return this;
        }
        UUID recipient = offlinePlayer.getUniqueId();
        if (player.getUniqueId().equals(recipient)) {
            sender.sendRawMessage("You can't mail a parcel to yourself!");
            return this;
        }
        this.postalPackageFactory.createAndPostPackage(player.getUniqueId(), recipient, this.contents);
        sender.sendRawMessage("Parcel mailed!");
        return END_OF_CONVERSATION;
    }
}
