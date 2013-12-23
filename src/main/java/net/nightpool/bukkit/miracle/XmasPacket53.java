package net.nightpool.bukkit.miracle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet53BlockChange;

public class XmasPacket53 extends Packet53BlockChange {

    public MiracleConnection con;
    
    static{
        try {
            Method m = Packet.class.getDeclaredMethod("a", int.class, boolean.class, boolean.class, Class.class);
            m.setAccessible(true);
            m.invoke(null, 53+0xf00, true, true, XmasPacket53.class); // will never be sent over the wire, so it doesn't matter.
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public XmasPacket53(MiracleConnection con, Packet53BlockChange packet) {
        try {
            Field id = Packet.class.getDeclaredField("packetID");
            id.setAccessible(true);
            id.set(this, 51);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        this.lowPriority = true;
        this.a = packet.a;
        this.b = packet.b;
        this.c = packet.c;
        short blockdata = con.getNewBlock(a, b, c, this.material, this.data) ;
        this.material = (blockdata>>4) & 0xFFF;
        this.data = blockdata&0xF;
    }
}
