package me.sothatsit.mobeggspawnerblocker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MobEggSpawnerBlocker extends JavaPlugin implements Listener
{
    
    private String message;
    private boolean blockCreative;
    
    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this, this);
        
        reloadConfiguration();
    }
    
    public void reloadConfiguration() {
        this.saveDefaultConfig();
        this.reloadConfig();
        
        if (!getConfig().isSet("message") || !getConfig().isString("message")) {
            getLogger().warning("\"message\" not set or invalid in config, resetting to default");
            getConfig().set("message", "&cChanging spawners using mob eggs is disabled on this server");
            saveConfig();
        }
        
        if (!getConfig().isSet("block-creative") || !getConfig().isBoolean("block-creative")) {
            getLogger().warning("\"block-creative\" not set or invalid in config, resetting to default");
            getConfig().set("block-creative", false);
            saveConfig();
        }
        
        this.message = getConfig().getString("message");
        this.blockCreative = getConfig().getBoolean("block-creative");
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (!blockCreative && e.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        
        if (e.getPlayer().isOp() || e.getPlayer().hasPermission("mobeggspawnerblocker.override"))
            return;
        
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        
        Block b = e.getClickedBlock();
        
        if (b == null)
            return;
        
        if (b.getType() != Material.SPAWNER)
            return;
        
        ItemStack i = e.getItem();
        
        if (i == null)
            return;
        
        if (!(i.getType().name().contains("SPAWN_EGG") || i.getType().name().contains("MONSTER_EGG")))
            return;
        
        CreatureSpawner cs = (CreatureSpawner) b.getState();
        
        final Location loc = cs.getLocation();
        final EntityType type = cs.getSpawnedType();
        
        if (message != null && !message.isEmpty())
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        
        e.setCancelled(true);
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                Block b = loc.getBlock();
                
                if (b == null || b.getType() != Material.SPAWNER)
                    return;
                
                CreatureSpawner cs = (CreatureSpawner) b.getState();
                
                cs.setSpawnedType(type);
            }
        });
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("mobeggspawnerblocker")) {
            if (!sender.isOp() && !sender.hasPermission("mobeggspawnerblocker.reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Error > &cYou do not have permission to run this command."));
                return true;
            }
            
            reloadConfiguration();
            sender.sendMessage(ChatColor.GREEN + "MobEggSpawnerBlocker config reloaded");
            
            return true;
        }
        
        return false;
    }
}