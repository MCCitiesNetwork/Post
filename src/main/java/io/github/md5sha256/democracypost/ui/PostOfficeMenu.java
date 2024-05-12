package io.github.md5sha256.democracypost.ui;

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
import io.github.md5sha256.democracypost.model.PackageContent;
import io.github.md5sha256.democracypost.model.PostalPackage;
import io.github.md5sha256.democracypost.model.PostalPackageFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public class PostOfficeMenu {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MM yyyy");

    private final JavaPlugin plugin;
    private final ConversationFactory conversationFactory;
    private final UserDataStore dataStore;
    private final PostalPackageFactory postalPackageFactory;

    public PostOfficeMenu(
            @Nonnull JavaPlugin plugin,
            @Nonnull UserDataStore dataStore,
            @Nonnull PostalPackageFactory postalPackageFactory
    ) {
        this.plugin = plugin;
        this.conversationFactory = new ConversationFactory(plugin);
        this.dataStore = dataStore;
        this.postalPackageFactory = postalPackageFactory;
    }

    private static boolean handleInventoryClose(InventoryGui.Close close, Inventory storageInv) {
        HumanEntity player = close.getPlayer();
        ItemStack[] contents = storageInv.getStorageContents();
        InventoryUtil.addItems(player, Arrays.asList(contents));
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


    @Nonnull
    public InventoryGui createParcelListUi(UUID user) {
        String[] rows = new String[]{
                "         ",
                " ddddddd ",
                " ddddddd ",
                " ddddddd ",
                " b p n e ",
        };
        return new InventoryGui(
                this.plugin,
                "Parcels",
                rows,
                elementPanes(' '),
                elementBack('b'),
                elementPrevious('p'),
                elementNext('n'),
                elementExit('e'),
                elementPackages('d', user)
        );
    }

    private GuiElement elementCollectPackage(char c, UserState userState, PostalPackage postalPackage) {
        return new DynamicGuiElement(c, humanEntity -> {
            int size = postalPackage.content().items().size();
            ItemStack collectButton = new ItemStack(Material.EMERALD);
            ItemMeta meta = collectButton.getItemMeta();
            Component displayName = Component.text("Collect Package", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false);
            meta.displayName(displayName);
            if (humanEntity.getInventory().getSize() < size) {
                Component warning = Component.text("Warning: insufficient inventory space!", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false);
                meta.lore(List.of(warning));
            }
            collectButton.setItemMeta(meta);
            GuiElement button = new DisplayGuiElement(c, collectButton);
            button.setAction(action -> {
                InventoryUtil.addItems(action.getWhoClicked(), postalPackage.content().items());
                userState.removePackage(postalPackage.id());
                return true;
            });
            return button;
        });
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
        meta.displayName(Component.text("Post a package", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        Component priceIndicator = Component.text("Price: $0.50", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);
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
        meta.displayName(Component.text("Open Postbox", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        int numPackages = userState.packages().size();
        Component numPackagesIndicator;
        if (numPackages > 0) {
            numPackagesIndicator = Component.text("Num packages: " + userState.packages().size(),
                            NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false);
        } else {
            numPackagesIndicator = Component.text("No unopened packages!", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false);
        }
        meta.lore(List.of(numPackagesIndicator));
        itemStack.setItemMeta(meta);
        GuiElement element = new DisplayGuiElement(c, itemStack);
        element.setAction(action -> {
            createParcelListUi(userState.getUuid()).show(action.getWhoClicked());
            return true;
        });
        return element;
    }

    private GuiElement elementPackages(char c, UUID user) {
        return new DynamicGuiElement(c, human -> {
            GuiElementGroup group = new GuiElementGroup('a');
            int i = 1;
            UserState userState = this.dataStore.getOrCreateUserState(user);
            for (PostalPackage postalPackage : userState.packages()) {
                group.addElement(elementPackageIcon('a', i, userState, postalPackage));
                i += 1;
            }
            return group;
        });
    }

    private GuiElement elementPackageIcon(char c, int index, UserState userState, PostalPackage postalPackage) {
        String date = DATE_FORMAT.format(postalPackage.expiryDate());
        PackageContent content = postalPackage.content();
        Server server = this.plugin.getServer();
        OfflinePlayer sender = server.getOfflinePlayer(content.sender());
        String senderName = sender.getName() == null ? sender.getUniqueId().toString() : sender.getName();
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = itemStack.getItemMeta();
        Component displayName = Component.text("Package Number: " + index, NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false);
        meta.displayName(displayName);
        Component displaySender = Component.text("From: " + senderName, NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false);
        Component displayNumItems = Component.text(content.items().size() + " items")
                .decoration(TextDecoration.ITALIC, false);
        Component displayExpiryDate = Component.text("Expires: " + date, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false);
        meta.lore(List.of(displaySender, displayNumItems, displayExpiryDate));
        itemStack.setItemMeta(meta);
        GuiElement element = new DisplayGuiElement(c, itemStack);
        element.setAction(action -> {
            HumanEntity who = action.getWhoClicked();
            if (action.getType().isShiftClick()) {
                InventoryUtil.addItems(who, postalPackage.content().items());
                userState.removePackage(postalPackage.id());
                action.getGui().removeElement(element);
                action.getGui().draw();
            } else {
                createParcelCollectionUi(userState, postalPackage).show(action.getWhoClicked());
            }
            return true;
        });
        return element;
    }

    private GuiElement elementNext(char c) {
        final ItemStack itemStack = new ItemStack(Material.PAPER);
        final Component displayName = Component.text("Next Page", NamedTextColor.DARK_AQUA)
                .decoration(TextDecoration.ITALIC, false);
        return new GuiPageElement(c,
                itemStack,
                GuiPageElement.PageAction.NEXT,
                LegacyComponentSerializer.legacySection().serialize(displayName));
    }

    private GuiElement elementPrevious(char c) {
        final ItemStack itemStack = new ItemStack(Material.PAPER);
        final Component displayName = Component.text("Previous Page", NamedTextColor.DARK_AQUA)
                .decoration(TextDecoration.ITALIC, false);
        return new GuiPageElement(c,
                itemStack,
                GuiPageElement.PageAction.PREVIOUS,
                LegacyComponentSerializer.legacySection().serialize(displayName));
    }

    private GuiElement elementPost(char c, Inventory storageInv) {
        ItemStack itemStack = new ItemStack(Material.DIAMOND);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.text("Post Package", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        itemStack.setItemMeta(meta);
        StaticGuiElement element = new StaticGuiElement(c, itemStack);
        element.setAction(action -> {
            processPost(action, element, itemStack, storageInv);
            return true;
        });
        return element;
    }

    private void processPost(
            GuiElement.Click action,
            StaticGuiElement element,
            ItemStack display,
            Inventory storageInv) {
        // Don't clear the storage inv, items will be added back
        if (!(action.getWhoClicked() instanceof Conversable conversable)) {
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : storageInv.getStorageContents()) {
            if (item != null) {
                items.add(item);
            }
        }
        if (items.isEmpty()) {
            ItemMeta updated = display.getItemMeta();
            updated.lore(List.of(Component.text("Cannot send empty parcels!", NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false)));
            display.setItemMeta(updated);
            element.setItem(display);
            action.getGui().draw();
            return;
        }
        // Clear the storage inv here
        storageInv.clear();
        // Copy the current history
        Deque<InventoryGui> history = new ArrayDeque<>(InventoryGui.getHistory(action.getWhoClicked()));
        // Remove the current UI so it isn't added back
        history.pollLast();
        action.getGui().close(action.getWhoClicked(), true);
        while (!history.isEmpty()) {
            InventoryGui.addHistory(action.getWhoClicked(), history.pollFirst());
        }
        Prompt prompt = new PostPrompt(items, this.postalPackageFactory, this.plugin.getServer());
        Conversation conversation = this.conversationFactory.withFirstPrompt(prompt)
                .withEscapeSequence("cancel")
                .withTimeout(60)
                .buildConversation(conversable);
        conversation.addConversationAbandonedListener(event -> {
            if (!event.gracefulExit()) {
                // If the conversation is abandoned, give the items back to the player
                InventoryUtil.addItems(action.getWhoClicked(), items);
            }
            Conversable who = event.getContext().getForWhom();
            if (who instanceof HumanEntity humanEntity) {
                InventoryGui previous = InventoryGui.getHistory(humanEntity).pollLast();
                if (previous != null) {
                    previous.show(humanEntity, false);
                }
            }
        });
        // Start conversation to send
        conversation.begin();
    }

    private GuiStorageElement elementDrop(char c, Inventory inventory) {
        return new GuiStorageElement(c, inventory);
    }

    @Nonnull
    private GuiElement elementPanes(char c) {
        final ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        final ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.empty());
        itemStack.setItemMeta(meta);
        return new StaticGuiElement(c, itemStack);
    }

    private GuiElement elementBack(char c) {
        final ItemStack itemStack = new ItemStack(Material.GLOWSTONE);
        final Component displayName = Component.text("Back", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false);
        return new GuiBackElement(c, itemStack, LegacyComponentSerializer.legacySection().serialize(displayName));
    }

    @Nonnull
    private GuiElement elementExit(char c) {
        final ItemStack itemStack = new ItemStack(Material.BARRIER);
        final Component displayName = Component.text("Exit", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false);
        return new StaticGuiElement(c,
                itemStack,
                click -> {
                    click.getGui().close(true);
                    return false;
                }, LegacyComponentSerializer.legacySection().serialize(displayName));
    }

}
