package hs.elementalMaces.managers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class MaceManager {

    private final JavaPlugin plugin;
    private final Map<String, UUID> maceOwners = new HashMap<>();
    private final File dataFile;
    private FileConfiguration config;

    public MaceManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "maces.yml");
        loadMaceData();
    }

    public boolean isMaceCrafted(String maceType) {
        return maceOwners.containsKey(maceType.toLowerCase());
    }

    public void markMaceCrafted(String maceType, UUID playerUUID) {
        maceOwners.put(maceType.toLowerCase(), playerUUID);
        saveMaceData();
    }

    public void resetMace(String maceType) {
        maceOwners.remove(maceType.toLowerCase());
        saveMaceData();
    }

    public void resetAllMaces() {
        maceOwners.clear();
        saveMaceData();
    }

    public UUID getMaceOwner(String maceType) {
        return maceOwners.get(maceType.toLowerCase());
    }

    private void loadMaceData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().warning("Failed to create maces.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : config.getKeys(false)) {
            String uuidStr = config.getString(key);
            if (uuidStr != null) {
                try {
                    maceOwners.put(key, UUID.fromString(uuidStr));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID for mace " + key + ": " + uuidStr);
                }
            }
        }
    }

    public void saveMaceData() {
        for (Map.Entry<String, UUID> entry : maceOwners.entrySet()) {
            config.set(entry.getKey(), entry.getValue().toString());
        }
        for (String key : config.getKeys(false)) {
            if (!maceOwners.containsKey(key)) {
                config.set(key, null);
            }
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save maces.yml: " + e.getMessage());
        }
    }

    public ItemStack createAirMace() {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "Air Mace");
        List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Harness the power of wind!",
                ChatColor.YELLOW + "Ability 1: Wind Shot (5s CD)",
                ChatColor.YELLOW + "Ability 2: Wind Strike (20s CD)",
                ChatColor.GREEN + "Passives:",
                ChatColor.GREEN + "• No fall damage when held",
                ChatColor.GREEN + "• Hit gives slow falling",
                ChatColor.GREEN + "• Wind charges pull players"
        );
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "mace_type"),
                PersistentDataType.STRING,
                "air"
        );

        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        mace.setItemMeta(meta);

        return mace;
    }

    public ItemStack createFireMace() {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "Fire Mace");
        List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Wield the flames of destruction!",
                ChatColor.YELLOW + "Ability 1: Fire Passthrough (10s CD)",
                ChatColor.YELLOW + "Ability 2: Meteors (25s CD)",
                ChatColor.GREEN + "Passives:",
                ChatColor.GREEN + "• No fire damage",
                ChatColor.GREEN + "• +2 damage when on fire",
                ChatColor.GREEN + "• Hit ignites target"
        );
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "mace_type"),
                PersistentDataType.STRING,
                "fire"
        );

        meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        mace.setItemMeta(meta);

        return mace;
    }

    public ItemStack createOceanMace() {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();

        meta.setDisplayName(ChatColor.BLUE + "Ocean Mace");
        List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Control the depths of the sea!",
                ChatColor.YELLOW + "Ability 1: Water Heal (10s CD)",
                ChatColor.YELLOW + "Ability 2: Water Geyser (30s CD)",
                ChatColor.GREEN + "Passives:",
                ChatColor.GREEN + "• 5x faster in water",
                ChatColor.GREEN + "• Conduit power",
                ChatColor.GREEN + "• 1% chance for mining fatigue on hit"
        );
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "mace_type"),
                PersistentDataType.STRING,
                "ocean"
        );

        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        mace.setItemMeta(meta);

        return mace;
    }

    public ItemStack createEarthMace() {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "Earth Mace");
        List<String> lore = Arrays.asList(
                ChatColor.GRAY + "Command the strength of the earth!",
                ChatColor.YELLOW + "Ability 1: Buddy Up (15s CD)",
                ChatColor.YELLOW + "Ability 2: Tornado Pull (CD varies)",
                ChatColor.GREEN + "Passives:",
                ChatColor.GREEN + "• Haste 5 when held",
                ChatColor.GREEN + "• Food acts like golden apples"
        );
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "mace_type"),
                PersistentDataType.STRING,
                "earth"
        );

        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        mace.setItemMeta(meta);

        return mace;
    }

    public String getMaceType(ItemStack item) {
        if (item == null || item.getType() != Material.MACE) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "mace_type"),
                PersistentDataType.STRING
        );
    }

    public void createMaceRecipes() {
        // Air Mace Recipe
        ItemStack airMace = createAirMace();
        NamespacedKey airKey = new NamespacedKey(plugin, "air_mace");
        ShapedRecipe airRecipe = new ShapedRecipe(airKey, airMace);
        airRecipe.shape("FFF", " S ", " S ");
        airRecipe.setIngredient('F', Material.FEATHER);
        airRecipe.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(airRecipe);

        // Fire Mace Recipe
        ItemStack fireMace = createFireMace();
        NamespacedKey fireKey = new NamespacedKey(plugin, "fire_mace");
        ShapedRecipe fireRecipe = new ShapedRecipe(fireKey, fireMace);
        fireRecipe.shape("FFF", " S ", " S ");
        fireRecipe.setIngredient('F', Material.FIRE_CHARGE);
        fireRecipe.setIngredient('S', Material.BLAZE_ROD);
        plugin.getServer().addRecipe(fireRecipe);

        // Ocean Mace Recipe
        ItemStack oceanMace = createOceanMace();
        NamespacedKey oceanKey = new NamespacedKey(plugin, "ocean_mace");
        ShapedRecipe oceanRecipe = new ShapedRecipe(oceanKey, oceanMace);
        oceanRecipe.shape("PPP", " S ", " S ");
        oceanRecipe.setIngredient('P', Material.PRISMARINE_SHARD);
        oceanRecipe.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(oceanRecipe);

        // Earth Mace Recipe
        ItemStack earthMace = createEarthMace();
        NamespacedKey earthKey = new NamespacedKey(plugin, "earth_mace");
        ShapedRecipe earthRecipe = new ShapedRecipe(earthKey, earthMace);
        earthRecipe.shape("III", " S ", " S ");
        earthRecipe.setIngredient('I', Material.IRON_INGOT);
        earthRecipe.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(earthRecipe);
    }
}