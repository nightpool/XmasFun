package net.nightpool.bukkit.miracle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
//import net.minecraft.server.Chunk;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet52MultiBlockChange;

public class XmasPacket52 extends Packet52MultiBlockChange {

    public MiracleConnection con;
    
    static{
        try {
            Method m = Packet.class.getDeclaredMethod("a", int.class, boolean.class, boolean.class, Class.class);
            m.setAccessible(true);
            m.invoke(null, 52+0xf00, true, true, XmasPacket52.class); // will never be sent over the wire, so it doesn't matter.
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public XmasPacket52(MiracleConnection con, Packet52MultiBlockChange packet) {
        try {
            Field id = Packet.class.getDeclaredField("packetID");
            id.setAccessible(true);
            id.set(this, 52);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        this.lowPriority = true;
//        Chunk chunk = con.player.world.getChunkAt(packet.a, packet.b);
        this.a = packet.a; // Chunk X
        this.b = packet.b; // Chunk Z
        this.d = packet.d; // Blocks Affected (Record Count)

        this.c = new byte[packet.c.length];
        int len = packet.d*4;
        short loc, type_data;
        byte x,y,z;
        for(int i = 0; i<c.length; i+=4){
            loc = (short) ((packet.c[i]&0xFF)<<8 | (packet.c[i+1]&0xFF));
            x = (byte) (loc >> 12 & 0xF);
            z = (byte) (loc >> 8 & 0xF);
            y = packet.c[i+1];
            type_data = (short) ((packet.c[i+2]&0xFF)<<8 | (packet.c[i+3]&0xFF));
            type_data = con.getNewBlock(x+a, y, z+b, (type_data>>4) & 0xFFF, type_data&0xF);
            c[i]= packet.c[i];
            c[i+1] = y;
            c[i+2] = (byte)(type_data >> 8 & 0xff);
            c[i+3] = (byte)((type_data) & 0xff);
        }
        if (c.length != len)
            throw new RuntimeException("Expected length " + len + " doesn't match received length "
                    + this.c.length);
    }
}
