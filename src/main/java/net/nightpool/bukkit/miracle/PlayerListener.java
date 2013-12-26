package net.nightpool.bukkit.miracle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private XmasMiraclePlugin plugin;
    public PlayerListener(XmasMiraclePlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent e){
        //Doing this 1 tick later can result in some chunks not being fully hidden (usually just the one they spawn in)
        //  We have to do it 1 tick later becuase the NSH is set in NetworkListenThread AFTER the player join event
        //  so we can't correctly remove it until the next tick. Possibly another event would be better, but didn't look
        //  into which one
        final ChatColor r = ChatColor.RED;
        final ChatColor g = ChatColor.GREEN;
        String s1 = "Merry Christmas!";
        String s2 = "Try /xmas for some more options";
        StringBuilder s1c = new StringBuilder();
        int n = 0;
        for(char c : s1.toCharArray()){
            s1c.append(((n%2)==0)? r:g);
            s1c.append(c);
            n++;
        }
        StringBuilder s2c = new StringBuilder();
        n = 1;
        for(char c : s2.toCharArray()){
            s2c.append(((n%2)==0)? r:g);
            s2c.append(c);
            n++;
        }
        e.getPlayer().sendMessage(s1c.toString());
        e.getPlayer().sendMessage(s2c.toString());
        Bukkit.getScheduler().runTask(plugin,new Runnable() {
            @Override
            public void run() {
                plugin.hook(e.getPlayer());
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent e){
        plugin.unhook((e.getPlayer()));
    }
}
