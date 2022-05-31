package io.github.reconsolidated.kcankaminiony.Recipes;

import io.github.reconsolidated.kcankaminiony.KcankaMiniony;
import io.github.reconsolidated.kcankaminiony.Minions.MinionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

public class Recipes {
    public static void init(KcankaMiniony plugin) {
        new StopWrongRecipes(plugin);
        new CorrectCrafting(plugin);
    }


    public static void registerRecipes(KcankaMiniony plugin) {
        ShapedRecipe ironMinion = new ShapedRecipe(new NamespacedKey(plugin, "iron_minion"), MinionManager.getMinionItem(plugin, Material.IRON_ORE));
        ironMinion.shape("***", "*B*", "***");
        ironMinion.setIngredient('*', Material.IRON_BLOCK);
        ironMinion.setIngredient('B', Material.WOODEN_PICKAXE);
        Bukkit.getServer().addRecipe(ironMinion);

        ShapedRecipe goldMinion = new ShapedRecipe(new NamespacedKey(plugin, "gold_minion"), MinionManager.getMinionItem(plugin, Material.GOLD_ORE));
        goldMinion.shape("***", "*B*", "***");
        goldMinion.setIngredient('*', Material.GOLD_BLOCK);
        goldMinion.setIngredient('B', Material.WOODEN_PICKAXE);
        Bukkit.getServer().addRecipe(goldMinion);

        ShapedRecipe diamondMinion = new ShapedRecipe(new NamespacedKey(plugin, "diamond_minion"), MinionManager.getMinionItem(plugin, Material.DIAMOND_ORE));
        diamondMinion.shape("***", "*B*", "***");
        diamondMinion.setIngredient('*', Material.DIAMOND_BLOCK);
        diamondMinion.setIngredient('B', Material.WOODEN_PICKAXE);
        Bukkit.getServer().addRecipe(diamondMinion);

        ShapedRecipe titanMinion = new ShapedRecipe(new NamespacedKey(plugin, "titan_minion"), MinionManager.getMinionItem(plugin, Material.AMETHYST_BLOCK));
        titanMinion.shape("***", "*B*", "***");
        titanMinion.setIngredient('*', Material.AMETHYST_BLOCK);
        titanMinion.setIngredient('B', Material.WOODEN_PICKAXE);
        Bukkit.getServer().addRecipe(titanMinion);
    }
}
