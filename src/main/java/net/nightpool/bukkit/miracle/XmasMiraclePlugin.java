package net.nightpool.bukkit.miracle;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.PlayerConnection;
import net.minecraft.server.ServerConnection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.CraftServer;

public class XmasMiraclePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.hook(p);
        }
        getLogger().info(getName() + " version " + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) {
            this.unhook(p);
        }
        getLogger().info(getName() + " version " + getDescription().getVersion() + " disabled!");
    }

    public void hook(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        CraftServer server = (CraftServer) getServer();

        Location loc = player.getLocation();
        MiracleConnection handlerHook = new MiracleConnection(this, server.getServer(),
                craftPlayer.getHandle().playerConnection.networkManager, craftPlayer.getHandle());
        handlerHook.teleport(loc);

        // Set the old one as disconnected to prevent PacketKeepAlives from
        // building up in the highPriority queue
        // craftPlayer.getHandle().netServerHandler.disconnected = true;

        // The problem with just hooking via overwriting the NSH is that inside
        // of NetworkListenerThread there is an array
        // of NSHs that is looped over to send a Packet0KeepAlive, unfortunately
        // this list doesn't stay in sync
        // with our hook, so the Packet0KeepAlives will constantly build up in
        // the queue to send, but never actually get sent thus leaking memory.

        // So instead, we have to go into the class and manually replace the NSH
        // in the array with our hook so it's correctly
        // looped over and updated when the player disconnects.
        try {
            Field conField = ServerConnection.class.getDeclaredField("c");
            conField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<PlayerConnection> cons = (List<PlayerConnection>) conField.get(server.getHandle().getServer().ag());
            Iterator<PlayerConnection> it = cons.iterator();
            PlayerConnection pl;
            while(it.hasNext()) {
                pl = it.next();
                if (pl.player.getName().equals(player.getName())) {
                    it.remove();
                }
            }
            cons.add(handlerHook);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // And now set the NSH to our new hook
        craftPlayer.getHandle().playerConnection = handlerHook;

    }

    public void unhook(Player player) {
        // Nothing to do
    }
}
