package hs.elementalMaces.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import hs.elementalMaces.ElementalMaces;
import org.bukkit.event.player.PlayerJoinEvent;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.*;

public class MaceListener implements Listener {

    private final ElementalMaces plugin;
    private final Set<UUID> firePassthroughPlayers = new HashSet<>();
    private final Map<UUID, IronGolem> playerGolems = new HashMap<>();
    private final Map<UUID, String> playerElements = new HashMap<>();

    public MaceListener(ElementalMaces plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        String maceType = plugin.getMaceManager().getMaceType(result);

        if (maceType != null) {
            if (plugin.getMaceManager().isMaceCrafted(maceType)) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    UUID owner = plugin.getMaceManager().getMaceOwner(maceType);
                    String ownerName = owner != null ? Bukkit.getOfflinePlayer(owner).getName() : "someone";
                    player.sendMessage(ChatColor.RED + "Only one " + maceType + " mace can exist! (owned by " + ownerName + ")");
                }
            } else {
                plugin.getMaceManager().markMaceCrafted(maceType, ((Player) event.getWhoClicked()).getUniqueId());
                if (event.getWhoClicked() instanceof Player player) {
                    player.sendMessage(ChatColor.GOLD + "You crafted the legendary " + maceType + " mace!");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Roll for random element
        String[] elements = {"air", "fire", "ocean", "earth"};
        String element = elements[(int) (Math.random() * 4)];
        playerElements.put(player.getUniqueId(), element);

        // Apply passive effects based on element
        switch (element) {
            case "air":
                player.sendMessage(ChatColor.AQUA + "You are attuned to AIR! No fall damage.");
                break;
            case "fire":
                player.sendMessage(ChatColor.RED + "You are attuned to FIRE! Fire immunity and damage boost when burning.");
                break;
            case "ocean":
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, Integer.MAX_VALUE, 0, false, false));
                player.sendMessage(ChatColor.BLUE + "You are attuned to OCEAN! Water breathing and fast swimming.");
                break;
            case "earth":
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 4, false, false));
                player.sendMessage(ChatColor.GREEN + "You are attuned to EARTH! Mining speed and golden apple food effects.");
                break;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        String mainType = plugin.getMaceManager().getMaceType(mainHand);
        String offType = plugin.getMaceManager().getMaceType(offHand);

        // Ocean mace: 5x water speed
        if (("ocean".equals(mainType) || "ocean".equals(offType) || "ocean".equals(playerElements.get(player.getUniqueId()))) && player.isInWater()) {
            Vector vel = player.getVelocity();
            if (vel.length() > 0) {
                vel.setX(vel.getX() * 5).setZ(vel.getZ() * 5);
                player.setVelocity(vel);
            }
        }

        // Fire mace: +2 damage when on fire
        if (("fire".equals(mainType) || "fire".equals(offType) || "fire".equals(playerElements.get(player.getUniqueId()))) && player.getFireTicks() > 0) {
            player.removePotionEffect(PotionEffectType.STRENGTH);
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20, 0, false, false));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        String maceType = plugin.getMaceManager().getMaceType(item);
        if (maceType == null) return;

        switch (maceType) {
            case "air":
                handleAirMaceAbility(player, event);
                break;
            case "fire":
                handleFireMaceAbility(player, event);
                break;
            case "ocean":
                handleOceanMaceAbility(player, event);
                break;
            case "earth":
                handleEarthMaceAbility(player, event);
                break;
        }
    }

    private void handleAirMaceAbility(Player player, PlayerInteractEvent event) {
        if (player.isSneaking()) {
            // Ability 2: Wind Strike
            if (plugin.getCooldownManager().hasCooldown(player, "air_strike")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "air_strike");
                player.sendMessage(ChatColor.RED + "Wind Strike on cooldown for " + remaining + " seconds!");
                return;
            }

            // Put players in 5x5 area in cobwebs and give slow falling
            Location center = player.getLocation();
            for (Entity entity : center.getWorld().getNearbyEntities(center, 2.5, 2.5, 2.5)) {
                if (entity instanceof Player target && !target.equals(player)) {
                    if (!plugin.getTrustManager().isTrusted(player, target)) {
                        createCobwebTrap(target.getLocation());
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0));
                    }
                } else if (entity instanceof LivingEntity mob && !(mob instanceof Player)) {
                    createCobwebTrap(mob.getLocation());
                    mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0));
                }
            }

            plugin.getCooldownManager().setCooldown(player, "air_strike", 20);
            player.sendMessage(ChatColor.AQUA + "Wind Strike activated!");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.AQUA + "Wind Strike - 20s cooldown"));
        } else {
            // Ability 1: Wind Shot
            if (plugin.getCooldownManager().hasCooldown(player, "air_shot")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "air_shot");
                player.sendMessage(ChatColor.RED + "Wind Shot on cooldown for " + remaining + " seconds!");
                return;
            }

            // Spawn wind charge
            WindCharge windCharge = (WindCharge) player.getWorld().spawnEntity(
                    player.getEyeLocation().add(player.getLocation().getDirection()),
                    EntityType.WIND_CHARGE
            );
            windCharge.setVelocity(player.getLocation().getDirection().multiply(2));

            plugin.getCooldownManager().setCooldown(player, "air_shot", 5);
            player.sendMessage(ChatColor.AQUA + "Wind Shot fired!");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.AQUA + "Wind Shot - 5s cooldown"));
        }
    }

    private void handleFireMaceAbility(Player player, PlayerInteractEvent event) {
        if (player.isSneaking()) {
            // Ability 2: Meteors
            if (plugin.getCooldownManager().hasCooldown(player, "meteors")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "meteors");
                player.sendMessage(ChatColor.RED + "Meteors on cooldown for " + remaining + " seconds!");
                return;
            }

            Location center = player.getLocation();
            for (int i = 0; i < 10; i++) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Random location in 5x5 area
                        double x = center.getX() + (Math.random() - 0.5) * 5;
                        double z = center.getZ() + (Math.random() - 0.5) * 5;
                        double y = center.getY() + 10;

                        Location meteorLoc = new Location(center.getWorld(), x, y, z);
                        Fireball meteor = (Fireball) center.getWorld().spawnEntity(meteorLoc, EntityType.FIREBALL);
                        meteor.setDirection(new Vector(0, -1, 0));
                        meteor.setYield(0); // No block damage

                        // Schedule damage when meteor hits
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Location impactLoc = meteor.getLocation();
                                for (Entity entity : impactLoc.getWorld().getNearbyEntities(impactLoc, 2, 2, 2)) {
                                    if (entity instanceof Player target && !target.equals(player)) {
                                        if (!plugin.getTrustManager().isTrusted(player, target)) {
                                            target.damage(4.0); // 2 hearts true damage
                                        }
                                    } else if (entity instanceof LivingEntity mob && !(mob instanceof Player)) {
                                        mob.damage(4.0); // 2 hearts true damage
                                    }
                                }
                                meteor.remove();
                            }
                        }.runTaskLater(plugin, 40L); // 2 seconds delay
                    }
                }.runTaskLater(plugin, i * 5L); // Stagger meteors
            }

            plugin.getCooldownManager().setCooldown(player, "meteors", 25);
            player.sendMessage(ChatColor.RED + "Meteors summoned!");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Meteors - 25s cooldown"));
        } else {
            // Ability 1: Fire Passthrough
            if (plugin.getCooldownManager().hasCooldown(player, "fire_passthrough")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "fire_passthrough");
                player.sendMessage(ChatColor.RED + "Fire Passthrough on cooldown for " + remaining + " seconds!");
                return;
            }

            firePassthroughPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Fire Passthrough activated for 5 seconds!");

            new BukkitRunnable() {
                @Override
                public void run() {
                    firePassthroughPlayers.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "Fire Passthrough ended");
                }
            }.runTaskLater(plugin, 100L);

            plugin.getCooldownManager().setCooldown(player, "fire_passthrough", 10);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Fire Passthrough - 10s cooldown"));
        }
    }

    private void handleOceanMaceAbility(Player player, PlayerInteractEvent event) {
        if (player.isSneaking()) {
            // Ability 2: Water Geyser
            if (plugin.getCooldownManager().hasCooldown(player, "water_geyser")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "water_geyser");
                player.sendMessage(ChatColor.RED + "Water Geyser on cooldown for " + remaining + " seconds!");
                return;
            }

            Location center = player.getLocation();
            for (Entity entity : center.getWorld().getNearbyEntities(center, 5, 5, 5)) {
                if (entity instanceof Player target && !target.equals(player)) {
                    if (!plugin.getTrustManager().isTrusted(player, target)) {
                        target.setVelocity(new Vector(0, 2, 0)); // Launch upwards
                    }
                } else if (entity instanceof LivingEntity mob && !(mob instanceof Player)) {
                    mob.setVelocity(new Vector(0, 2, 0)); // Launch upwards
                }
            }

            // Create water effect
            center.getWorld().spawnParticle(Particle.SPLASH, center, 50, 2, 0, 2, 1);
            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);

            plugin.getCooldownManager().setCooldown(player, "water_geyser", 30);
            player.sendMessage(ChatColor.BLUE + "Water Geyser activated!");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.BLUE + "Water Geyser - 30s cooldown"));
        } else {
            // Ability 1: Water Heal
            if (plugin.getCooldownManager().hasCooldown(player, "water_heal")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "water_heal");
                player.sendMessage(ChatColor.RED + "Water Heal on cooldown for " + remaining + " seconds!");
                return;
            }

            double currentHealth = player.getHealth();
            double maxHealth = player.getMaxHealth();
            double healAmount = Math.min(4.0, maxHealth - currentHealth); // 2 hearts

            player.setHealth(currentHealth + healAmount);
            player.sendMessage(ChatColor.BLUE + "Healed for " + (healAmount / 2) + " hearts!");

            // Healing particles
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 5);

            plugin.getCooldownManager().setCooldown(player, "water_heal", 10);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.BLUE + "Water Heal - 10s cooldown"));
        }
    }

    private void handleEarthMaceAbility(Player player, PlayerInteractEvent event) {
        if (player.isSneaking()) {
            // Ability 2: Tornado Pull
            if (plugin.getCooldownManager().hasCooldown(player, "tornado_pull")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "tornado_pull");
                player.sendMessage(ChatColor.RED + "Tornado Pull on cooldown for " + remaining + " seconds!");
                return;
            }

            Location center = player.getLocation();
            for (Entity entity : center.getWorld().getNearbyEntities(center, 2.5, 2.5, 2.5)) {
                if (entity instanceof Player target && !target.equals(player)) {
                    if (!plugin.getTrustManager().isTrusted(player, target)) {
                        Vector direction = center.toVector().subtract(target.getLocation().toVector()).normalize();
                        target.setVelocity(direction.multiply(2));
                    }
                } else if (entity instanceof LivingEntity mob && !(mob instanceof Player)) {
                    Vector direction = center.toVector().subtract(mob.getLocation().toVector()).normalize();
                    mob.setVelocity(direction.multiply(2));
                }
            }

            // Create tornado effect
            center.getWorld().spawnParticle(Particle.CLOUD, center, 30, 2, 2, 2, 0.1);
            center.getWorld().playSound(center, Sound.ENTITY_WITHER_SHOOT, 1.0f, 2.0f);

            plugin.getCooldownManager().setCooldown(player, "tornado_pull", 15);
            player.sendMessage(ChatColor.GREEN + "Tornado Pull activated!");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Tornado Pull - 15s cooldown"));
        } else {
            // Ability 1: Buddy Up
            if (plugin.getCooldownManager().hasCooldown(player, "buddy_up")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "buddy_up");
                player.sendMessage(ChatColor.RED + "Buddy Up on cooldown for " + remaining + " seconds!");
                return;
            }

            // Remove existing golem if any
            IronGolem existingGolem = playerGolems.get(player.getUniqueId());
            if (existingGolem != null && !existingGolem.isDead()) {
                existingGolem.remove();
            }

            // Spawn new iron golem
            IronGolem golem = (IronGolem) player.getWorld().spawnEntity(
                    player.getLocation().add(2, 0, 0),
                    EntityType.IRON_GOLEM
            );
            golem.setMaxHealth(20.0); // 10 hearts
            golem.setHealth(20.0);
            golem.setCustomName(ChatColor.GREEN + player.getName() + "'s Buddy");
            golem.setCustomNameVisible(true);

            playerGolems.put(player.getUniqueId(), golem);

            plugin.getCooldownManager().setCooldown(player, "buddy_up", 15);
            player.sendMessage(ChatColor.GREEN + "Iron Golem buddy summoned!");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Buddy Up - 15s cooldown"));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Handle mace hit effects
        if (event.getDamager() instanceof Player attacker) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            String maceType = plugin.getMaceManager().getMaceType(weapon);

            if (maceType != null && event.getEntity() instanceof Player victim) {
                if (!plugin.getTrustManager().isTrusted(attacker, victim)) {
                    handleMaceHitEffect(attacker, victim, maceType);
                } else {
                    event.setCancelled(true); // Cancel damage between trusted players
                    return;
                }
            }
        }

        // Handle fire passthrough
        if (event.getEntity() instanceof Player player) {
            if (firePassthroughPlayers.contains(player.getUniqueId())) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                        event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                        event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                    // Convert to true damage
                    event.setDamage(event.getFinalDamage());
                }
            }
        }

        // Handle iron golem protection
        if (event.getEntity() instanceof Player player) {
            IronGolem golem = playerGolems.get(player.getUniqueId());
            if (golem != null && !golem.isDead() && event.getDamager() instanceof Player attacker) {
                if (!plugin.getTrustManager().isTrusted(player, attacker)) {
                    golem.setTarget(attacker);
                }
            }
        }
    }

    private void createCobwebTrap(Location targetLoc) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location webLoc = targetLoc.clone().add(x, 0, z);
                if (webLoc.getBlock().getType() == Material.AIR) {
                    webLoc.getBlock().setType(Material.COBWEB);
                    // Remove cobwebs after 5 seconds
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (webLoc.getBlock().getType() == Material.COBWEB) {
                                webLoc.getBlock().setType(Material.AIR);
                            }
                        }
                    }.runTaskLater(plugin, 100L);
                }
            }
        }
    }

    private void handleMaceHitEffectMob(Player attacker, Entity victim, String maceType) {
        switch (maceType) {
            case "air":
                if (victim instanceof LivingEntity living) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0));
                }
                break;
            case "fire":
                victim.setFireTicks(60); // 3 seconds of fire
                break;
            case "ocean":
                // 1% chance for mining fatigue
                if (Math.random() < 0.01 && victim instanceof LivingEntity living) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 2));
                    attacker.sendMessage(ChatColor.BLUE + "Mining fatigue applied!");
                }
                break;
        }
    }

    private void handleMaceHitEffect(Player attacker, Player victim, String maceType) {
        switch (maceType) {
            case "air":
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0));
                break;
            case "fire":
                victim.setFireTicks(60); // 3 seconds of fire
                break;
            case "ocean":
                // 1% chance for mining fatigue
                if (Math.random() < 0.01) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 2));
                    attacker.sendMessage(ChatColor.BLUE + "Mining fatigue applied!");
                }
                break;
            case "earth":
                // No special hit effect for earth mace
                break;
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        String mainMaceType = plugin.getMaceManager().getMaceType(mainHand);
        String offMaceType = plugin.getMaceManager().getMaceType(offHand);

        // Air mace: No fall damage
        if ((mainMaceType != null && mainMaceType.equals("air")) ||
                (offMaceType != null && offMaceType.equals("air"))) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }

        // Fire mace: No fire damage, +2 damage when on fire
        if ((mainMaceType != null && mainMaceType.equals("fire")) ||
                (offMaceType != null && offMaceType.equals("fire"))) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                    event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());

        // Remove old effects
        String oldMaceType = plugin.getMaceManager().getMaceType(oldItem);
        if (oldMaceType != null) {
            removeMacePassiveEffects(player, oldMaceType);
        }

        // Apply new effects
        String newMaceType = plugin.getMaceManager().getMaceType(newItem);
        if (newMaceType != null) {
            applyMacePassiveEffects(player, newMaceType);
        }
    }

    private void applyMacePassiveEffects(Player player, String maceType) {
        switch (maceType) {
            case "ocean":
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, Integer.MAX_VALUE, 0, false, false));
                break;
            case "earth":
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 4, false, false));
                break;
        }
    }

    private void removeMacePassiveEffects(Player player, String maceType) {
        switch (maceType) {
            case "ocean":
                player.removePotionEffect(PotionEffectType.CONDUIT_POWER);
                break;
            case "earth":
                player.removePotionEffect(PotionEffectType.HASTE);
                break;
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        String mainMaceType = plugin.getMaceManager().getMaceType(mainHand);
        String offMaceType = plugin.getMaceManager().getMaceType(offHand);

        // Earth mace: All food acts like golden apples
        if ((mainMaceType != null && mainMaceType.equals("earth")) ||
                (offMaceType != null && offMaceType.equals("earth"))) {

            // Give regeneration and absorption like golden apples
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
        }
    }
}