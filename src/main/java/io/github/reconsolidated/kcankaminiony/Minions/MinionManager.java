package io.github.reconsolidated.kcankaminiony.Minions;

import io.github.reconsolidated.kcankaminiony.KcankaMiniony;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
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
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;


public class MinionManager extends BukkitRunnable implements Listener {

    public static NamespacedKey minionCooldownKey;
    public static NamespacedKey minionInventorySizeKey;
    public static NamespacedKey minionLevelKey;
    public static NamespacedKey minionBreaksRequiredKey;
    public static NamespacedKey minionOwnerKey;
    private final KcankaMiniony plugin;
    private List<Minion> minions;
    public static NamespacedKey playerMinionCountKey;
    public static NamespacedKey minionTypeKey;


    public MinionManager(KcankaMiniony plugin) {
        this.plugin = plugin;
        playerMinionCountKey = new NamespacedKey(plugin, "player_minion_count");
        minionOwnerKey = new NamespacedKey(plugin, "minion_owner");
        minionTypeKey = new NamespacedKey(plugin, "minion_type");
        minionCooldownKey = new NamespacedKey(plugin, "minion_cooldown");
        minionInventorySizeKey = new NamespacedKey(plugin, "minion_inventory_size");
        minionLevelKey = new NamespacedKey(plugin, "minion_level");
        minionBreaksRequiredKey = new NamespacedKey(plugin, "minion_breaks_required");

        minions = new ArrayList<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(new DontDropMinionBlocks(), plugin);
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
            container.set(minionTypeKey, PersistentDataType.STRING, "titan_ash");
            container.set(minionCooldownKey, PersistentDataType.INTEGER, 200);
            container.set(minionInventorySizeKey, PersistentDataType.INTEGER, 3);
            container.set(minionLevelKey, PersistentDataType.INTEGER, 1);
            container.set(minionBreaksRequiredKey, PersistentDataType.INTEGER, 8640);
        } else {
            container.set(minionTypeKey, PersistentDataType.STRING, "titan_ash");
            container.set(minionCooldownKey, PersistentDataType.INTEGER, 200);
            container.set(minionInventorySizeKey, PersistentDataType.INTEGER, 3);
            container.set(minionLevelKey, PersistentDataType.INTEGER, 1);
            container.set(minionBreaksRequiredKey, PersistentDataType.INTEGER, 8640);
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
        return entity instanceof ArmorStand && entity.getPersistentDataContainer().get(minionTypeKey, PersistentDataType.STRING) != null;
    }

    public boolean isMinion(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return item.getItemMeta().getPersistentDataContainer().get(minionTypeKey, PersistentDataType.STRING) != null;
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
            if (minion.getOwnerName().equalsIgnoreCase(event.getPlayer().getName())) {
                new MinionInventory(plugin, event.getPlayer(), minion, "Minion", 5);
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "To nie twój minion!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMinionPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        ItemStack item = event.getItemInHand().clone();
        if (!isMinion(item)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        if (canPlaceMinion(player)) {
            Minion minion = new Minion(plugin, event.getBlockPlaced().getLocation(), item, event.getPlayer().getName());
            if (minion.spawnWithPersistentData(item.getItemMeta().getPersistentDataContainer())) {
                event.getPlayer().sendMessage(ChatColor.GREEN + "Postawiono miniona.");
                setMinionCount(player, getMinionCount(player) + 1);
                item.setAmount(1);
                event.getPlayer().getInventory().removeItem(item);
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "Nie można postawić tu miniona, bloki dookoła muszą być puste!");
            }
        } else {
            if (player.getWorld().getName().equals("oneblock_world")) {
                player.sendMessage(ChatColor.RED + "Osiągnięto już limit minionów!");
            } else {
                player.sendMessage(ChatColor.RED + "Nie możesz tego tu zrobić!");
            }
        }

    }

    private boolean canPlaceMinion(Player player) {
        return player.isOp() ||
                (player.getWorld().getName().equals("oneblock_world") && getMaxMinions(player) > getMinionCount(player));
    }

    private int getMinionCount(Player player) {
        Integer value = player.getPersistentDataContainer().get(playerMinionCountKey, PersistentDataType.INTEGER);
        if (value == null) {
            return 0;
        }
        return value;
    }

    private int getMaxMinions(Player player)  {
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            if (perm.getPermission().startsWith("minions.max.")) {
                String value = perm.getPermission().split("\\.")[2];
                if (value.equalsIgnoreCase("*")) {
                    return 10000;
                }
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException exception) {
                    Bukkit.getLogger().warning(
                            "Player %s has incorrect minion permission: %s"
                                    .formatted(player.getName(), perm.getPermission()));
                    return 1;
                }
            }
        }
        return 1;

    }

    private void setMinionCount(Player player, int value) {
        player.getPersistentDataContainer().set(playerMinionCountKey, PersistentDataType.INTEGER, value);
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (isMinion(entity)
                    && minions.stream().noneMatch((minion -> minion.getMinion().equals(entity)
            ))) {
                minions.add(new Minion(plugin, (ArmorStand) entity));
            }
        });

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
