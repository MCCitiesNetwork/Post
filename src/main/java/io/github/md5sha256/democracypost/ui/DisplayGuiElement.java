package io.github.md5sha256.democracypost.ui;

import de.themoep.inventorygui.GuiElement;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public class DisplayGuiElement extends GuiElement {

    private final ItemStack itemStack;

    public DisplayGuiElement(char c, ItemStack itemStack) {
        super(c);
        this.itemStack = itemStack;
    }

    @Override
    public ItemStack getItem(HumanEntity who, int slot) {
        return this.itemStack == null ? null : this.itemStack.clone();
    }

}
