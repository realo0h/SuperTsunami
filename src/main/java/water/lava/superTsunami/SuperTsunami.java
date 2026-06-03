package water.lava.superTsunami;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SuperTsunami extends JavaPlugin implements Listener {

    private TsunamiConfig tsunamiConfig;

    private final List<AtomicBoolean> activeTokens = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadTsunamiConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SuperTsunami enabled. Stay dry.");
    }

    private void loadTsunamiConfig() {
        reloadConfig();
        tsunamiConfig = new TsunamiConfig(getConfig(), getLogger());
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!command.getName().equalsIgnoreCase("tsunami")) return false;

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /tsunami <reload|stop>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                loadTsunamiConfig();
                sender.sendMessage("§aSuperTsunami config reloaded.");
            }
            case "stop" -> {
                int count = activeTokens.size();
                activeTokens.forEach(t -> t.set(true));
                activeTokens.clear();
                sender.sendMessage(
                    "§aCancelled " + count + " active tsunami(s)."
                );
            }
            default -> sender.sendMessage(
                "§cUnknown subcommand. Use: reload, stop"
            );
        }
        return true;
    }

    private static final int[][] DIRS = {
        { 1, 0, 0 },
        { -1, 0, 0 },
        { 0, 0, 1 },
        { 0, 0, -1 },
    };

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Material liquid = liquidFor(event.getBucket());
        if (liquid == null) return;

        Block origin = event
            .getBlockClicked()
            .getRelative(event.getBlockFace());

        AtomicBoolean token = new AtomicBoolean(false);
        activeTokens.removeIf(AtomicBoolean::get);
        activeTokens.add(token);

        Set<Block> visited = new HashSet<>();
        visited.add(origin);

        long delay = tsunamiConfig.getSpreadSpeed(liquid);
        for (int[] dir : DIRS) {
            Block neighbour = origin.getRelative(dir[0], dir[1], dir[2]);
            if (
                !tsunamiConfig.isWhitelisted(neighbour.getType()) &&
                visited.add(neighbour)
            ) {
                new SpreadTask(
                    this,
                    tsunamiConfig,
                    neighbour,
                    liquid,
                    visited,
                    origin.getLocation(),
                    token
                ).runTaskLater(this, delay);
            }
        }
    }

    private static Material liquidFor(Material bucket) {
        return switch (bucket) {
            case WATER_BUCKET -> Material.WATER;
            case LAVA_BUCKET -> Material.LAVA;
            case POWDER_SNOW_BUCKET -> Material.POWDER_SNOW;
            default -> null;
        };
    }
}
