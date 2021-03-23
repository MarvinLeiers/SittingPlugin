package de.marvinleiers.sitplugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.ArrayList;

public final class SitPlugin extends JavaPlugin implements Listener
{
    private static ArrayList<Player> sitting = new ArrayList<>();

    @Override
    public void onEnable()
    {
        this.getCommand("sit").setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this, this);
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("§cOnly a player can perform this command!");
            return true;
        }

        Player player = (Player) sender;

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
}
