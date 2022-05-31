package io.github.reconsolidated.kcankaminiony.Recipes;

import io.github.reconsolidated.kcankaminiony.KcankaMiniony;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class CorrectCrafting implements Listener {
    public CorrectCrafting(KcankaMiniony plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe() == null) return;

        if (event.getRecipe().getResult().getEnchantments().size() > 0) {
            for (ItemStack item : event.getInventory().getMatrix()) {
                if (item != null) {
                    if (event.getRecipe() instanceof ShapedRecipe) {
                        ShapedRecipe recipe = (ShapedRecipe) event.getRecipe();
                        for (ItemStack recipeItem : recipe.getIngredientMap().values()) {
                            if (recipeItem.getType().equals(item.getType())) {
                                item.setAmount(item.getAmount() - recipeItem.getAmount());
                            }
                        }
                    }
                }
            }
        }

    }
}
