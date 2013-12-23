package net.nightpool.bukkit.miracle;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.zip.Deflater;

import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;

public class XmasPacket51 extends Packet51MapChunk {

    public int size;
    public byte[] buffer;
    public MiracleConnection con;
    public byte[] inflatedBuffer;
    
    static{
        try {
            Method m = Packet.class.getDeclaredMethod("a", int.class, boolean.class, boolean.class, Class.class);
            m.setAccessible(true);
            m.invoke(null, 51+0xf00, true, true, XmasPacket51.class); // will never be sent over the wire, so it doesn't matter.
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public XmasPacket51(MiracleConnection con, Packet51MapChunk packet) {
        try {
            Field id = Packet.class.getDeclaredField("packetID");
            id.setAccessible(true);
            id.set(this, 51);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        this.lowPriority = true;
        Chunk chunk = con.player.world.getChunkAt(packet.a, packet.b);
        this.a = packet.a;
        this.b = packet.b;
        this.e = packet.e;

        ChunkMap localChunkMap = con.djChunkMap(chunk, e, 0xffff);
        Deflater localDeflater = new Deflater(-1);
        this.d = localChunkMap.c;
        this.c = localChunkMap.b;
        try {
            this.inflatedBuffer = localChunkMap.a;
            localDeflater.setInput(localChunkMap.a, 0, localChunkMap.a.length);
            localDeflater.finish();
            this.buffer = new byte[localChunkMap.a.length];
            this.size = localDeflater.deflate(this.buffer);
        } finally {
            localDeflater.end();
        }
    }

    @Override
    public void a(DataOutput paramDataOutput) {
        try {
            paramDataOutput.writeInt(this.a);
            paramDataOutput.writeInt(this.b);
            paramDataOutput.writeBoolean(this.e);
            paramDataOutput.writeShort((short) (this.c & 0xFFFF));
            paramDataOutput.writeShort((short) (this.d & 0xFFFF));

            paramDataOutput.writeInt(this.size);
            paramDataOutput.write(this.buffer, 0, this.size);

        } catch (IOException e1) {
            this.con.pl.getLogger().severe("IOError in packet 51 for player " + con.player.getName());
        }
    }

    @Override
    public int a() {
        return (17 + this.size);
    }
}
