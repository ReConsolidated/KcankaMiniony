package io.github.reconsolidated.kcankaminiony.CustomInventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ClickOnlyItem extends InventoryItem{
    private Consumer<InventoryClickEvent> onClick;

    public ClickOnlyItem(ItemStack item, int row, int column, Consumer<InventoryClickEvent> onClick) {
        super(item, row, column);
        this.onClick = onClick;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        onClick.accept(event);
    }
}
