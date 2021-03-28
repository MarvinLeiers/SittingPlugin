package de.marvinleiers.sitplugin.commands;

import de.marvinleiers.sitplugin.SitPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.ArrayList;

public class SitCommand implements CommandExecutor, Listener
{
    private static final FileConfiguration config = SitPlugin.getInstance().getConfig();
    private static final ArrayList<Player> sitting = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("§cOnly a player can perform this command!");
            return true;
        }

        Player player = (Player) sender;

        if (config.getBoolean("use-permission") && !player.hasPermission("mplugin.sitplugin.sit"))
        {
            player.sendMessage("§cError: §eYou are not permitted to do that!");
            return true;
        }

        if (!player.isOnGround())
        {
            player.sendMessage("§cYou cannot sit while you are in the air!");
            return true;
        }

        if (sitting.contains(player))
        {
            player.sendMessage("§cYou are already sitting!");
            return true;
        }

        sitting.add(player);

        Location location = player.getLocation();
        World world = location.getWorld();
        ArmorStand chair = (ArmorStand) world.spawnEntity(location.add(0, -1.6, 0), EntityType.ARMOR_STAND);

        chair.setGravity(false);
        chair.setVisible(false);
        chair.setInvulnerable(false);
        chair.addPassenger(player);

        return true;
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();

        if (!(event.getDismounted() instanceof ArmorStand))
            return;

        if (sitting.contains(player))
        {
            event.getDismounted().remove();
            sitting.remove(player);
        }
    }
}
