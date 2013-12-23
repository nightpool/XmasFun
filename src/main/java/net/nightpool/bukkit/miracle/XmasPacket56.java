package net.nightpool.bukkit.miracle;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet56MapChunkBulk;

public class XmasPacket56 extends Packet56MapChunkBulk {

    private int[] c;
    private int[] d;
    private byte[][] inflatedBuffers;
    private boolean h;
    private byte[] buildBuffer = new byte[0];
    private byte[] buffer;
    private int size;

    static final ThreadLocal<Deflater> localDeflater = new ThreadLocal<Deflater>() {
        @Override
        protected Deflater initialValue() {
            return new Deflater(6);
        }
    };
    static{
        try {
            Method m = Packet.class.getDeclaredMethod("a", int.class, boolean.class, boolean.class, Class.class);
            m.setAccessible(true);
            m.invoke(null, 56+0xf00, true, false, XmasPacket56.class); // will never be sent over the wire, so it doesn't matter.
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public XmasPacket56(MiracleConnection con, Packet56MapChunkBulk packet) {
        try {
            Field id = Packet.class.getDeclaredField("packetID");
            id.setAccessible(true);
            id.set(this, 56);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        List<Chunk> list = new ArrayList<Chunk>();
        try {
            Field x = Packet56MapChunkBulk.class.getDeclaredField("c");
            x.setAccessible(true);
            this.c = (int[]) x.get(packet);
            Field z = Packet56MapChunkBulk.class.getDeclaredField("d");
            z.setAccessible(true);
            this.d = (int[]) z.get(packet);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        for (int j = 0; j < c.length; j++) {
            Chunk ch = con.player.world.getChunkAt(c[j], d[j]);
            list.add(ch);
        }
        int i = list.size();
        this.a = packet.a;
        this.b = packet.b;
        this.inflatedBuffers = new byte[i][];
        this.h = ((!(list.isEmpty())) && (!(((Chunk) list.get(0)).world.worldProvider.g)));
        int j = 0;

        for (int k = 0; k < i; ++k) {
            Chunk chunk = list.get(k);
            ChunkMap chunkmap = con.djChunkMap(chunk, true, '\uffff');

            if (buildBuffer.length < j + chunkmap.a.length) {
                byte[] abyte = new byte[j + chunkmap.a.length];

                System.arraycopy(this.buildBuffer, 0, abyte, 0, this.buildBuffer.length);
                this.buildBuffer = abyte;
            }

            System.arraycopy(chunkmap.a, 0, this.buildBuffer, j, chunkmap.a.length);
            j += chunkmap.a.length;
            this.c[k] = chunk.x;
            this.d[k] = chunk.z;
            this.inflatedBuffers[k] = chunkmap.a;
        }
    }

    @Override
    public void compress() {
        if (this.buffer != null) {
            return;
        }

        Deflater deflater = (Deflater) localDeflater.get();
        deflater.reset();
        deflater.setInput(this.buildBuffer);
        deflater.finish();

        this.buffer = new byte[this.buildBuffer.length + 100];
        this.size = deflater.deflate(this.buffer);
    }

    @Override
    public void a(DataOutput dataoutput) throws IOException {
        compress();
        dataoutput.writeShort(this.c.length);
        dataoutput.writeInt(this.size);
        dataoutput.writeBoolean(this.h);
        dataoutput.write(this.buffer, 0, this.size);

        for (int i = 0; i < this.c.length; ++i) {
            dataoutput.writeInt(this.c[i]);
            dataoutput.writeInt(this.d[i]);
            dataoutput.writeShort((short) (this.a[i] & 0xFFFF));
            dataoutput.writeShort((short) (this.b[i] & 0xFFFF));
        }
    }

    @Override
    public int a() {
        return 6 + this.size + 12 * this.c.length;
    }
}
