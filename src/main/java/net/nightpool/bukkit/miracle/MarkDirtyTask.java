package net.nightpool.bukkit.miracle;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;

public class MarkDirtyTask implements Runnable {

    public CraftPlayer player;
    private int radius;
    private double _radiusSquared;
    private Vector center;
    private Vector currentPoint;
    private int _x;
    private Plugin plugin;
    private long startTime;
    private Set<Integer> info_rows;
    private NumberFormat fmt;

    public MarkDirtyTask(Plugin plugin, CraftPlayer player) {
        if(!player.isOnline()){
            return;
        }
        this.plugin = plugin;
        this.player = player;
        radius = 12*16;
        _radiusSquared = (radius + 0.5) * (radius+0.5);
        center = player.getLocation().toVector();
        currentPoint = center.clone();
        _x = -radius;
        plugin.getLogger().info("Marking chunks for "+player.getName());
        String msg = "Updating blocks. Logging off and back on will speed this up.";
        StringBuilder msg_c = new StringBuilder();
        int n = 0;
        for(String i: msg.split(" ")){
            msg_c.append((n%2 == 0)?ChatColor.RED:ChatColor.GREEN);
            msg_c.append(i+" ");
            n++;
        }
        player.sendMessage(msg_c.toString());
        this.startTime = new Date().getTime();
        info_rows = Sets.newHashSet();
        for(int i = 1; i<=10; i++){
            info_rows.add((int) (((i)*((radius*2)/10.0))-radius));
        }
        fmt = NumberFormat.getPercentInstance();
        fmt.setMaximumFractionDigits(2);
    }

    @Override
    public void run() {
        if(_x<=radius && player.isOnline()){
            if(info_rows.contains(_x)){
                player.sendMessage(ChatColor.RED+fmt.format(((_x+radius)/(radius*2.0)))+ChatColor.GREEN+" completed");
            }
            currentPoint.setX(center.getX() + _x);
            for (int _z = -radius; _z <= radius; _z++){
                currentPoint.setZ(center.getZ() + _z);
                if (center.distanceSquared(currentPoint) <= _radiusSquared){
                    for(int y=0; y<255; y++){
                        player.getHandle().world.notify(currentPoint.getBlockX(), y, currentPoint.getBlockZ());   
                    }
                }
            }
            _x++;
            Bukkit.getScheduler().runTaskLater(plugin, this, 2);
        } else {
            plugin.getLogger().info("Completed marking chunks for "+player.getName()+". " +
            		""+(new Date().getTime()-startTime)/1000.0+" seconds elapsed.");
            player = null;
        }
    }
}
