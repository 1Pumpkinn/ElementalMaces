package hs.elementalMaces.managers;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public void setCooldown(Player player, String ability, int seconds) {
        UUID playerUUID = player.getUniqueId();
        cooldowns.computeIfAbsent(playerUUID, k -> new HashMap<>())
                .put(ability, System.currentTimeMillis() + (seconds * 1000L));
    }

    public boolean hasCooldown(Player player, String ability) {
        UUID playerUUID = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);

        if (playerCooldowns == null) {
            return false;
        }

        Long cooldownTime = playerCooldowns.get(ability);
        if (cooldownTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= cooldownTime) {
            playerCooldowns.remove(ability);
            return false;
        }

        return true;
    }

    public int getRemainingCooldown(Player player, String ability) {
        UUID playerUUID = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);

        if (playerCooldowns == null) {
            return 0;
        }

        Long cooldownTime = playerCooldowns.get(ability);
        if (cooldownTime == null) {
            return 0;
        }

        long remaining = cooldownTime - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) + 1 : 0;
    }
}