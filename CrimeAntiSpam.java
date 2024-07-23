package ru.crimeland.mc.crimeantispam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrimeAntiSpam extends JavaPlugin implements Listener {
//    private Set<UUID> newPlayers = new HashSet<>();
    private HashMap<UUID, Long> newPlayers = new HashMap<UUID, Long>();
    private int blockTime;
    private String blockMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        blockTime = config.getInt("block-time", 60) * 1000; // Время блокировки в секундах
        blockMessage = translateHexColorCodes(config.getString("block-message", "&cВы не можете использовать чат в течение первых {time} секунд после входа."));

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            newPlayers.put(player.getUniqueId(), System.currentTimeMillis());
         }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (newPlayers.containsKey(player.getUniqueId())) {
            long time = System.currentTimeMillis() - newPlayers.get(player.getUniqueId());
            if (time > blockTime){
                newPlayers.remove(player.getUniqueId());
                return;
            }
            event.setCancelled(true);
            player.sendMessage(blockMessage.replace("{time}", String.valueOf((blockTime - time) / 1000)));
        }
    }

    private String translateHexColorCodes(String message) {
        final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char ch : hex.toCharArray()) {
                replacement.append("\u00A7").append(ch);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
