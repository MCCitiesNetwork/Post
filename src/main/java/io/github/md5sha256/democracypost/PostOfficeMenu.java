package io.github.md5sha256.democracypost;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import io.github.md5sha256.democracypost.database.UserDataStore;
import io.github.md5sha256.democracypost.database.UserState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PostOfficeMenu {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MM yyyy");

    private final JavaPlugin plugin;
    private final ConversationFactory conversationFactory;
    private final UserDataStore dataStore;

    public PostOfficeMenu(@Nonnull JavaPlugin plugin, @Nonnull UserDataStore dataStore) {
        this.plugin = plugin;
        this.conversationFactory = new ConversationFactory(plugin);
        this.dataStore = dataStore;
    }

    private static boolean handleInventoryClose(InventoryGui.Close close, Inventory storageInv) {
        HumanEntity player = close.getPlayer();
        ItemStack[] contents = storageInv.getStorageContents();
        InventoryUtil.addItems(player, List.of(contents));
        storageInv.clear();
        return true;
    }

    public InventoryGui createPostUi(@Nonnull UUID user) {
        UserState userState = this.dataStore.getOrCreateUserState(user);
        String[] rows = new String[]{
                "         ",
                " ####### ",
                " #p###v# ",
                " ####### ",
                "         "
        };
        // p = post, v = view packages
        return new InventoryGui(this.plugin,
                "POST >> Main Menu",
                rows,
                elementPanes(' '),
                new DisplayGuiElement('#', null),
                elementPostIcon('p'),
                elementPackagesIcon('v', userState));
    }

    public InventoryGui createParcelPostUi() {
        String[] rows = new String[]{
                "         ",
                " ddddddd ",
                " ddddddd ",
                " ddddddd ",
                "  b p e  ",
        };
        // d = space to drop
        // blank space = panes
        // b = back, p = post parcel, e = exit
        Inventory storageInv = this.plugin.getServer().createInventory(null, 9 * 3);
        InventoryGui gui = new InventoryGui(
                this.plugin,
                "Parcel Drop",
                rows,
                elementPanes(' '),
                elementDrop('d', storageInv),
                elementBack('b'),
                elementExit('e'),
                elementPost('p', storageInv)
        );
        gui.setCloseAction(close -> handleInventoryClose(close, storageInv));
        return gui;
    }

    private InventoryGui createParcelCollectionUi(UserState userState, PostalPackage postalPackage) {
        String[] rows = new String[]{
                "         ",
                " ddddddd ",
                " ddddddd ",
                " ddddddd ",
                "  b c e  ",
        };
        // d = space to drop
        // blank space = panes
        // b = back, c = collect items, e = exit
        return new InventoryGui(
                this.plugin,
                "Parcel Drop",
                rows,
                elementPanes(' '),
                elementPackageContents('d', postalPackage),
                elementBack('b'),
                elementExit('e'),
                elementCollectPackage('c', userState, postalPackage)
        );
    }


    public InventoryGui createParcelListUi(Collection<PostalPackage> packages) {
        String[] rows = new String[]{
                "         ",
                " ddddddd ",
                " ddddddd ",
                " ddddddd ",
                "  b e n  ",
        };
        return new InventoryGui(
                this.plugin,
                "Parcels",
                rows,
                elementPanes(' '),
                elementNext('n'),
                elementPrevious('b'),
                elementExit('e'),
                elementPackages('d', packages)
        );
    }

    private GuiElement elementCollectPackage(char c, UserState userState, PostalPackage postalPackage) {
        DynamicGuiElement element = new DynamicGuiElement(c, humanEntity -> {
            int size = postalPackage.content().items().size();
            ItemStack collectButton = new ItemStack(Material.EMERALD);
            ItemMeta meta = collectButton.getItemMeta();
            meta.displayName(Component.text("Collect Package", NamedTextColor.GREEN));
            if (humanEntity.getInventory().getSize() < size) {
                Component warning = Component.text("Warning: insufficient inventory space!", NamedTextColor.YELLOW)
                        .style(Style.empty());
                meta.lore(List.of(warning));
            }
            collectButton.setItemMeta(meta);
            return new DisplayGuiElement(c, collectButton);
        });
        element.setAction(action -> {
            if (action.getType().isShiftClick()) {
                InventoryUtil.addItems(action.getWhoClicked(), postalPackage.content().items());
                userState.removePackage(postalPackage.id());
            } else {
                createParcelCollectionUi(userState, postalPackage).show(action.getWhoClicked());
            }
            return true;
        });
        return element;
    }


    private GuiElement elementPackageContents(char c, PostalPackage postalPackage) {
        GuiElementGroup group = new GuiElementGroup(c);
        for (ItemStack itemStack : postalPackage.content().items()) {
            group.addElement(elementPackageContent('a', itemStack));
        }
        return group;
    }

    private GuiElement elementPackageContent(char c, ItemStack item) {
        return new DisplayGuiElement(c, item);
    }

    private GuiElement elementPostIcon(char c) {
        ItemStack itemStack = new ItemStack(Material.CHEST);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.text("Post a package", NamedTextColor.RED));
        Component priceIndicator = Component.text("Price: $0.50", NamedTextColor.GRAY);
        meta.lore(List.of(priceIndicator));
        itemStack.setItemMeta(meta);
        GuiElement element = new DisplayGuiElement(c, itemStack);
        element.setAction(action -> {
            createParcelPostUi().show(action.getWhoClicked());
            return true;
        });
        return element;
    }

    private GuiElement elementPackagesIcon(char c, UserState userState) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.text("Open Postbox", NamedTextColor.RED));
        Component numPackages = Component.text(userState.packages().size(), NamedTextColor.WHITE).style(Style.empty());
        meta.lore(List.of(numPackages));
        GuiElement element = new DisplayGuiElement(c, itemStack);
        element.setAction(action -> {
            action.getGui().close(false);
            createParcelListUi(userState.packages()).draw(action.getWhoClicked());
            return true;
        });
        return element;
    }

    private GuiElement elementPackages(char c, Collection<PostalPackage> packages) {
        GuiElementGroup group = new GuiElementGroup(c);
        int i = 1;
        for (PostalPackage postalPackage : packages) {
            group.addElement(elementPackageIcon('a', i, postalPackage));
            i += 1;
        }
        return group;
    }

    private GuiElement elementPackageIcon(char c, int index, PostalPackage postalPackage) {
        String date = DATE_FORMAT.format(postalPackage.expiryDate());
        PackageContent content = postalPackage.content();
        Server server = this.plugin.getServer();
        OfflinePlayer sender = server.getOfflinePlayer(content.sender());
        String senderName = sender.getName() == null ? sender.getUniqueId().toString() : sender.getName();
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = itemStack.getItemMeta();
        Component displayName = Component.text("Package Number: " + index, NamedTextColor.GOLD)
                .style(Style.empty());
        meta.displayName(displayName);
        Component displaySender = Component.text("From: " + senderName, NamedTextColor.WHITE).style(Style.empty());
        Component displayNumItems = Component.text(content.items().size() + " items").style(Style.empty());
        Component displayExpiryDate = Component.text("Expires: " + date, NamedTextColor.YELLOW).style(Style.empty());
        meta.lore(List.of(displaySender, displayNumItems, displayExpiryDate));
        itemStack.setItemMeta(meta);
        return new DisplayGuiElement(c, itemStack);
    }

    private GuiElement elementNext(char c) {
        final ItemStack itemStack = new ItemStack(Material.PAPER);
        final Component displayName = Component.text("Next Page", NamedTextColor.DARK_AQUA);
        return new GuiPageElement(c,
                itemStack,
                GuiPageElement.PageAction.NEXT,
                LegacyComponentSerializer.legacySection().serialize(displayName));
    }

    private GuiElement elementPrevious(char c) {
        final ItemStack itemStack = new ItemStack(Material.PAPER);
        final Component displayName = Component.text("Previous Page", NamedTextColor.DARK_AQUA);
        return new GuiPageElement(c,
                itemStack,
                GuiPageElement.PageAction.PREVIOUS,
                LegacyComponentSerializer.legacySection().serialize(displayName));
    }

    private GuiElement elementPost(char c, Inventory storageInv) {
        ItemStack itemStack = new ItemStack(Material.DIAMOND);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.text("Post Package", NamedTextColor.AQUA));
        itemStack.setItemMeta(meta);
        StaticGuiElement element = new StaticGuiElement(c, itemStack);
        element.setAction(action -> {
            // Don't clear the storage inv, items will be added back
            if (!(action.getWhoClicked() instanceof Conversable conversable)) {
                return false;
            }
            List<ItemStack> items = new ArrayList<>();
            for (ItemStack item : storageInv.getStorageContents()) {
                if (item != null) {
                    items.add(item);
                }
            }
            // Clear the storage inv here
            storageInv.clear();
            Prompt prompt = new PostPrompt(items, this.dataStore, this.plugin.getServer());
            Conversation conversation = this.conversationFactory.withFirstPrompt(prompt)
                    .withEscapeSequence("cancel")
                    .withTimeout(60)
                    .buildConversation(conversable);
            conversation.addConversationAbandonedListener(unused -> {
                // If the conversation is abandoned, give the items back to the player
                InventoryUtil.addItems(action.getWhoClicked(), items);
            });
            // Start conversation to send
            conversation.begin();
            return true;
        });
        return element;
    }

    private GuiStorageElement elementDrop(char c, Inventory inventory) {
        return new GuiStorageElement(c, inventory);
    }

    @Nonnull
    private GuiElement elementPanes(char c) {
        final ItemStack itemStack = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        final ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.empty());
        itemStack.setItemMeta(meta);
        return new StaticGuiElement(c, itemStack);
    }

    private GuiElement elementBack(char c) {
        final ItemStack itemStack = new ItemStack(Material.BARRIER);
        final Component displayName = Component.text("Back", NamedTextColor.RED);
        return new GuiBackElement(c, itemStack, LegacyComponentSerializer.legacySection().serialize(displayName));
    }

    @Nonnull
    private GuiElement elementExit(char c) {
        final ItemStack itemStack = new ItemStack(Material.BARRIER);
        final Component displayName = Component.text("Exit", NamedTextColor.RED);
        return new StaticGuiElement(c,
                itemStack,
                click -> {

                    click.getGui().close();
                    return false;
                }, LegacyComponentSerializer.legacySection().serialize(displayName));
    }

}
