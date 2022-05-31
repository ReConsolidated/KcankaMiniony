package io.github.reconsolidated.kcankaminiony.CustomInventory;

import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class InventoryItem {
    @Getter
    private ItemStack item;
    @Getter
    private int row;
    @Getter
    private int column;

    public InventoryItem(ItemStack item, int row, int column) {
        this.item = item;
        this.row = row;
        this.column = column;
    }

    public abstract void onClick(InventoryClickEvent event);
}
