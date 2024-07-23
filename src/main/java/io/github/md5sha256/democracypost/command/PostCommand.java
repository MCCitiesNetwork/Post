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

import javax.annotation.Nonnull;

public class PostCommand {

    private final PostOfficeMenu postOfficeMenu;

    public PostCommand(@Nonnull Plugin plugin, @Nonnull PostOfficeMenu postOfficeMenu) {
        this.postOfficeMenu = postOfficeMenu;
        var commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);
        var annotationParser = new AnnotationParser<>(commandManager, CommandSourceStack.class);
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

    @Command("post view <player>")
    @Permission("democracypost.view")
    public void commandView(
            @Nonnull CommandSourceStack senderSourceStack,
            @Nonnull @Argument(value = "player") OfflinePlayer who) {
        CommandSender sender = senderSourceStack.getSender();
        if (!(senderSourceStack.getExecutor() instanceof Player executor)) {
            sender.sendMessage("Executor must be an instance of a player!");
            return;
        }
        // Prevent the sender from going "back"
        InventoryGui.clearHistory(executor);
        this.postOfficeMenu.createParcelListUi(who.getUniqueId()).whenComplete((gui, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(Component.text("Error occurred when retrieving packages for " + who.getName(),
                        NamedTextColor.RED));
                return;
            }
            gui.show(executor);
        });
    }


}
