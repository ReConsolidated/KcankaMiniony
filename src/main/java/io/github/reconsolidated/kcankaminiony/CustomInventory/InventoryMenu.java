package io.github.reconsolidated.kcankaminiony.CustomInventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;


public class InventoryMenu implements Listener {

    private final List<InventoryItem> elements;
    protected final Plugin plugin;
    protected final Player player;
    protected Inventory inventory;

    private String title;

    public InventoryMenu(Plugin plugin, Player player, String title, int rows) {
        this.plugin = plugin;
        this.player = player;
        elements = new ArrayList<>();
        this.inventory = Bukkit.createInventory(player, rows*9, title);
        player.openInventory(inventory);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public InventoryMenu addItem(InventoryItem item) {
        // remove old items on this slot
        for (int i = 0; i<elements.size(); i++) {
            InventoryItem old = elements.get(i);
            if (old.getRow() == item.getRow() && old.getColumn() == item.getColumn()) {
                elements.remove(i);
                i--;
            }
        }
        elements.add(item);
        inventory.setItem((item.getRow()-1)*9 + item.getColumn()-1, item.getItem());
        return this;
    }

    protected void fillWithEmptyItems() {
        fillWith(Material.GRAY_STAINED_GLASS_PANE);
    }

    protected void fillWith(Material material) {
        for (int i = 0; i<inventory.getSize(); i++) {
            addItem(new ClickOnlyItem(new ItemStack(material), i/9 + 1, (i+1)%9 + 1, (ignored) -> {}));
        }
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        if (!event.getView().getTopInventory().equals(inventory)) return;

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inventory)) {
            if (event.getClick().isShiftClick()) {
                event.setCancelled(true);
            }
            if (event.getClick().isKeyboardClick()) {
                event.setCancelled(true);
            }
        }



        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inventory)) return;



        InventoryItem item = getInventoryItem(event.getSlot());
        if (item == null)  {
            // We don't want players to put items in empty spaces.
            event.setCancelled(true);
            return;
        }
        item.onClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getView().getTopInventory().equals(inventory)) return;
        if (!event.getInventory().equals(inventory)) return;
        // We don't want to deal with that yet
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (event instanceof InventoryClickEvent) return;

        // Cancel all events that are not taken into consideration here
        event.setCancelled(true);
    }

    private InventoryItem getInventoryItem(int slotNumber) {
        int row = slotNumber/9 + 1;
        int column = slotNumber%9 + 1;
        for (InventoryItem item : elements) {
            if (item.getRow() == row && item.getColumn() == column) return item;
        }
        return null;
    }

}
