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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class MaceManager {

    private final JavaPlugin plugin;
    private final Set<String> craftedMaces = new HashSet<>();

    public MaceManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a mace of the given type has already been crafted
     */
    public boolean isMaceCrafted(String maceType) {
        return craftedMaces.contains(maceType.toLowerCase());
    }

    /**
     * Mark a mace type as crafted
     */
    public void markMaceCrafted(String maceType) {
        craftedMaces.add(maceType.toLowerCase());
    }

    /**
     * Remove a mace type from the crafted list (useful for resetting)
     */
    public void unmarkMaceCrafted(String maceType) {
        craftedMaces.remove(maceType.toLowerCase());
    }

    /**
     * Get all crafted mace types
     */
    public Set<String> getCraftedMaces() {
        return new HashSet<>(craftedMaces);
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