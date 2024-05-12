package io.github.md5sha256.democracypost.command;

import io.github.md5sha256.democracypost.PostOfficeMenu;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
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
import java.util.UUID;

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
            @Nonnull CommandSender sender,
            @Nonnull @Argument(value = "player") Player player
    ) {
        openPostUi(player, player.getUniqueId());
    }

    @Command("post view <player>")
    @Permission("democracypost.view")
    public void commandView(
            @Nonnull Player sender,
            @Nonnull @Argument(value = "player") OfflinePlayer who) {
        openPostUi(sender, who.getUniqueId());
    }

    private void openPostUi(@Nonnull HumanEntity viewer, UUID who) {
        this.postOfficeMenu.createPostUi(who).show(viewer);
    }

}
