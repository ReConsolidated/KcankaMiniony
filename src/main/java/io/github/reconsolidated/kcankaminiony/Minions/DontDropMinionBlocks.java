package io.github.reconsolidated.kcankaminiony.Minions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DontDropMinionBlocks implements Listener  {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.AMETHYST_BLOCK) {
            if (!block.getLocation().getNearbyEntitiesByType(ArmorStand.class, 2).isEmpty()) {
                event.setDropItems(true);
            }
        }
    }
}
