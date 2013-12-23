package net.nightpool.bukkit.miracle;

import org.bukkit.Bukkit;
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
