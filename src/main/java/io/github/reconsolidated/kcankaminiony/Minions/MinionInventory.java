package io.github.reconsolidated.kcankaminiony.Minions;

import io.github.reconsolidated.kcankaminiony.CustomInventory.ClickOnlyItem;
import io.github.reconsolidated.kcankaminiony.CustomInventory.InventoryMenu;
import io.github.reconsolidated.kcankaminiony.CustomInventory.TakeOnlyItem;
import io.github.reconsolidated.kcankaminiony.KcankaMiniony;

import io.github.reconsolidated.kcankaminiony.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MinionInventory extends InventoryMenu {
    private Minion minion;

    public MinionInventory(KcankaMiniony plugin, Player player, Minion minion, String title, int rows) {
        super(plugin, player, title, rows);

        this.minion = minion;

        fillWithEmptyItems();

        addItem(new ClickOnlyItem(getItemStack("pickup"), 2, 4, (event) -> {
            if (Utils.getSpaceInInventory(player) < 0) {
                player.sendMessage(ChatColor.RED + "Musisz mieć miejsce w ekwipunku, żeby to zrobić!");
                inventory.close();
            } else {
                ItemStack result = minion.destroyAndGetItemStack();
                plugin.getMinionManager().removeMinion(minion);
                player.getInventory().addItem(result);
                player.sendMessage(ChatColor.GREEN + "Podniesiono miniona!");
                player.closeInventory();
            }
        }));

        addItem(new ClickOnlyItem(getItemStack("info"), 2, 5, (event) -> {
            int level = minion.getLevel();
            int levelUpCost = minion.getLevelUpCost();
            int amountPlayerHas = Utils.countItems(player, minion.getItem());

        }));

        addItem(new ClickOnlyItem(getItemStack("levelup"), 2, 6, (event) -> {
            int levelUpCost = minion.getLevelUpCost();
            int amountPlayerHas = Utils.countItems(player, minion.getItem());

            if (amountPlayerHas >= levelUpCost) {
                minion.levelUp();
                Utils.removeItems(player, minion.getItem(), levelUpCost);
                player.sendMessage(ChatColor.GREEN + "Zwiększono poziom miniona do %d".formatted(minion.getLevel()));
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "Nie masz wystarczająco wiele itemów tego typu!");
            }
        }));

        for (int i = 0; i<minion.getInventorySize(); i++) {
            int finalI = i;
            addItem(new TakeOnlyItem(minion.getInventoryItem(i), 4+(i/5), 4+(i%5), (event) -> {
                minion.setInventoryItem(new ItemStack(Material.AIR), finalI);
            }));
        }
    }


    /*
    [1, 5] - minion info
    [1, 6] - upgrade
    [1, 4] - pickup

    [3, 4] - [5, 8] - items
     */


    private ItemStack getItemStack(String name) {
        switch (name) {
            case "pickup" -> {
                ItemStack is = new ItemStack(Material.REDSTONE);
                ItemMeta meta = is.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Podnieś");
                meta.setLore(List.of("", ChatColor.YELLOW + "Kliknij, by podnieść", ChatColor.YELLOW + "tego miniona."));
                is.setItemMeta(meta);
                return is;
            }
            case "levelup" -> {
                ItemStack is = new ItemStack(Material.GLOWSTONE);
                ItemMeta meta = is.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Poziom " + minion.getLevel());
                meta.setLore(List.of(
                        "",
                        ChatColor.YELLOW + "Koszt: " + minion.getLevelUpCost(),
                        ChatColor.YELLOW + "Kliknij, by zwiększyć poziom",
                        ChatColor.YELLOW + "tego miniona."
                ));
                is.setItemMeta(meta);
                return is;
            }
        }
        return new ItemStack(Material.GRAY_WOOL);
    }

}
