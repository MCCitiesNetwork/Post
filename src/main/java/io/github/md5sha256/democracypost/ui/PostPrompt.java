package io.github.md5sha256.democracypost.ui;

import io.github.md5sha256.democracypost.PostSettings;
import io.github.md5sha256.democracypost.localization.MessageContainer;
import io.github.md5sha256.democracypost.model.PostalPackageFactory;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
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
    private final MessageContainer messageContainer;
    private final Economy economy;
    private final PostSettings postSettings;

    public PostPrompt(
            @Nonnull List<ItemStack> items,
            @Nonnull PostalPackageFactory postalPackageFactory,
            @Nonnull MessageContainer messageContainer,
            @Nonnull Server server,
            @Nonnull Economy economy,
            @Nonnull PostSettings postSettings
    ) {
        this.contents = List.copyOf(items);
        this.postalPackageFactory = postalPackageFactory;
        this.messageContainer = messageContainer;
        this.server = server;
        this.economy = economy;
        this.postSettings = postSettings;
    }

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {
        return this.messageContainer.plaintextMessageFor("prompt.post-parcel.initial-message");
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
        if (input == null) {
            return this;
        }
        Conversable sender = context.getForWhom();
        if (!(sender instanceof Player player)) {
            return END_OF_CONVERSATION;
        }
        OfflinePlayer offlinePlayer = this.server.getOfflinePlayerIfCached(input);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            String message = this.messageContainer.plaintextMessageFor("prompt.post-parcel.unknown-player")
                            .replace("%player%", input);
            sender.sendRawMessage(message);
            return this;
        }
        UUID recipient = offlinePlayer.getUniqueId();
        if (player.getUniqueId().equals(recipient)) {
            sender.sendRawMessage(this.messageContainer.plaintextMessageFor("prompt.post-parcel.send-parcel-self"));
            return this;
        }
        EconomyResponse response = this.economy.withdrawPlayer(player, this.postSettings.postPrice());
        if (!response.transactionSuccess()) {
            sender.sendRawMessage(this.messageContainer.plaintextMessageFor("prompt.post-parcel.insufficient-balance"));
            return END_OF_CONVERSATION;
        }
        this.postalPackageFactory.createAndPostPackage(player.getUniqueId(), recipient, this.contents, false)
                .thenAccept(result -> {
                    var messageKey = switch (result) {
                        case SUCCESS -> null;
                        case TOO_LARGE -> "prompt.post-parcel.parcel-too-large";
                        case UNKNOWN_ERROR -> "prompt.post-parcel.send-parcel-failed";
                    };
                    if (messageKey == null) {
                        return;
                    }
                    player.getInventory().addItem(this.contents.toArray(ItemStack[]::new));
                    var message = this.messageContainer.plaintextMessageFor(messageKey);
                    sender.sendRawMessage(message);
                });
        sender.sendRawMessage(this.messageContainer.plaintextMessageFor("prompt.post-parcel.send-parcel-success"));
        return END_OF_CONVERSATION;
    }
}
