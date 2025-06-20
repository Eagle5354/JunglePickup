package me.junglepickup;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class JunglePickup extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private Set<UUID> disabledPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("junglepickup").setExecutor(this);
        getLogger().info("JunglePickup enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("JunglePickup disabled.");
    }

    private boolean addToInventory(Player player, ItemStack item) {
        PlayerInventory inv = player.getInventory();
        return inv.addItem(item).isEmpty();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!isEnabledFor(p)) return;
        e.setDropItems(false);
        for (ItemStack drop : e.getBlock().getDrops(p.getInventory().getItemInMainHand())) {
            if (!addToInventory(p, drop)) {
                p.getWorld().dropItemNaturally(e.getBlock().getLocation(), drop);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        Player p = e.getEntity().getKiller();
        if (!isEnabledFor(p)) return;
        for (ItemStack drop : e.getDrops()) {
            if (!addToInventory(p, drop)) {
                p.getWorld().dropItemNaturally(p.getLocation(), drop);
            }
        }
        e.getDrops().clear();
    }

    private boolean isEnabledFor(Player p) {
        return config.getBoolean("enabled", true)
            && p.hasPermission("junglepickup.use")
            && !disabledPlayers.contains(p.getUniqueId());
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("junglepickup.use")) {
            p.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            p.sendMessage("§eUsage: /junglepickup <on|off>");
            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            disabledPlayers.remove(p.getUniqueId());
            p.sendMessage("§aJunglePickup is now §lENABLED§r§a.");
        } else if (args[0].equalsIgnoreCase("off")) {
            disabledPlayers.add(p.getUniqueId());
            p.sendMessage("§cJunglePickup is now §lDISABLED§r§c.");
        } else {
            p.sendMessage("§eUsage: /junglepickup <on|off>");
        }
        return true;
    }
}
