package io.github.reconsolidated.kcankaminiony.Minions;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.github.reconsolidated.kcankaminiony.KcankaMiniony;
import io.github.reconsolidated.kcankaminiony.Utils;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static io.github.reconsolidated.kcankaminiony.Minions.MinionManager.*;
import static io.github.reconsolidated.kcankaminiony.Minions.MinionPhase.BREAKING;
import static io.github.reconsolidated.kcankaminiony.Minions.MinionPhase.PLACING;

public class Minion {

    private final KcankaMiniony plugin;
    @Getter
    private final Location location;
    private int jobCooldown;
    private String displayName;
    @Getter
    private ArmorStand minion;
    @Getter
    private ItemStack item;
    private final List<Block> blocks;
    private MinionPhase currentPhase = BREAKING;

    @Getter
    private int level;

    private int breaksRequiredForDrop = 1;

    private final int defaultCooldown = 200;


    private int doJobIn = 0;
    private MinionJob nextJob = null;
    private int nextDropCount = 0;
    @Getter
    private String ownerName = "";

    public Minion(KcankaMiniony plugin, Location location, ItemStack item, int jobCooldown, int level) {
        this.plugin = plugin;
        this.location = location.toCenterLocation();
        this.blocks = new ArrayList<>();
        this.item = item;
        this.jobCooldown = jobCooldown;
        this.level = level;
    }


    public Minion(KcankaMiniony plugin, ArmorStand minion) {
        this.minion = minion;
        this.plugin = plugin;
        this.location = minion.getLocation().toCenterLocation();
        this.blocks = new ArrayList<>();
        String type = minion.getPersistentDataContainer().get(minionTypeKey, PersistentDataType.STRING);
        if (type.equalsIgnoreCase("glowstone_dust") || type.equalsIgnoreCase("titan_ash")) {
            item = plugin.getItemProvider().getItem("titan_ash", "titan_ash");
        } else {
            item = new ItemStack(Material.getMaterial(type));
        }
        this.displayName = "Minion Tytanów";

        this.jobCooldown = minion.getPersistentDataContainer().get(minionCooldownKey, PersistentDataType.INTEGER);
        this.level = minion.getPersistentDataContainer().get(minionLevelKey, PersistentDataType.INTEGER);
        this.breaksRequiredForDrop = minion.getPersistentDataContainer().get(minionBreaksRequiredKey, PersistentDataType.INTEGER);
        this.ownerName = minion.getPersistentDataContainer().get(minionOwnerKey, PersistentDataType.STRING);


        setupBlocks();
    }

    public Minion(KcankaMiniony plugin, Location location, ItemStack item, String ownerName) {
        this.plugin = plugin;
        this.location = location.toCenterLocation();
        String type = item.getItemMeta().getPersistentDataContainer().get(minionTypeKey, PersistentDataType.STRING);
        if (type.equalsIgnoreCase("titan_ash") || type.equalsIgnoreCase("glowstone_dust")) {
            this.item = plugin.getItemProvider().getItem("titan_ash", "titan_ash");
        } else {
            this.item = new ItemStack(Material.getMaterial(type));
        }
        this.displayName = ChatColor.translateAlternateColorCodes('&', "&8[&d⛏&8] &bMinion Tytanów");

        this.jobCooldown = item.getItemMeta().getPersistentDataContainer().get(minionCooldownKey, PersistentDataType.INTEGER);
        this.level = item.getItemMeta().getPersistentDataContainer().get(minionLevelKey, PersistentDataType.INTEGER);
        this.breaksRequiredForDrop = item.getItemMeta().getPersistentDataContainer().get(minionBreaksRequiredKey, PersistentDataType.INTEGER);
        this.ownerName = ownerName;
        this.blocks = new ArrayList<>();
        setupBlocks();
    }

    private void setupBlocks() {
        blocks.add(location.getWorld().getBlockAt(location.getBlockX() - 1, location.getBlockY() -1, location.getBlockZ() - 1));
        blocks.add(location.getWorld().getBlockAt(location.getBlockX() - 1, location.getBlockY() -1, location.getBlockZ()));
        blocks.add(location.getWorld().getBlockAt(location.getBlockX() - 1, location.getBlockY() -1, location.getBlockZ() + 1));
        blocks.add(location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() -1, location.getBlockZ() - 1));
        blocks.add(location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() -1, location.getBlockZ() + 1));
        blocks.add(location.getWorld().getBlockAt(location.getBlockX() + 1, location.getBlockY() -1, location.getBlockZ() - 1));
        blocks.add(location.getWorld().getBlockAt(location.getBlockX() + 1, location.getBlockY() -1, location.getBlockZ()));
        blocks.add(location.getWorld().getBlockAt(location.getBlockX() + 1, location.getBlockY() -1, location.getBlockZ() + 1));
    }

    public int getLevelUpCost() {
        return level * (level+5);
    }

    public void levelUp() {
        level += 1;
        jobCooldown = defaultCooldown / level;
        saveMinion();
    }

    private void saveMinion() {
        minion.getPersistentDataContainer().set(minionTypeKey, PersistentDataType.STRING, plugin.getItemProvider().getItemName(item));
        minion.getPersistentDataContainer().set(minionCooldownKey, PersistentDataType.INTEGER, jobCooldown);
        minion.getPersistentDataContainer().set(minionLevelKey, PersistentDataType.INTEGER, level);
        minion.getPersistentDataContainer().set(minionBreaksRequiredKey, PersistentDataType.INTEGER, breaksRequiredForDrop);
        minion.getPersistentDataContainer().set(minionOwnerKey, PersistentDataType.STRING, ownerName);
    }


    // This is ran every tick
    public void run() {
        if (!isAnyPlayerNearby()) {
            return;
        }

        doJobIn = Math.max(0, doJobIn - 1);
        if (doJobIn == 0 && nextJob != null) {
            if (currentPhase == BREAKING) {
                nextDropCount += 1;
                if (nextDropCount >= breaksRequiredForDrop) {
                    putInventoryItem(item);
                    nextDropCount = 0;
                }
            }
            nextJob.execute();
            startMiningAnimation(3);
            nextJob = null;
        }

        if (currentPhase == PLACING) {
            if (nextJob == null) {
                for (Block block : blocks) {
                    if (block.getType().equals(Material.AIR)) {
                        if (nextJob == null) {
                            nextJob = new MinionJob(block, item.getType().name());
                            startRotatingToBlockAnimation(block);
                            doJobIn = jobCooldown;
                        }
                    }
                }
            }
            if (nextJob == null) {
                currentPhase = BREAKING;
            }
        }

        if (currentPhase == BREAKING) {
            if (nextJob == null) {
                for (Block block : blocks) {
                    if (!block.getType().equals(Material.AIR)) {
                        if (nextJob == null) {
                            nextJob = new MinionJob(block, "AIR");
                            startRotatingToBlockAnimation(block);
                            doJobIn = jobCooldown;
                        }
                    }
                }
            }
            if (nextJob == null) {
                currentPhase = PLACING;
            }
        }
    }

    private boolean isAnyPlayerNearby() {
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= 30 * 30) {
                return true;
            }
        }
        return false;
    }

    public ItemStack destroyAndGetItemStack() {
        ItemStack itemStack = getHead(item.getType().toString());
        ItemMeta meta = itemStack.getItemMeta();
        Utils.copyPersistentData(minion.getPersistentDataContainer(), meta.getPersistentDataContainer());
        itemStack.setItemMeta(meta);
        minion.remove();
        return itemStack;
    }

    public boolean spawnWithPersistentData(PersistentDataContainer container) {
        if (!spawn()) return false;

        Utils.copyPersistentData(container, minion.getPersistentDataContainer());
        return true;
    }

    public boolean spawn() {
        setupBlocks();

        for (Block block : blocks) {
            if (!block.getType().equals(Material.AIR)) {
                return false;
            }
        }


        minion = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, -0.5, 0), EntityType.ARMOR_STAND);
        minion.setSmall(true);
        minion.setArms(true);
        minion.setGravity(false);
        minion.setCanMove(false);
        minion.setBasePlate(false);
        minion.setPersistent(true);
        minion.setCustomNameVisible(true);
        minion.setInvulnerable(true);
        minion.customName(Component.text(ChatColor.translateAlternateColorCodes('&', displayName)));
        minion.getEquipment().setHelmet(getHead(item.getType().toString()));
        minion.getEquipment().setChestplate(getLeatherArmor(Material.LEATHER_CHESTPLATE, Color.PURPLE));
        minion.getEquipment().setLeggings(getLeatherArmor(Material.LEATHER_LEGGINGS, Color.FUCHSIA));
        minion.getEquipment().setBoots(getLeatherArmor(Material.LEATHER_BOOTS, Color.PURPLE));
        minion.getEquipment().setItemInMainHand(getMainhandItem());
        minion.getPersistentDataContainer().set(minionTypeKey, PersistentDataType.STRING, item.getType().name());
        minion.getPersistentDataContainer().set(minionCooldownKey, PersistentDataType.INTEGER, jobCooldown);
        minion.getPersistentDataContainer().set(minionLevelKey, PersistentDataType.INTEGER, level);

        plugin.getMinionManager().addMinion(this);


        return true;
    }

    public void startRotatingToBlockAnimation(Block block) {
        Vector finalDirection = block.getLocation().toVector().subtract(minion.getLocation().toBlockLocation().toVector()).normalize();

        Location location = minion.getLocation().clone().setDirection(finalDirection);
        float finalYaw = location.getYaw();
        minion.setRotation(finalYaw, minion.getLocation().getPitch());
    }

    public void startMiningAnimation(int times) {
        if (times <= 0) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                minion.setRightArmPose(minion.getRightArmPose().add(0.2, 0, 0));
                if (minion.getRightArmPose().getX() >= 0.4) {
                    cancel();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (minion.getRightArmPose().getX() <= -0.8) {
                                startMiningAnimation(times-1);
                                cancel();
                            }
                            minion.setRightArmPose(minion.getRightArmPose().add(-0.2, 0, 0));
                        }
                    }.runTaskTimer(plugin, 2L, 1L);
                }

            }
        }.runTaskTimer(plugin, 2L, 1L);
    }

    private ItemStack getMainhandItem() {
        return new ItemStack(Material.STONE_PICKAXE);
    }

    private ItemStack getLeatherArmor(Material type, Color color) {
        ItemStack item = new ItemStack(type);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack getHead(String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        PlayerProfile playerProfile = Bukkit.createProfile(name);
        meta.setPlayerProfile(playerProfile);
        head.setItemMeta(meta);
        return head;
    }

    public ItemStack getInventoryItem(int number) {
        int inventorySize = getInventorySize();
        if (number >= inventorySize) throw new RuntimeException("Attempt to get inventory item out of bounds: " + number + ", max: " + inventorySize);
        String item = minion.getPersistentDataContainer().get(new NamespacedKey(plugin, "minion_inventory_item_" + number), PersistentDataType.STRING);
        if (item == null) {
            minion.getPersistentDataContainer().set(new NamespacedKey(plugin, "minion_inventory_item_" + number), PersistentDataType.STRING, "AIR;0");
            return new ItemStack(Material.AIR);
        }
        String itemName = item.split(";")[0];
        String countString = item.split(";")[1];
        int count = Integer.parseInt(countString);
        ItemStack itemStack = plugin.getItemProvider().getItem("minions", itemName);
        if (itemStack.getType().equals(Material.GLOWSTONE_DUST)) {
            itemStack = plugin.getItemProvider().getItem("titan_ash", "titan_ash");
        }
        itemStack.setAmount(count);
        return itemStack;
    }

    public void setInventoryItem(ItemStack item, int number) {
        String value = plugin.getItemProvider().getItemName(item) + ";" + item.getAmount();
        minion.getPersistentDataContainer().set(new NamespacedKey(plugin, "minion_inventory_item_" + number), PersistentDataType.STRING, value);
    }

    private void putInventoryItem(ItemStack item) {
        if (item.getType().equals(Material.GLOWSTONE_DUST)) {
            item = plugin.getItemProvider().getItem("titan_ash", "titan_ash");
        }


        String name = plugin.getItemProvider().getItemName(item);
        int amount = item.getAmount();
        for (int i = 0; i<getInventorySize(); i++) {
            if (amount <= 0) return;
            ItemStack item2 = getInventoryItem(i);
            if (item2 == null || item2.getType().equals(Material.AIR)) {
                setInventoryItem(item, i);
                return;
            }
            String name2 = plugin.getItemProvider().getItemName(item2);
            if (name.equalsIgnoreCase(name2)) {
                if (item2.getAmount() < item2.getType().getMaxStackSize()) {
                    if (amount > item2.getType().getMaxStackSize()) {
                        amount -= (item2.getMaxStackSize() - item2.getAmount());
                        item2.setAmount(item2.getType().getMaxStackSize());
                    } else {
                        item2.setAmount(item2.getAmount() + amount);
                        amount = 0;
                    }
                    setInventoryItem(item2, i);
                }
            }
        }
    }

    public int getInventorySize() {
        Integer result = minion.getPersistentDataContainer().get(minionInventorySizeKey, PersistentDataType.INTEGER);
        if (result == null) {
            minion.getPersistentDataContainer().set(minionInventorySizeKey, PersistentDataType.INTEGER, 3);
            return 3;
        }
        return result;
    }


    public void setInventorySize(int i) {
        minion.getPersistentDataContainer().set(minionInventorySizeKey, PersistentDataType.INTEGER, i);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setMinion(ArmorStand entity) {
        this.minion = entity;
    }
}
