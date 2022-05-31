package io.github.reconsolidated.kcankaminiony;

import io.github.reconsolidated.itemprovider.ItemProvider;
import io.github.reconsolidated.kcankaminiony.Minions.Minion;
import io.github.reconsolidated.kcankaminiony.Minions.MinionManager;
import io.github.reconsolidated.kcankaminiony.Recipes.Recipes;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class KcankaMiniony extends JavaPlugin {

    @Getter
    private MinionManager minionManager;

    @Getter
    private ItemProvider itemProvider;

    @Override
    public void onEnable() {

        itemProvider = getServer().getServicesManager().load(ItemProvider.class);
        if (itemProvider == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException("Couldn't load ItemProvider plugin. Make sure it's added as a dependency.");
        }

        // Plugin startup logic
        minionManager = new MinionManager(this);


        Recipes.init(this);
        Recipes.registerRecipes(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
