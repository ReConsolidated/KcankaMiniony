package io.github.reconsolidated.kcankaminiony.Recipes;

import io.github.reconsolidated.kcankaminiony.KcankaMiniony;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class StopWrongRecipes implements Listener {
    public StopWrongRecipes(KcankaMiniony plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null) {
                for (Enchantment enchantment : item.getEnchantments().keySet()) {
                    if (!event.getRecipe().getResult().containsEnchantment(enchantment)) {
                        event.getInventory().setResult(new ItemStack(Material.AIR));
                        Bukkit.broadcastMessage("Nie pozwalam na crafting (brak enchantu).");
                        return;
                    }
                }
            }
        }

        if (event.getRecipe().getResult().getEnchantments().size() > 0) {
            for (ItemStack item : event.getInventory().getMatrix()) {
                if (item != null) {
                    if (event.getRecipe() instanceof ShapedRecipe) {
                        ShapedRecipe recipe = (ShapedRecipe) event.getRecipe();
                        for (ItemStack recipeItem : recipe.getIngredientMap().values()) {
                            if (recipeItem.getType().equals(item.getType())) {
                                if (recipeItem.getAmount() != item.getAmount()) {
                                    event.getInventory().setResult(new ItemStack(Material.AIR));
                                    Bukkit.broadcastMessage("Nie pozwalam na crafting (zła ilość).");
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
