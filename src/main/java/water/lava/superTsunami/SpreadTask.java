package water.lava.superTsunami;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SpreadTask extends BukkitRunnable {

    private static final int[][] DIRS = {
        { 1, 0, 0 },
        { -1, 0, 0 },
        { 0, 0, 1 },
        { 0, 0, -1 },
    };

    private final JavaPlugin plugin;
    private final TsunamiConfig config;
    private final Block block;
    private final Material liquid;
    private final Set<Block> visited;
    private final Location origin;
    private final AtomicBoolean cancelled;

    public SpreadTask(
        JavaPlugin plugin,
        TsunamiConfig config,
        Block block,
        Material liquid,
        Set<Block> visited,
        Location origin,
        AtomicBoolean cancelled
    ) {
        this.plugin = plugin;
        this.config = config;
        this.block = block;
        this.liquid = liquid;
        this.visited = visited;
        this.origin = origin;
        this.cancelled = cancelled;
    }

    @Override
    public void run() {
        if (cancelled.get()) return;

        int maxRadius = config.getMaxRadius();
        if (maxRadius > 0) {
            double dx = block.getX() - origin.getX();
            double dz = block.getZ() - origin.getZ();
            if (dx * dx + dz * dz > (double) maxRadius * maxRadius) return;
        }

        Material current = block.getType();
        if (
            config.isWhitelisted(current) || !config.isReplaceable(current)
        ) return;

        block.setType(liquid);

        long delay = config.getSpreadSpeed(liquid);
        for (int[] dir : DIRS) {
            Block neighbor = block.getRelative(dir[0], dir[1], dir[2]);
            if (config.isWhitelisted(neighbor.getType())) continue;
            if (visited.add(neighbor)) {
                new SpreadTask(
                    plugin,
                    config,
                    neighbor,
                    liquid,
                    visited,
                    origin,
                    cancelled
                ).runTaskLater(plugin, delay);
            }
        }
    }
}
