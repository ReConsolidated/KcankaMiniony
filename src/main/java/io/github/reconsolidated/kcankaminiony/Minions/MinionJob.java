package io.github.reconsolidated.kcankaminiony.Minions;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class MinionJob {
    private final Block block;
    private final String newType;

    public void execute() {
        if (newType.equalsIgnoreCase("GLOWSTONE_DUST")) {
            block.setType(Material.AMETHYST_BLOCK);
        } else {
            block.setType(Material.getMaterial(newType));
        }

    }
}
