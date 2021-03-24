package de.marvinleiers.sitplugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater
{
    private static ConsoleCommandSender console = Bukkit.getConsoleSender();
    private final JavaPlugin plugin;
    private UpdateStatus status;
    private String message, updateMessage;

    public Updater(JavaPlugin plugin)
    {
        this.plugin = plugin;
        this.status = UpdateStatus.UNDEFINED;

        final ConsoleCommandSender console = Bukkit.getConsoleSender();
        final String url = "https://api.marvinleiers.de/resource/" + plugin.getDescription().getName().toLowerCase();

        execute(url);
    }

    private void execute(String url)
    {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String receivedData = "";
            updateMessage = null;
            status = UpdateStatus.UNDEFINED;

            try
            {
                receivedData = getData(url);
            }
            catch (IOException e)
            {
                console.sendMessage("§cCould not reach update server for " + plugin.getName());
                return;
            }

            JSONParser jsonParser = new JSONParser();

            try
            {
                JSONObject obj = (JSONObject) jsonParser.parse(receivedData);

                if (obj.get("version") != null)
                {
                    String newestVersion = (String) obj.get("version");
                    String currentVersion = plugin.getDescription().getVersion().replaceAll("-SNAPSHOT", ".");

                    if (Integer.parseInt(newestVersion.replace(".", ""))
                            > Integer.parseInt(currentVersion.replace(".", "")))
                    {
                        this.status = UpdateStatus.UPDATE;

                        String downloadLink = (String) obj.get("link");

                        this.updateMessage = "§cThere is a newer version of §4§l" + plugin.getName() + "§c available. Download at: §e"
                                + downloadLink;

                        console.sendMessage(updateMessage);
                    }
                    else
                    {
                        this.status = UpdateStatus.NO_UPDATE;
                        this.updateMessage = "No update found. You are running the newest version of " + plugin.getName() + ". Yay!";

                        console.sendMessage(updateMessage);
                    }

                    if (obj.containsKey("message"))
                    {
                        this.message = ChatColor.translateAlternateColorCodes('&', (String) obj.get("message"));

                        console.sendMessage(message);
                    }

                    this.sendUpdateNotifications();
                }
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }, 0, 20 * 60 * 60);
    }

    private void sendUpdateNotifications()
    {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (status != UpdateStatus.UNDEFINED && updateMessage != null)
            {
                for (Player player : Bukkit.getOnlinePlayers())
                {
                    if (player.isOp())
                        player.sendMessage("§9[" + plugin.getName() + "] §7" + updateMessage);
                }
            }
        });
    }

    public void sendUpdateNotification(Player player)
    {
        if (status != UpdateStatus.UNDEFINED && updateMessage != null)
            player.sendMessage("§9[" + plugin.getName() + "] §7" + updateMessage);
    }

    public String getData(String urlString) throws IOException
    {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setInstanceFollowRedirects(true);
        con.setDoOutput(true);
        con.setDoInput(true);

        DataOutputStream output = new DataOutputStream(con.getOutputStream());
        output.close();

        DataInputStream input = new DataInputStream(con.getInputStream());

        int c;
        StringBuilder result = new StringBuilder();

        while ((c = input.read()) != -1)
        {
            result.append((char) c);
        }

        input.close();

        return result.toString();
    }
}
