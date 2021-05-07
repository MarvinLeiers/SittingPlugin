package de.marvinleiers.sitplugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class Updater
{
    private static final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private static final String PREFIX = "§9[§9§lMUpdater§9]§r";
    private final JavaPlugin plugin;
    private UpdateStatus status;
    private String message, updateMessage, downloadLink;

    public Updater(JavaPlugin plugin)
    {
        this.plugin = plugin;
        this.status = UpdateStatus.UNDEFINED;

        final String url = "https://api.marvinleiers.de/resource/" + plugin.getDescription().getName().toLowerCase().trim();

        execute(url);
    }

    private void execute(String url)
    {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String receivedData = "";
            updateMessage = null;
            status = UpdateStatus.UNDEFINED;

            receivedData = getData(url);

            if (receivedData == null)
            {
                sendMessage("§cCould not reach update server for " + plugin.getName());
                return;
            }

            JSONParser jsonParser = new JSONParser();

            try
            {
                JSONObject obj = (JSONObject) jsonParser.parse(receivedData);

                if (obj.containsKey("block"))
                {
                    boolean allow = !Boolean.parseBoolean((String) obj.get("block"));

                    if (!allow)
                        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
                }

                if (obj.get("version") != null)
                {
                    String newestVersion = (String) obj.get("version");
                    String currentVersion = plugin.getDescription().getVersion().replaceAll("-SNAPSHOT", ".");

                    if (Integer.parseInt(newestVersion.replace(".", ""))
                            > Integer.parseInt(currentVersion.replace(".", "")))
                    {
                        this.status = UpdateStatus.UPDATE;

                        downloadLink = (String) obj.get("link");

                        this.updateMessage = "§cThere is a newer version of §4§l" + plugin.getName() + "§c available. Download at: §e"
                                + downloadLink;

                        sendMessage(updateMessage);
                    }
                    else
                    {
                        this.status = UpdateStatus.NO_UPDATE;
                        this.updateMessage = "No update found. You are running the newest version of " + plugin.getName() + ". Yay!";

                        sendMessage(updateMessage);
                    }

                    if (obj.containsKey("message"))
                    {
                        this.message = ChatColor.translateAlternateColorCodes('&', (String) obj.get("message"));
                        sendMessage(message);
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
            if (this.status == UpdateStatus.UPDATE && updateMessage != null)
            {
                for (Player player : Bukkit.getOnlinePlayers())
                {
                    if (player.isOp())
                        sendUpdateNotification(player);
                }
            }
        });
    }

    private void sendMessage(String msg)
    {
        console.sendMessage(PREFIX + " " + msg);
    }

    public void sendUpdateNotification(Player player)
    {
        if (this.status == UpdateStatus.UPDATE && updateMessage != null)
            player.sendMessage(PREFIX + " §cThis version of §e" + plugin.getName() + " §cis outdated! Download here §e" + downloadLink);
    }

    public String getData(String urlString)
    {
        BufferedReader in = null;

        try
        {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            String redirect = connection.getHeaderField("Location");

            if (redirect != null)
                connection = new URL(redirect).openConnection();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            return in.readLine();
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            try
            {
                if (in == null)
                {
                    return null;
                }

                in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        /* alter code
        URL url = new URL(urlString);
        con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setInstanceFollowRedirects(true);
        con.setDoOutput(true);

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

         */
    }
}
