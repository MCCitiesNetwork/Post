package io.github.md5sha256.democracypost.command;

import de.themoep.inventorygui.InventoryGui;
import io.github.md5sha256.democracypost.ui.PostOfficeMenu;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

import javax.annotation.Nonnull;

public class PostCommand {

    private final PostOfficeMenu postOfficeMenu;

    public PostCommand(@Nonnull Plugin plugin, @Nonnull PostOfficeMenu postOfficeMenu) {
        this.postOfficeMenu = postOfficeMenu;
        var commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);
        var annotationParser = new AnnotationParser<>(commandManager, Source.class);
        annotationParser.parse(this);
    }

    @Command("post open <player>")
    @Permission("democracypost.open")
    public void commandOpen(
            @Nonnull @Argument(value = "player") Player player
    ) {
        this.postOfficeMenu.createPostUi(player.getUniqueId())
                .whenComplete((gui, throwable) -> {
                    if (throwable != null) {
                        player.sendMessage(Component.text("Error occurred when retrieving your packages!",
                                NamedTextColor.RED));
                        return;
                    }
                    gui.show(player);
                });
    }

    @Command("post open <player> <target>")
    @Permission("democracypost.open")
    public void commandOpenWithTarget(
            @Nonnull @Argument(value = "player") Player player,
            @Nonnull @Argument(value = "target") OfflinePlayer target
    ) {
        if (!target.hasPlayedBefore()) {
            player.sendMessage(Component.text("Unknown or invalid target player.", NamedTextColor.RED));
            return;
        }
        this.postOfficeMenu.createParcelPostUi(target).show(player);
    }

    @Command("post view <player>")
    @Permission("democracypost.view")
    public void commandView(
            @Nonnull PlayerSource playerSource,
            @Nonnull @Argument(value = "player") OfflinePlayer who) {
        Player sender = playerSource.source();
        // Prevent the sender from going "back"
        InventoryGui.clearHistory(sender);
        this.postOfficeMenu.createParcelListUi(who.getUniqueId()).whenComplete((gui, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(Component.text("Error occurred when retrieving packages for " + who.getName(),
                        NamedTextColor.RED));
                return;
            }
            gui.show(sender);
        });
    }

    @Command("post view <player> <targetPlayer>")
    @Permission("democracypost.view.other")
    public void commandViewOther(
            @Nonnull @Argument(value = "player") OfflinePlayer who,
            @Nonnull @Argument(value = "targetPlayer") Player toOpen) {
        // Prevent the sender from going "back"
        InventoryGui.clearHistory(toOpen);
        this.postOfficeMenu.createParcelListUi(who.getUniqueId()).whenComplete((gui, throwable) -> {
            if (throwable != null) {
                toOpen.sendMessage(Component.text("Error occurred when retrieving packages for " + who.getName(),
                        NamedTextColor.RED));
                return;
            }
            gui.show(toOpen);
        });
    }


}
