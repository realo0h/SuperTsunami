package water.lava.superTsunami;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class TsunamiConfig {

    private static final Map<String, Long> DEFAULT_SPEEDS = Map.of(
        "water",
        7L,
        "lava",
        45L,
        "powder_snow",
        15L
    );

    private final Map<Material, Long> spreadSpeeds = new HashMap<>();
    private final int maxRadius;
    private final Set<Material> whitelist;
    private final Set<Material> replaceableExtra;

    public TsunamiConfig(FileConfiguration config, Logger logger) {
        for (Map.Entry<String, Long> entry : DEFAULT_SPEEDS.entrySet()) {
            long speed = config.getLong(
                "spread-speed." + entry.getKey(),
                entry.getValue()
            );
            parseMaterial(entry.getKey().toUpperCase(), logger).ifPresent(mat ->
                spreadSpeeds.put(mat, Math.max(1L, speed))
            );
        }

        maxRadius = Math.max(0, config.getInt("max-radius", 200));

        whitelist = parseMaterialList(
            config.getStringList("block-whitelist"),
            logger
        );
        replaceableExtra = parseMaterialList(
            config.getStringList("replaceable-extra"),
            logger
        );
        whitelist.remove(Material.AIR);
    }

    public boolean isReplaceable(Material material) {
        return material == Material.AIR || replaceableExtra.contains(material);
    }

    public boolean isWhitelisted(Material material) {
        return whitelist.contains(material);
    }

    public long getSpreadSpeed(Material liquid) {
        return spreadSpeeds.getOrDefault(liquid, 10L);
    }

    public int getMaxRadius() {
        return maxRadius;
    }

    private static Set<Material> parseMaterialList(
        List<String> names,
        Logger logger
    ) {
        Set<Material> set = EnumSet.noneOf(Material.class);
        for (String name : names) {
            parseMaterial(name.toUpperCase(), logger).ifPresent(set::add);
        }
        return set;
    }

    private static Optional<Material> parseMaterial(
        String name,
        Logger logger
    ) {
        try {
            return Optional.of(Material.valueOf(name));
        } catch (IllegalArgumentException e) {
            logger.warning(
                "[SuperTsunami] Unknown material in config: " + name
            );
            return Optional.empty();
        }
    }
}
