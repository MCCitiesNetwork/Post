package io.github.md5sha256.democracypost.ui;

import de.themoep.inventorygui.InventoryGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;

public class PaperInventoryCreator {

    private PaperInventoryCreator() {
        throw new IllegalStateException("Cannot instantiate utility class");
    }

    public static InventoryGui.InventoryCreator creator(Server server) {
        var sizeCreator = new SizeCreatorImpl(server);
        var typeCreator = new TypeCreatorImpl(server);
        return new InventoryGui.InventoryCreator(typeCreator, sizeCreator);
    }

    private static class SizeCreatorImpl implements InventoryGui.InventoryCreator.CreatorImplementation<Integer> {

        private final Server server;

        public SizeCreatorImpl(@Nonnull Server server) {
            this.server = server;
        }

        @Override
        public Inventory create(InventoryGui gui, HumanEntity who, Integer size) {
            String title = gui.getTitle();
            String replacedTitle = gui.replaceVars(who, title);
            Component component;
            if (replacedTitle.indexOf(ChatColor.COLOR_CHAR) != -1) {
                component = LegacyComponentSerializer.legacySection().deserialize(replacedTitle);
            } else {
                component = MiniMessage.miniMessage().deserialize(replacedTitle);
            }
            return this.server.createInventory(new InventoryGui.Holder(gui), size, component);
        }
    }

    private static class TypeCreatorImpl implements InventoryGui.InventoryCreator.CreatorImplementation<InventoryType> {

        private final Server server;

        public TypeCreatorImpl(@Nonnull Server server) {
            this.server = server;
        }

        @Override
        public Inventory create(InventoryGui gui, HumanEntity who, InventoryType type) {
            String title = gui.replaceVars(who, gui.getTitle());
            Component component = MiniMessage.miniMessage().deserialize(title);
            return this.server.createInventory(new InventoryGui.Holder(gui), type, component);
        }
    }
}
