package it.reo90.regionrenameutility;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RenameCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (args.length != 2) {
            return false;
        }

        String id = args[0];
        String newId = args[1];
        // regex pattern
        String regexPattern = "^[a-z0-9_-]+$";
        if (!newId.matches(regexPattern)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l» &e&lREGIONRENAME &fPuoi utilizzare solo lettere latine minuscole, numeri e underscore."));
            return true;
        }

        World world = player.getWorld();
        String worldName = player.getWorld().getName();
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        com.sk89q.worldedit.bukkit.BukkitWorld adaptedWorld = (com.sk89q.worldedit.bukkit.BukkitWorld) BukkitAdapter.adapt(world);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(adaptedWorld);

        Bukkit.getScheduler().runTaskAsynchronously(Main.getPluginInstance(), () -> {
            YamlConfiguration config = loadRegionConfig(worldName);
            if (config.contains("regions." + id)) {
                Map<String, Object> regionParams = config.getConfigurationSection("regions." + id).getValues(true);
                config.set("regions." + id, null);
                config.createSection("regions." + newId, regionParams);
                File wgFolder = new File(Bukkit.getPluginManager().getPlugin("WorldGuard").getDataFolder(), "worlds/" + worldName);
                File regionsConfigFile = new File(wgFolder, "regions.yml");
                try {
                    config.save(regionsConfigFile);
                    regionManager.load();

                } catch (IOException e) {
                    Bukkit.getLogger().warning("[RenameRegionUtility] Non è stato possibile salvare il config di WorldGuard");
                } catch (StorageException e) {
                    Bukkit.getLogger().warning("[RenameRegionUtility] Non è stato possibile ricaricare in automatico il config delle region di WorldGuard");
                }

                Bukkit.getScheduler().runTask(Main.getPluginInstance(), () -> {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l» &e&lREGIONRENAME &fRegion " + id + " rinominata con successo!"));
                });
            }
            else {
                Bukkit.getScheduler().runTask(Main.getPluginInstance(), () -> {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l» &e&lREGIONRENAME &fLa region indicata non esiste oppure non ti trovi nel mondo di appartenenza"));
                });
            }
        });
        return true;
    }

    public YamlConfiguration loadRegionConfig(String worldName) {
        File wgFolder = new File(Bukkit.getPluginManager().getPlugin("WorldGuard").getDataFolder(), "worlds/" + worldName);
        File regionsConfigFile = new File(wgFolder, "regions.yml");
        if (!regionsConfigFile.exists()) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(regionsConfigFile);
    }

}
