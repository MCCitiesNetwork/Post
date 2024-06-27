package io.github.md5sha256.democracypost.ui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import io.github.md5sha256.democracypost.PostSettings;
import io.github.md5sha256.democracypost.database.DatabaseAdapter;
import io.github.md5sha256.democracypost.localization.MessageContainer;
import io.github.md5sha256.democracypost.model.PackageContent;
import io.github.md5sha256.democracypost.model.PostalPackage;
import io.github.md5sha256.democracypost.model.PostalPackageFactory;
import io.github.md5sha256.democracypost.util.InventoryUtil;
import io.github.md5sha256.democracypost.util.PostPackageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PostOfficeMenu {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MM yyyy");
    private static final NumberFormat PRICE_FORMAT = new DecimalFormat("###.##");

    private final JavaPlugin plugin;
    private final ConversationFactory conversationFactory;
    private final DatabaseAdapter databaseAdapter;
    private final PostalPackageFactory postalPackageFactory;
    private final MessageContainer messageContainer;
    private final InventoryGui.InventoryCreator inventoryCreator;
    private final UiItemFactory itemFactory;
    private final PostSettings postSettings;
    private final Economy economy;

    public PostOfficeMenu(
            @Nonnull JavaPlugin plugin,
            @Nonnull DatabaseAdapter databaseAdapter,
            @Nonnull PostalPackageFactory postalPackageFactory,
            @Nonnull MessageContainer messageContainer,
            @Nonnull UiItemFactory itemFactory,
            @Nonnull PostSettings postSettings, 
            @Nonnull Economy economy
    ) {
        this.plugin = plugin;
        this.conversationFactory = new ConversationFactory(plugin);
        this.databaseAdapter = databaseAdapter;
        this.postalPackageFactory = postalPackageFactory;
        this.messageContainer = messageContainer;
        this.inventoryCreator = PaperInventoryCreator.creator(plugin.getServer());
        this.itemFactory = itemFactory;
        this.postSettings = postSettings;
        this.economy = economy;
    }

    private static boolean handleInventoryClose(InventoryGui.Close close, Inventory storageInv) {
        HumanEntity player = close.getPlayer();
        ItemStack[] contents = storageInv.getStorageContents();
        InventoryUtil.addItems(player, Arrays.asList(contents));
        storageInv.clear();
        return true;
    }

    private InventoryGui createGui(@Nonnull Component title, @Nonnull String[] rows, GuiElement... elements) {
        String serializedTitle = MiniMessage.miniMessage().serialize(title);
        return new InventoryGui(this.plugin, this.inventoryCreator, null, serializedTitle, rows, elements);
    }

    public CompletableFuture<InventoryGui> createPostUi(@Nonnull UUID user) {
        CompletableFuture<InventoryGui> future = new CompletableFuture<>();
        OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(user);
        if (!player.hasPlayedBefore()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("User has not played before: " + user));
        }
        BukkitScheduler scheduler = this.plugin.getServer().getScheduler();
        OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(user);
        scheduler.runTaskAsynchronously(this.plugin, () -> {
            List<PostalPackage> packages;
            try {
                packages = this.databaseAdapter.getPackagesForRecipient(user);
            } catch (SQLException ex) {
                future.completeExceptionally(ex);
                return;
            }
            scheduler.runTask(this.plugin, () -> future.complete(createPostUi(player.getName(), packages)));
        });
        return future;
    }

    public InventoryGui createPostUi(String playerName, List<PostalPackage> packages) {
        String[] rows = new String[]{
                "         ",
                " ####### ",
                " #p###v# ",
                " ###e### ",
                "         "
        };
        // p = post, v = view packages
        return createGui(this.messageContainer.messageFor("menu.main.title"),
                rows,
                elementPanes(' '),
                new DisplayGuiElement('#', null),
                elementSendPackage('p'),
                elementExit('e'),
                elementPackagesIcon('v', playerName, packages));
    }

    public InventoryGui createParcelPostUi() {
        String[] rows = new String[]{
                "         ",
                " ddddddd ",
                " ddddddd ",
                " ddddddd ",
                "   b p   ",
        };
        // d = space to drop
        // blank space = panes
        // b = back, p = post parcel, e = exit
        Inventory storageInv = this.plugin.getServer().createInventory(null, 9 * 3);
        InventoryGui gui = createGui(
                this.messageContainer.messageFor("menu.parcel.drop"),
                rows,
                elementPanes(' '),
                elementDrop('d', storageInv),
                elementBack('b'),
                elementPost('p', storageInv)
        );
        gui.setCloseAction(close -> handleInventoryClose(close, storageInv));
        return gui;
    }

    private InventoryGui createParcelCollectionUi(PostalPackage postalPackage) {
        String[] rows = new String[]{
                "         ",
                " ddddddd ",
                " ddddddd ",
                " ddddddd ",
                "   b c   ",
        };
        // d = space to drop
        // blank space = panes
        // b = back, c = collect items, e = exit
        return createGui(
                this.messageContainer.messageFor("menu.parcel.collection-title"),
                rows,
                elementPanes(' '),
                elementPackageContents('d', postalPackage),
                elementBack('b'),
                elementCollectPackage('c', postalPackage)
        );
    }

    public CompletableFuture<InventoryGui> createParcelListUi(@Nonnull UUID user) {
        OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(user);
        CompletableFuture<InventoryGui> future = new CompletableFuture<>();
        BukkitScheduler scheduler = this.plugin.getServer().getScheduler();
        if (!offlinePlayer.hasPlayedBefore()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("User has never played before: " + user));
        }
        scheduler.runTaskAsynchronously(this.plugin, () -> {
            List<PostalPackage> packages;
            try {
                packages = this.databaseAdapter.getPackagesForRecipient(user);
            } catch (SQLException ex) {
                future.completeExceptionally(ex);
                return;
            }
            scheduler.runTask(this.plugin, () -> future.complete(createParcelListUi(offlinePlayer.getName(), packages)));
        });
        return future;
    }


    @Nonnull
    public InventoryGui createParcelListUi(String playerName, List<PostalPackage> packages) {
        String[] rows = new String[]{
                "         ",
                " ddddddd ",
                " ddddddd ",
                " ddddddd ",
                "  p b n  ",
        };
        Component title = this.messageContainer.messageFor("menu.parcel.list")
                .replaceText(builder -> builder.matchLiteral("%player%").replacement(playerName));
        return createGui(
                title,
                rows,
                elementPanes(' '),
                elementBack('b'),
                elementPrevious('p'),
                elementNext('n'),
                elementPackages('d', packages)
        );
    }

    private GuiElement elementCollectPackage(char c, PostalPackage postalPackage) {
        return new DynamicGuiElement(c, humanEntity -> {
            int size = postalPackage.content().items().size();
            ItemStack collectButton = this.itemFactory.createCollectPackageIcon();
            ItemMeta meta = collectButton.getItemMeta();
            Component displayName = this.messageContainer.messageFor("menu.parcel.collect-parcel")
                    .decoration(TextDecoration.ITALIC, false);
            meta.displayName(displayName);
            if (humanEntity.getInventory().getSize() < size) {
                Component warning = this.messageContainer.messageFor("messages.insufficient-inventory-space")
                                .decoration(TextDecoration.ITALIC, false);
                meta.lore(List.of(warning));
            }
            collectButton.setItemMeta(meta);
            GuiElement button = new DisplayGuiElement(c, collectButton);
            button.setAction(action -> {
                PostPackageUtil.openPackage(
                        action.getWhoClicked(),
                        postalPackage,
                        this.databaseAdapter,
                        this.plugin
                );
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

    private ItemStack createSendPackageIcon() {
        ItemStack itemStack = this.itemFactory.createSendPackageButton();
        ItemMeta meta = itemStack.getItemMeta();
        Component displayName = this.messageContainer.messageFor("menu.parcel.post-parcel")
                .decoration(TextDecoration.ITALIC, false);
        meta.displayName(displayName);
        double price = this.postSettings.postPrice();
        String formattedPrice = PRICE_FORMAT.format(price);
        Component priceIndicator = this.messageContainer.messageFor("menu.parcel.post-parcel-price")
                .replaceText(builder -> builder.matchLiteral("%price%").replacement(formattedPrice))
                .decoration(TextDecoration.ITALIC, false);
        meta.lore(List.of(priceIndicator));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private GuiElement elementSendPackage(char c) {
        ItemStack itemStack = createSendPackageIcon();
        double price = this.postSettings.postPrice();
        DisplayGuiElement element = new DisplayGuiElement(c, itemStack);
        element.setAction(action -> {
            HumanEntity who = action.getWhoClicked();
            if (!(who instanceof Player player)) {
                return true;
            }
            if (this.economy.getBalance(player) < price) {
                player.sendMessage(this.messageContainer.messageFor("menu.parcel.post-insufficient-balance"));
                action.getGui().close(player);
                return true;
            }
            createParcelPostUi().show(action.getWhoClicked());
            return true;
        });
        return element;
    }

    private GuiElement elementPackagesIcon(char c, String playerName, List<PostalPackage> packages) {
        ItemStack itemStack = this.itemFactory.createPackagesIcon();
        ItemMeta meta = itemStack.getItemMeta();
        Component displayName = this.messageContainer.messageFor("menu.main.open-mailbox")
                        .decoration(TextDecoration.ITALIC, false);
        meta.displayName(displayName);
        int numPackages = packages.size();
        Component numPackagesIndicator;
        if (numPackages > 0) {
            numPackagesIndicator = this.messageContainer.messageFor("menu.main.unopened-packages")
                    .replaceText(builder -> builder.matchLiteral("%packages%").replacement(String.valueOf(numPackages)))
                    .decoration(TextDecoration.ITALIC, false);
        } else {
            numPackagesIndicator = this.messageContainer.messageFor("menu.main.no-unopened-packages")
                    .decoration(TextDecoration.ITALIC, false);
        }
        meta.lore(List.of(numPackagesIndicator));
        itemStack.setItemMeta(meta);
        GuiElement element = new DisplayGuiElement(c, itemStack);
        element.setAction(action -> {
            createParcelListUi(playerName, packages).show(action.getWhoClicked());
            return true;
        });
        return element;
    }

    private GuiElement elementPackages(char c, List<PostalPackage> packages) {
        return new DynamicGuiElement(c, human -> {
            GuiElementGroup group = new GuiElementGroup('a');
            int i = 1;
            for (PostalPackage postalPackage : packages) {
                group.addElement(elementPackageIcon('a', i, postalPackage));
                i += 1;
            }
            return group;
        });
    }

    private ItemStack createPackageIcon(int index, PostalPackage postalPackage) {
        String date = DATE_FORMAT.format(postalPackage.expiryDate());
        PackageContent content = postalPackage.content();
        Server server = this.plugin.getServer();
        OfflinePlayer sender = server.getOfflinePlayer(content.sender());
        String senderName = sender.getName() == null ? sender.getUniqueId().toString() : sender.getName();
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = itemStack.getItemMeta();
        int numItems = postalPackage.content().items().size();
        Component displayName = this.messageContainer.messageFor("menu.parcel.package-number")
                .replaceText(builder -> builder.matchLiteral("%number%").replacement(String.valueOf(index)))
                .decoration(TextDecoration.ITALIC, false);
        meta.displayName(displayName);
        Component displaySender = this.messageContainer.messageFor("menu.parcel.package-sender")
                .replaceText(builder -> builder.matchLiteral("%player%").replacement(senderName))
                .decoration(TextDecoration.ITALIC, false);
        Component displayNumItems = this.messageContainer.messageFor("menu.parcel.package-num-items")
                .replaceText(builder -> builder.matchLiteral("%items%").replacement(String.valueOf(numItems)))
                .decoration(TextDecoration.ITALIC, false);
        Component displayExpiryDate = this.messageContainer.messageFor("menu.parcel.package-expiry-date")
                .replaceText(builder -> builder.matchLiteral("%date%").replacement(date))
                .decoration(TextDecoration.ITALIC, false);
        meta.lore(List.of(displaySender, displayNumItems, displayExpiryDate));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private GuiElement elementPackageIcon(char c, int index, PostalPackage postalPackage) {
        ItemStack itemStack = createPackageIcon(index, postalPackage);
        GuiElement element = new DisplayGuiElement(c, itemStack);
        element.setAction(action -> {
            HumanEntity who = action.getWhoClicked();
            if (action.getType().isShiftClick()) {
                PostPackageUtil.openPackage(who, postalPackage, this.databaseAdapter, this.plugin);
                action.getGui().removeElement(element);
                action.getGui().draw();
            } else {
                createParcelCollectionUi(postalPackage).show(action.getWhoClicked());
            }
            return true;
        });
        return element;
    }

    private GuiElement elementNext(char c) {
        final ItemStack itemStack = this.itemFactory.createNextButton();
        final Component displayName = this.messageContainer.messageFor("menu.next-page")
                .decoration(TextDecoration.ITALIC, false);
        return new GuiPageElement(c,
                itemStack,
                GuiPageElement.PageAction.NEXT,
                LegacyComponentSerializer.legacySection().serialize(displayName));
    }

    private GuiElement elementPrevious(char c) {
        final ItemStack itemStack = this.itemFactory.createPreviousButton();
        final Component displayName = this.messageContainer.messageFor("menu.previous-page")
                .decoration(TextDecoration.ITALIC, false);
        return new GuiPageElement(c,
                itemStack,
                GuiPageElement.PageAction.PREVIOUS,
                LegacyComponentSerializer.legacySection().serialize(displayName));
    }

    private GuiElement elementPost(char c, Inventory storageInv) {
        ItemStack itemStack = this.itemFactory.createSendPackageButton();
        ItemMeta meta = itemStack.getItemMeta();
        Component displayName = this.messageContainer.messageFor("menu.main.post-parcel")
                        .decoration(TextDecoration.ITALIC, false);
        meta.displayName(displayName);
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
            Component emptyParcelMessage = this.messageContainer.messageFor("menu.parcel.post-empty-parcel")
                            .decoration(TextDecoration.ITALIC, false);
            updated.lore(List.of(emptyParcelMessage));
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
        Prompt prompt = new PostPrompt(
                items,
                this.postalPackageFactory,
                this.messageContainer,
                this.plugin.getServer(),
                this.economy,
                this.postSettings
        );
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
        final ItemStack itemStack = this.itemFactory.createBackButton();
        final Component displayName = this.messageContainer.messageFor("menu.back")
                .decoration(TextDecoration.ITALIC, false);
        return new GuiBackElement(c, itemStack, LegacyComponentSerializer.legacySection().serialize(displayName));
    }

    @Nonnull
    private GuiElement elementExit(char c) {
        final ItemStack itemStack = this.itemFactory.createExitButton();
        final Component displayName = this.messageContainer.messageFor("menu.exit")
                .decoration(TextDecoration.ITALIC, false);
        return new StaticGuiElement(c,
                itemStack,
                click -> {
                    click.getGui().close(true);
                    return false;
                }, LegacyComponentSerializer.legacySection().serialize(displayName));
    }

}
