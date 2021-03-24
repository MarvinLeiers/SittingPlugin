package de.marvinleiers.sitplugin;

import de.marvinleiers.sitplugin.commands.SitCommand;
import de.marvinleiers.sitplugin.utils.Updater;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class SitPlugin extends JavaPlugin
{
    private static ConsoleCommandSender console;

    @Override
    public void onEnable()
    {
        console = Bukkit.getConsoleSender();

        this.getCommand("sit").setExecutor(new SitCommand());
        this.getServer().getPluginManager().registerEvents(new SitCommand(), this);

        new Updater(this);

        console.sendMessage(" ");
        console.sendMessage(" ");
        console.sendMessage("\t\t\t§9Plugin by: §r");
        console.sendMessage(" ");
        console.sendMessage(" §9__  __                  _       _         _");
        console.sendMessage("§9|  \\/  | __ _ _ ____   _(_)_ __ | |    ___(_) ___ _ __ ___");
        console.sendMessage("§9| |\\/| |/ _` | '__\\ \\ / | | '_ \\| |   / _ | |/ _ | '__/ __|");
        console.sendMessage("§9| |  | | (_| | |   \\ V /| | | | | |__|  __| |  __| |  \\__ \\");
        console.sendMessage("§9|_|  |_|\\__,_|_|    \\_/ |_|_| |_|_____\\___|_|\\___|_|  |___/");
        console.sendMessage(" ");
        console.sendMessage("\t\t§bhttps://www.marvinleiers.de");
        console.sendMessage(" ");
        console.sendMessage(" ");
    }
}
