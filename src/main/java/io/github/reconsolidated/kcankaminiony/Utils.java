package io.github.reconsolidated.kcankaminiony;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Utils {
    public static int getSpaceInInventory(Player player) {
        int count = 0;
        Inventory inv = player.getInventory();
        for (ItemStack item : inv.getStorageContents()) {
            if (item == null || item.getType().equals(Material.AIR)) {
                count++;
            }
        }
        return count;
    }

    public static void copyPersistentData(PersistentDataContainer from, PersistentDataContainer to) {
        for (NamespacedKey key : from.getKeys()) {
            try {
                to.set(key, PersistentDataType.STRING, from.get(key, PersistentDataType.STRING));
            } catch (Exception ignored) {}
            try {
                to.set(key, PersistentDataType.INTEGER, from.get(key, PersistentDataType.INTEGER));
            } catch (Exception ignored) {}
        }
    }

    public static int countItems(Player player, ItemStack itemStack) {
        int count = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null
                    && itemStack.getType().equals(item.getType())
                    && item.getItemMeta() != null && itemStack.getItemMeta() != null
                    && itemStack.getItemMeta().getPersistentDataContainer().getKeys()
                    .equals(item.getItemMeta().getPersistentDataContainer().getKeys())) {
                count+= item.getAmount();
            }
        }
        return count;
    }

    public static int removeItems(Player player, ItemStack itemStack, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null
                    && itemStack.getType().equals(item.getType())
                    && item.getItemMeta() != null && itemStack.getItemMeta() != null
                    && itemStack.getItemMeta().getPersistentDataContainer().getKeys()
                    .equals(item.getItemMeta().getPersistentDataContainer().getKeys())) {
                if (count + item.getAmount() <= amount) {
                    count += item.getAmount();
                    item.setAmount(0);
                } else if (count < amount) {
                    item.setAmount(item.getAmount() - (amount - count));
                    count = amount;
                }
            }
        }
        return amount-count;
    }
}
