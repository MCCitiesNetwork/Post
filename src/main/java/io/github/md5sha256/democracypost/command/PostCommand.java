package io.github.md5sha256.democracypost.command;

import de.themoep.inventorygui.InventoryGui;
import io.github.md5sha256.democracypost.ui.PostOfficeMenu;
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
import org.incendo.cloud.paper.PaperCommandManager;

import javax.annotation.Nonnull;

public class PostCommand {

    private final PostOfficeMenu postOfficeMenu;

    public PostCommand(@Nonnull Plugin plugin, @Nonnull PostOfficeMenu postOfficeMenu) {
        this.postOfficeMenu = postOfficeMenu;
        var commandManager = new PaperCommandManager<>(
                plugin,
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.identity()
        );
        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier();
        }
        var annotationParser = new AnnotationParser<>(commandManager, CommandSender.class);
        annotationParser.parse(this);
    }

    @Command("post open <player>")
    @Permission("democracypost.open")
    public void commandOpen(
            @Nonnull @Argument(value = "player") Player player
    ) {
        this.postOfficeMenu.createPostUi(player.getUniqueId()).show(player);
    }

    @Command("post view <player>")
    @Permission("democracypost.view")
    public void commandView(
            @Nonnull Player sender,
            @Nonnull @Argument(value = "player") OfflinePlayer who) {
        // Prevent the sender from going "back"
        InventoryGui.clearHistory(sender);
        this.postOfficeMenu.createParcelListUi(who.getUniqueId()).show(sender);
    }


}
