package hs.elementalMaces;

import org.bukkit.plugin.java.JavaPlugin;
import hs.elementalMaces.commands.TrustCommand;
import hs.elementalMaces.listeners.MaceListener;
import hs.elementalMaces.managers.TrustManager;
import hs.elementalMaces.managers.CooldownManager;
import hs.elementalMaces.managers.MaceManager;

public final class ElementalMaces extends JavaPlugin {

    private TrustManager trustManager;
    private CooldownManager cooldownManager;
    private MaceManager maceManager;

    @Override
    public void onEnable() {
        // Initialize managers
        this.trustManager = new TrustManager(this);
        this.cooldownManager = new CooldownManager();
        this.maceManager = new MaceManager(this);

        // Register commands
        getCommand("trust").setExecutor(new TrustCommand(trustManager));
        getCommand("untrust").setExecutor(new TrustCommand(trustManager));
        getCommand("trustlist").setExecutor(new TrustCommand(trustManager));

        // Register listeners
        getServer().getPluginManager().registerEvents(new MaceListener(this), this);

        // Create mace recipes
        maceManager.createMaceRecipes();

        getLogger().info("ElementalMaces plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ElementalMaces plugin has been disabled!");
    }

    public TrustManager getTrustManager() {
        return trustManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public MaceManager getMaceManager() {
        return maceManager;
    }
}