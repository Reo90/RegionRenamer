package it.reo90.regionrenameutility;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public static Main instance;

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("[RegionRenamer] Plugin enabled.");
        getCommand("rgrename").setExecutor(new RenameCommand());
        instance = this;
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("[RegionRenamer] Plugin disabled.");
    }

    public static Main getPluginInstance() {
        return instance;
    }
}
