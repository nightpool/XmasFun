package net.nightpool.bukkit.miracle;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.PlayerConnection;
import net.minecraft.server.ServerConnection;
import net.nightpool.bukkit.miracle.MiracleConnection.Algo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.CraftServer;

@SuppressWarnings("deprecation")
public class XmasMiraclePlugin extends JavaPlugin {

    static public Set<Integer> exceptions = new HashSet<Integer>();
    static {
        for (Material i : Material.values()) {
            if (!i.isOccluding()) { // Occluding, Solid, Transparent
                if(i.equals(Material.LAVA) ||
                   i.equals(Material.LEAVES)||
                   i.equals(Material.TRAPPED_CHEST)){continue;}
                exceptions.add(i.getId());
            }
            exceptions.add(Material.CHEST.getId());
        }
    }

    public Map<String, Algo> algo_pref;
    public Algo default_algo = Algo.STRIPED_X;
    
    public XmasMiraclePlugin(){
        algo_pref = new HashMap<String, Algo>();
    }
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
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final ChatColor r = ChatColor.RED;
        final ChatColor g = ChatColor.GREEN;
        
        if(!(sender instanceof CraftPlayer)){
            if (args.length > 0) {
                Algo a = Algo.getByString(args[0]);
                if (a != null) {
                    default_algo = a;
                    algo_pref.put(sender.getName(), a);
                    sender.sendMessage(g + "Changing pattern to " + r + a.name + g + "...");
                } else {
                    sender.sendMessage(r + "Supported " + g + "patterns" + r + ": ");
                    int n = 1;
                    for (Algo i : Algo.values()) {
                        ChatColor c = ((n % 2) == 0)? r : g;
                        sender.sendMessage("  " + c + i.name);
                        n++;
                    }
                }
            } else {
                sender.sendMessage(r+"Current "+g+"default "+r+"is"+r+": "+g+default_algo.name);
                sender.sendMessage(g+"Try "+r+"another "+g+"one"+r+"! "+g+"Supported "+r+"options "+g+"are"+r+": ");
                int n = 0;
                for(Algo i:Algo.values()){
                    ChatColor c = ((n%2) == 0)? r:g;
                    sender.sendMessage("  "+c+i.name);
                    n++;
                }
                sender.sendMessage(r+"("+g+"Use "+r+"/xmas "+g+"<pattern> "+r+"to "+g+"change "+r+"your "+g+"pattern"+r+")");
            }
            return true;
        }
        MiracleConnection con = ((MiracleConnection)((CraftPlayer)sender).getHandle().playerConnection);
        if(args.length > 0){
            Algo a = Algo.getByString(args[0]);
            if(a!=null){
                con.algo = a;
                Bukkit.getScheduler().runTask(this, new MarkDirtyTask(this, (CraftPlayer)sender));
                algo_pref.put(sender.getName(), a);
                sender.sendMessage(ChatColor.GREEN+"Changing pattern to "+ChatColor.RED+a.name+ChatColor.GREEN+"...");
            } else {
                sender.sendMessage(r+"Supported "+g+"patterns"+r+": ");
                int n = 1;
                for(Algo i:Algo.values()){
                    ChatColor c = ((n%2) == 0)? r:g;
                    sender.sendMessage("  "+c+i.name);
                    n++;
                }
            }
        } else {
            sender.sendMessage(r+"Your "+g+"current "+r+"pattern "+g+"is"+r+": "+g+con.algo.name);
            sender.sendMessage(g+"Try "+r+"another "+g+"one"+r+"! "+g+"Supported "+r+"options "+g+"are"+r+": ");
            int n = 0;
            for(Algo i:Algo.values()){
                ChatColor c = ((n%2) == 0)? r:g;
                sender.sendMessage("  "+c+i.name);
                n++;
            }
            sender.sendMessage(r+"("+g+"Use "+r+"/xmas "+g+"<pattern> "+r+"to "+g+"change "+r+"your "+g+"pattern"+r+")");
        }
        return true;
    }
    
}
