package water.lava.superTsunami;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class SuperTsunami extends JavaPlugin implements Listener {

    private final Set<Block> spreadBlocks = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onLiquidPlace(PlayerBucketEmptyEvent event) {
        Material bucketType = event.getBucket();
        if (bucketType == Material.WATER_BUCKET || bucketType == Material.LAVA_BUCKET) {
            Block block = event.getBlockClicked().getRelative(event.getBlockFace());
            Material liquidType = bucketType == Material.WATER_BUCKET ? Material.WATER : Material.LAVA;
            spreadLiquid(block, 0, liquidType);
        }
    }

    private void spreadLiquid(Block block, int depth, Material liquidType) {
        if (spreadBlocks.contains(block)) return;

        spreadBlocks.add(block);

        if (block.getType() == Material.AIR) {
            block.setType(liquidType);

            long delay = liquidType == Material.WATER ? 7L : 45L;

            new BukkitRunnable() {
                @Override
                public void run() {
                    spreadLiquid(block.getRelative(1, 0, 0), depth + 1, liquidType);
                    spreadLiquid(block.getRelative(-1, 0, 0), depth + 1, liquidType);
                    spreadLiquid(block.getRelative(0, 0, 1), depth + 1, liquidType);
                    spreadLiquid(block.getRelative(0, 0, -1), depth + 1, liquidType);
                }
            }.runTaskLater(this, delay);
        }
    }
}
