package io.github.reconsolidated.kcankaminiony.CustomInventory;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class TakeOnlyItem extends InventoryItem {
    private Consumer<InventoryClickEvent> onClick;

    public TakeOnlyItem(ItemStack item, int row, int column, Consumer<InventoryClickEvent> onClick) {
        super(item, row, column);
        this.onClick = onClick;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType().equals(Material.AIR)) {
            if (event.getClick().equals(ClickType.LEFT) || event.getClick().equals(ClickType.SHIFT_LEFT)) {
                onClick.accept(event);
            } else {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }


    }
}
