package io.github.reconsolidated.kcankaminiony.Minions;

import io.github.reconsolidated.kcankaminiony.KcankaMiniony;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class MinionManager extends BukkitRunnable implements Listener {

    private final KcankaMiniony plugin;
    private List<Minion> minions;
    private final NamespacedKey hasMinionKey;

    public MinionManager(KcankaMiniony plugin) {
        this.plugin = plugin;
        hasMinionKey = new NamespacedKey(plugin, "has_minion");
        minions = new ArrayList<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        runTaskTimer(plugin, 0L, 1L);

        for (World world : Bukkit.getWorlds()) {
            loadWorldMinions(world);
        }

    }

    public static ItemStack getMinionItem(KcankaMiniony plugin, Material material) {
        ItemStack head = plugin.getItemProvider().getItem("minion", "minion");
        ItemMeta meta = head.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (material == Material.AMETHYST_BLOCK) {
            container.set(new NamespacedKey(plugin, "minion_type"), PersistentDataType.STRING, "titan_ash");
            container.set(new NamespacedKey(plugin, "minion_cooldown"), PersistentDataType.INTEGER, 200);
            container.set(new NamespacedKey(plugin, "minion_inventory_size"), PersistentDataType.INTEGER, 3);
            container.set(new NamespacedKey(plugin, "minion_level"), PersistentDataType.INTEGER, 1);
            container.set(new NamespacedKey(plugin, "minion_breaks_required"), PersistentDataType.INTEGER, 8640);
        } else {
            container.set(new NamespacedKey(plugin, "minion_type"), PersistentDataType.STRING, "titan_ash");
            container.set(new NamespacedKey(plugin, "minion_cooldown"), PersistentDataType.INTEGER, 60);
            container.set(new NamespacedKey(plugin, "minion_inventory_size"), PersistentDataType.INTEGER, 3);
            container.set(new NamespacedKey(plugin, "minion_level"), PersistentDataType.INTEGER, 1);
            container.set(new NamespacedKey(plugin, "minion_breaks_required"), PersistentDataType.INTEGER, 1);
        }

        head.setItemMeta(meta);
        return head;
    }

    @Override
    public void run() {
        for (Minion minion : minions) {
            minion.run();
        }
    }

    public boolean isMinion(Entity entity) {
        return entity instanceof ArmorStand && entity.getPersistentDataContainer().get(new NamespacedKey(plugin, "minion_type"), PersistentDataType.STRING) != null;
    }

    public boolean isMinion(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "minion_type"), PersistentDataType.STRING) != null;
    }

    public Minion getMinion(Entity entity) {
        for (Minion minion : minions) {
            if (minion.getMinion().equals(entity)) {
                return minion;
            }
        }
        return null;
    }

    @EventHandler
    public void onMinionClick(PlayerInteractAtEntityEvent event) {
        if (isMinion(event.getRightClicked())) {
            event.setCancelled(true);
            Minion minion = getMinion(event.getRightClicked());
            if (minion == null) {
                Bukkit.getLogger().warning("Someone clicked on a minion, but it's not loaded! " + event.getRightClicked().getLocation());
                return;
            }
            new MinionInventory(plugin, event.getPlayer(), minion, "Minion", 5);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMinionPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        ItemStack item = event.getItemInHand().clone();
        if (!isMinion(item)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!hasMinion(player)) {
            Minion minion = new Minion(plugin, event.getBlockPlaced().getLocation(), item, event.getPlayer().getName());
            if (minion.spawnWithPersistentData(item.getItemMeta().getPersistentDataContainer())) {
                event.getPlayer().sendMessage(ChatColor.GREEN + "Postawiono miniona.");
                item.setAmount(1);
                event.getPlayer().getInventory().removeItem(item);
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "Nie można postawić tu miniona, bloki dookoła muszą być puste!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Masz już postawionego miniona!");
        }

    }

    private boolean hasMinion(Player player) {
        return player.getPersistentDataContainer().get(hasMinionKey, PersistentDataType.STRING) != null;
    }

    private void setHasMinion(Player player, boolean value) {

    }


    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Entity entity : event.getChunk().getEntities()) {
                loadIfMinion(entity);
            }
        }, 100L);
    }

    private void loadIfMinion(Entity entity) {
        if (isMinion(entity)
                && minions.stream().noneMatch((minion -> minion.getMinion().equals(entity)
        ))) {
            minions.add(new Minion(plugin, (ArmorStand) entity));
        }
    }


    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        loadWorldMinions(event.getWorld());
    }

    private void loadWorldMinions(World world) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            world.getEntitiesByClass(ArmorStand.class).forEach(this::loadIfMinion);
        }, 100L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            world.getEntitiesByClass(ArmorStand.class).forEach(this::loadIfMinion);
        }, 1000L);
    }


    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        for (int i = 0; i<minions.size(); i++) {
            if (minions.get(i).getLocation().getWorld().equals(event.getWorld())) {
                minions.remove(i);
                i--;
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (int i = 0; i<minions.size(); i++) {
                if (minions.get(i).getLocation().getChunk().equals(event.getChunk())) {
                    minions.remove(i);
                    i--;
                }
            }
        });
    }


    public void addMinion(Minion minion) {
        minions.add(minion);
    }

    public void removeMinion(Minion minion) {
        minions.remove(minion);
    }
}
