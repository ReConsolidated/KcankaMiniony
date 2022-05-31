package io.github.reconsolidated.kcankaminiony.Minions;

import io.github.reconsolidated.kcankaminiony.KcankaMiniony;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

public class CancelMinionDamage implements Listener {
    private final KcankaMiniony plugin;

    public CancelMinionDamage(KcankaMiniony plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMinionDamage(EntityDamageEvent event) {
        if (event.getEntity().getPersistentDataContainer().get(new NamespacedKey(plugin, "minion_type"), PersistentDataType.STRING) != null) {
            event.setCancelled(true);
        }
    }

}
