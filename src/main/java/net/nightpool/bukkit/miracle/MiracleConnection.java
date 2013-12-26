package net.nightpool.bukkit.miracle;


import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NibbleArray;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet52MultiBlockChange;
import net.minecraft.server.Packet53BlockChange;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.World;
import org.bukkit.Material;

@SuppressWarnings("deprecation")
public class MiracleConnection extends PlayerConnection {
//    private final int updateRadius = 2;
    enum Algo {
        STRIPED_X("striped-x"),
        STRIPED_Z("striped-z"),
        STRIPED_Y("striped-y"),
        TRUE_ALT("true");
        
        public final String name;
        Algo(String s){
            this.name = s;
        }
        public static Algo getByString(String string) {
            try{
                return valueOf(string);
            } catch(IllegalArgumentException e){
                
            }
            Algo a = null;
            for(Algo i:values()){
                if(i.name.equals(string)){
                    a = i;
                }
            }
            return a;
        }
    }
    public XmasMiraclePlugin pl;
    public static byte[] buildBuffer = new byte[196864];
    public boolean firstPacket = true;
    public final static short red = (short) Material.REDSTONE_BLOCK.getId();
    public final static short green = (short) Material.EMERALD_BLOCK.getId();
    public Algo algo;

    public MiracleConnection(XmasMiraclePlugin p, MinecraftServer minecraftserver, INetworkManager networkmanager,
            EntityPlayer player) {
        super(minecraftserver, networkmanager, player);
        this.pl = p;
        this.algo = p.default_algo;
        if(p.algo_pref.containsKey(player.getName())){
            this.algo = p.algo_pref.get(player.getName());
        }
    }

    @Override
    public void sendPacket(Packet packet) {
        if (packet instanceof Packet51MapChunk) {
            packet = new XmasPacket51(this, (Packet51MapChunk) packet);
        } else if (packet instanceof Packet56MapChunkBulk) {
            packet = new XmasPacket56(this, (Packet56MapChunkBulk) packet);
        } else if (packet instanceof Packet52MultiBlockChange){
            packet = new XmasPacket52(this, (Packet52MultiBlockChange) packet);
        } else if (packet instanceof Packet53BlockChange){
            packet = new XmasPacket53(this, (Packet53BlockChange) packet);
        }
        super.sendPacket(packet);
    }

    public ChunkMap djChunkMap(Chunk chunk, boolean flag, int paramInt) {
        int i = 0;
        ChunkSection[] arrayOfChunkSection = chunk.i();
        int j = 0;
        ChunkMap localChunkMap = new ChunkMap();
        byte[] arrayOfByte1 = buildBuffer;

        if (flag) {
            chunk.seenByPlayer = true;
        }
        int k;
        for (k = 0; k < arrayOfChunkSection.length; ++k)
            if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                    && ((paramInt & 1 << k) != 0)) {
                localChunkMap.b |= 1 << k;
                if (arrayOfChunkSection[k].getExtendedIdArray() != null) {
                    localChunkMap.c |= 1 << k;
                    ++j;
                }
            }
        byte[] byteObject;
        NibbleArray nibObject;
        for (k = 0; k < arrayOfChunkSection.length; ++k) {
            if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                    && ((paramInt & 1 << k) != 0)) {
                byteObject = replaceCoveredBlocks(chunk, arrayOfChunkSection[k]);
                System.arraycopy(byteObject, 0, arrayOfByte1, i, byteObject.length);
                i += byteObject.length;
            }
        }
        for (k = 0; k < arrayOfChunkSection.length; ++k) {
            if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                    && ((paramInt & 1 << k) != 0)) {
                nibObject = arrayOfChunkSection[k].getDataArray();
                System.arraycopy(nibObject.a, 0, arrayOfByte1, i, nibObject.a.length);
                i += nibObject.a.length;
            }
        }
        for (k = 0; k < arrayOfChunkSection.length; ++k) {
            if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                    && ((paramInt & 1 << k) != 0)) {
                nibObject = arrayOfChunkSection[k].getEmittedLightArray();
                System.arraycopy(nibObject.a, 0, arrayOfByte1, i, nibObject.a.length);
                i += nibObject.a.length;
            }
        }
        if (!(chunk.world.worldProvider.g)) {
            for (k = 0; k < arrayOfChunkSection.length; ++k) {
                if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                        && ((paramInt & 1 << k) != 0)) {
                    nibObject = arrayOfChunkSection[k].getSkyLightArray();
                    System.arraycopy(nibObject.a, 0, arrayOfByte1, i, nibObject.a.length);
                    i += nibObject.a.length;
                }
            }
        }

        if (j > 0) {
            for (k = 0; k < arrayOfChunkSection.length; ++k) {
                if ((arrayOfChunkSection[k] != null) && (((!(flag)) || (!(arrayOfChunkSection[k].isEmpty()))))
                        && (arrayOfChunkSection[k].getExtendedIdArray() != null) && ((paramInt & 1 << k) != 0)) {
                    nibObject = arrayOfChunkSection[k].getExtendedIdArray();
                    System.arraycopy(nibObject.a, 0, arrayOfByte1, i, nibObject.a.length);
                    i += nibObject.a.length;
                }
            }
        }

        if (flag) {
            byte[] arrayOfByte2 = chunk.m();
            System.arraycopy(arrayOfByte2, 0, arrayOfByte1, i, arrayOfByte2.length);
            i += arrayOfByte2.length;
        }

        localChunkMap.a = new byte[i];
        System.arraycopy(arrayOfByte1, 0, localChunkMap.a, 0, i);

        return ((ChunkMap) localChunkMap);
    }

    public boolean isBlockTransparent(World world, int x, int y, int z) {
        int blockType = world.getTypeId(x, y, z);
        return XmasMiraclePlugin.exceptions.contains(blockType);
    }

    public byte[] replaceCoveredBlocks(Chunk chunk, ChunkSection section) {

        /*******
         * WARNING WARNING WARNING DO NOT FORGET TO CLONE THE BLOCK DATA FOR
         * THIS SECTION OTHERWISE YOU WILL OVERWRITE WORLD DATA WHEN SETTING TO
         * STONE WARNING WARNING WARNING
         *********/
        byte[] blockData = section.getIdArray().clone(); // Get the block data for this section
        // logF("blockData-before ", blockData);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {

                    int worldX = (chunk.x << 4) + x;
                    int worldY = section.getYPosition() + y;
                    int worldZ = (chunk.z << 4) + z;

                    int type = section.getTypeId(x, y, z);
                    if (chunk.world.getTypeId(worldX, worldY, worldZ) != type) {
                        pl.getLogger().warning("Block type mismatch " + chunk.world.getTypeId(worldX, worldY, worldZ)
                                + " vs " + type);
                    }
                    if(XmasMiraclePlugin.exceptions.contains(type))
                        continue;
                    blockData[y << 8 | z << 4 | x] = (byte) ((getNewBlock(worldX, worldY, worldZ, type, 0)>>4)&0xFFF); // Set it to smooth stone
                }
            }
        }
        return blockData;
    }

    public short getNewBlock(int x, int y, int z, int type, int data) {
        if(XmasMiraclePlugin.exceptions.contains(type))
            return (short) ((type & 0xFFF) << 4 | data & 0xF);
        boolean t = false;
        switch(this.algo){
            case STRIPED_X:
                t = (x%2==0) == (y%2==0); break;
            case STRIPED_Z:
                t = (z%2==0) == (y%2==0); break;
            case STRIPED_Y:
                t = (x%2==0) == (z%2==0); break;
            case TRUE_ALT:
                t = ((y%2==0) != ((z%2==0) == (x%2==0))); break;
//            case PINHOLES:
//                t = ((x%2==0) == (y%2==0)) && (z%2==0) == (y%2==0); break; 
        }
        return (short)(((t? red : green) & 0xFFF) << 4 | data & 0xF);
    }
}
