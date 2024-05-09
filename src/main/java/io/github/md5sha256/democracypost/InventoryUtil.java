package io.github.md5sha256.democracypost;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryUtil {

    private InventoryUtil() {}


    public static void addItems(@Nonnull HumanEntity humanEntity, Iterable<ItemStack> toAdd) {
        Inventory inventory = humanEntity.getInventory();
        Map<Integer, ItemStack> didNotFit = new HashMap<>();
        for (ItemStack itemStack : toAdd) {
            if (itemStack != null) {
                didNotFit.putAll(inventory.addItem(itemStack));
            }
        }
        Location playerLocation = humanEntity.getLocation();
        UUID playerUuid = humanEntity.getUniqueId();
        for (ItemStack itemStack : didNotFit.values()) {
            Item itemEntity = humanEntity.getWorld().dropItem(playerLocation, itemStack);
            // Ensure only the player can pick up the item
            itemEntity.setOwner(playerUuid);
        }
    }
}
