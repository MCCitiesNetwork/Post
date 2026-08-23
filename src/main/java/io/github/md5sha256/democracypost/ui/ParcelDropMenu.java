package io.github.md5sha256.democracypost.ui;

import io.github.md5sha256.democracypost.localization.MessageContainer;
import io.github.md5sha256.democracypost.util.InventoryUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The screen a player drops items into when posting a parcel.
 *
 * <p>Unlike the other menus this is a plain Bukkit {@link Inventory} rather than an
 * {@code InventoryGui}: the drop area is a real container, so the server moves the items and the
 * usual click/drag emulation - along with the dupe risks that come with it - is not needed. Only
 * the decorative and button slots are click-guarded.
 */
public final class ParcelDropMenu implements Listener {

    private static final String[] LAYOUT = {
            "         ",
            " ddddddd ",
            " ddddddd ",
            " ddddddd ",
            "   b p   ",
    };
    private static final char DROP = 'd';
    private static final char BACK = 'b';
    private static final char POST = 'p';

    private static final Set<Integer> DROP_SLOTS = slotsOf(DROP);
    private static final int BACK_SLOT = slotsOf(BACK).iterator().next();
    private static final int POST_SLOT = slotsOf(POST).iterator().next();
    private static final int SIZE = LAYOUT.length * 9;

    private final JavaPlugin plugin;
    private final MessageContainer messageContainer;
    private final UiItemFactory itemFactory;
    private final Handler handler;

    public ParcelDropMenu(
            @Nonnull JavaPlugin plugin,
            @Nonnull MessageContainer messageContainer,
            @Nonnull UiItemFactory itemFactory,
            @Nonnull Handler handler
    ) {
        this.plugin = plugin;
        this.messageContainer = messageContainer;
        this.itemFactory = itemFactory;
        this.handler = handler;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private static Set<Integer> slotsOf(char c) {
        Set<Integer> slots = new LinkedHashSet<>();
        for (int row = 0; row < LAYOUT.length; row++) {
            String line = LAYOUT[row];
            for (int column = 0; column < line.length(); column++) {
                if (line.charAt(column) == c) {
                    slots.add(row * 9 + column);
                }
            }
        }
        return Set.copyOf(slots);
    }

    /**
     * Opens the drop screen.
     *
     * @param player    the player dropping items in
     * @param recipient the player the parcel is addressed to, or {@code null} to prompt for one
     *                  once the parcel is posted
     * @param onBack    what to show when the player goes back, or {@code null} to hide the back
     *                  button because there is nothing to go back to
     */
    public void open(@Nonnull Player player, @Nullable UUID recipient, @Nullable Runnable onBack) {
        View view = new View(recipient, onBack);
        Component title = this.messageContainer.messageFor("menu.parcel.drop");
        Inventory inventory = this.plugin.getServer().createInventory(view, SIZE, title);
        view.inventory = inventory;
        decorate(inventory, onBack != null);
        player.openInventory(inventory);
    }

    private void decorate(Inventory inventory, boolean showBack) {
        ItemStack pane = named(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), Component.empty());
        for (int slot = 0; slot < SIZE; slot++) {
            if (!DROP_SLOTS.contains(slot) && slot != BACK_SLOT && slot != POST_SLOT) {
                inventory.setItem(slot, pane);
            }
        }
        // The back button is left as filler when there is nothing to go back to, matching how
        // CustomBackElement hides itself in the InventoryGui screens.
        inventory.setItem(BACK_SLOT, showBack
                ? named(this.itemFactory.createBackButton(), message("menu.back"))
                : pane);
        inventory.setItem(POST_SLOT, named(this.itemFactory.createSendPackageButton(), message("menu.main.post-parcel")));
    }

    private Component message(String key) {
        return this.messageContainer.messageFor(key).decoration(TextDecoration.ITALIC, false);
    }

    private static ItemStack named(ItemStack itemStack, Component displayName) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(displayName);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Nullable
    private static View viewOf(InventoryHolder holder) {
        return holder instanceof View view ? view : null;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        View view = viewOf(event.getView().getTopInventory().getHolder());
        if (view == null) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        // Gathering a stack by double clicking pulls matching items out of every slot, decorative
        // ones included, so it is never allowed here.
        if (event.getClick() == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            // A click outside of any inventory - dropping the cursor stack is harmless.
            return;
        }
        if (clicked.getHolder() == view) {
            int slot = event.getSlot();
            if (DROP_SLOTS.contains(slot)) {
                // A real container slot: let the server move the item.
                return;
            }
            event.setCancelled(true);
            if (slot == BACK_SLOT) {
                goBack(player, view);
            } else if (slot == POST_SLOT) {
                post(player, view);
            }
            return;
        }
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // Vanilla would shift items into the first free slot, decorative ones included.
            event.setCancelled(true);
            event.setCurrentItem(addToDropArea(view.inventory, event.getCurrentItem()));
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        View view = viewOf(event.getView().getTopInventory().getHolder());
        if (view == null) {
            return;
        }
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < SIZE && !DROP_SLOTS.contains(rawSlot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        View view = viewOf(event.getInventory().getHolder());
        if (view == null || view.consumed) {
            return;
        }
        returnDroppedItems(event.getPlayer(), view);
    }

    /**
     * Distributes a stack across the drop slots the way a container would, merging into partial
     * stacks of the same item before filling empty slots.
     *
     * @return whatever did not fit, or {@code null} if all of it did
     */
    @Nullable
    private static ItemStack addToDropArea(Inventory inventory, @Nullable ItemStack toAdd) {
        if (toAdd == null || toAdd.getType().isAir()) {
            return toAdd;
        }
        ItemStack remaining = toAdd.clone();
        for (int slot : DROP_SLOTS) {
            ItemStack existing = inventory.getItem(slot);
            if (existing == null || !existing.isSimilar(remaining)) {
                continue;
            }
            int space = existing.getMaxStackSize() - existing.getAmount();
            if (space <= 0) {
                continue;
            }
            int moved = Math.min(space, remaining.getAmount());
            existing.setAmount(existing.getAmount() + moved);
            inventory.setItem(slot, existing);
            remaining.setAmount(remaining.getAmount() - moved);
            if (remaining.getAmount() == 0) {
                return null;
            }
        }
        for (int slot : DROP_SLOTS) {
            ItemStack existing = inventory.getItem(slot);
            if (existing != null && !existing.getType().isAir()) {
                continue;
            }
            int moved = Math.min(remaining.getMaxStackSize(), remaining.getAmount());
            ItemStack placed = remaining.clone();
            placed.setAmount(moved);
            inventory.setItem(slot, placed);
            remaining.setAmount(remaining.getAmount() - moved);
            if (remaining.getAmount() == 0) {
                return null;
            }
        }
        return remaining;
    }

    @Nonnull
    private static List<ItemStack> droppedItems(View view) {
        List<ItemStack> items = new ArrayList<>();
        for (int slot : DROP_SLOTS) {
            ItemStack item = view.inventory.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                items.add(item);
            }
        }
        return items;
    }

    private static void clearDropArea(View view) {
        for (int slot : DROP_SLOTS) {
            view.inventory.setItem(slot, null);
        }
    }

    private static void returnDroppedItems(HumanEntity player, View view) {
        List<ItemStack> items = droppedItems(view);
        clearDropArea(view);
        InventoryUtil.addItems(player, items);
    }

    private void goBack(Player player, View view) {
        // The close handler hands the items back before the previous screen opens.
        player.closeInventory();
        if (view.onBack != null) {
            view.onBack.run();
        }
    }

    private void post(Player player, View view) {
        List<ItemStack> items = droppedItems(view);
        if (items.isEmpty()) {
            ItemStack button = view.inventory.getItem(POST_SLOT);
            if (button != null) {
                ItemMeta meta = button.getItemMeta();
                meta.lore(List.of(message("menu.parcel.post-empty-parcel")));
                button.setItemMeta(meta);
                view.inventory.setItem(POST_SLOT, button);
            }
            return;
        }
        clearDropArea(view);
        // The items are the handler's responsibility from here, so closing must not hand them back.
        view.consumed = true;
        player.closeInventory();
        this.handler.postParcel(player, items, view.recipient);
    }

    /**
     * Receives the parcel once the player confirms it.
     */
    public interface Handler {

        /**
         * Posts the dropped items. The drop screen is already closed and has given up ownership of
         * the items, so the handler must deliver them or hand them back to the player.
         *
         * @param player    the sender
         * @param items     the dropped items
         * @param recipient the addressee, or {@code null} to ask the player for one
         */
        void postParcel(@Nonnull Player player, @Nonnull List<ItemStack> items, @Nullable UUID recipient);
    }

    /**
     * Per-open state, doubling as the inventory's holder so events can be routed back to it.
     */
    private static final class View implements InventoryHolder {

        private final UUID recipient;
        private final Runnable onBack;

        private Inventory inventory;
        private boolean consumed;

        private View(@Nullable UUID recipient, @Nullable Runnable onBack) {
            this.recipient = recipient;
            this.onBack = onBack;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return this.inventory;
        }
    }
}
