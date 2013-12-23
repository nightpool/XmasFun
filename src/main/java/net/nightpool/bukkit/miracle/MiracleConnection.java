package net.nightpool.bukkit.miracle;

import java.util.HashSet;
import java.util.Set;

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
    public XmasMiraclePlugin pl;
    public static byte[] buildBuffer = new byte[196864];
    public boolean firstPacket = true;
    static public Set<Integer> transparents = new HashSet<Integer>();
    public final static short red = (short) Material.REDSTONE_BLOCK.getId();
    public final static short green = (short) Material.EMERALD_BLOCK.getId();
    static {
        for (Material i : Material.values()) {
            if (i.isTransparent()) {
                if(i.equals(Material.LAVA)){continue;}
                transparents.add(i.getId());
            }
        }
    }

    public MiracleConnection(XmasMiraclePlugin p, MinecraftServer minecraftserver, INetworkManager networkmanager,
            EntityPlayer player) {
        super(minecraftserver, networkmanager, player);
        this.pl = p;
    }

    @Override
    public void sendPacket(Packet packet) {
        if (packet instanceof Packet51MapChunk) {
//            pl.getLogger().info("51: Map Chunk");
//            packet = new XmasPacket51(this, (Packet51MapChunk) packet);
        } else if (packet instanceof Packet56MapChunkBulk) {
//            pl.getLogger().info("56: Map Chunk Bulk");
//            packet = new XmasPacket56(this, (Packet56MapChunkBulk) packet);
        } else if (packet instanceof Packet52MultiBlockChange){
//            pl.getLogger().info("52: Multi Block Change");
            packet = new XmasPacket52(this, (Packet52MultiBlockChange) packet);
        } else if (packet instanceof Packet53BlockChange){
//            pl.getLogger().info("53: Block Change");
//            packet = new XmasPacket53(this, (Packet53BlockChange) packet);
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
        return transparents.contains(blockType);
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
                    if(transparents.contains(type))
                        continue;
//                    blockData[y << 8 | z << 4 | x] = 1; // Set it to smooth stone
                }
            }
        }
        return blockData;
    }

    public short getNewBlock(int x, int y, int z, int type, int data) {
        if(transparents.contains(type))
            return (short) ((type & 0xFFF) << 4 | data & 0xF);;
        return (short)( ( ((y%1==0)? red:green) & 0xFFF) << 4 | data & 0xF);
    }
}
