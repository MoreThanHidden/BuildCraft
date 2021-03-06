package buildcraft.robotics.zone;

import javax.annotation.Nullable;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ZonePlannerMapChunk {
    private final MapColourData[][] data = new MapColourData[16][16];

    public ZonePlannerMapChunk(World world, ZonePlannerMapChunkKey key) {
        Chunk chunk = world.getChunkFromChunkCoords(key.chunkPos.chunkXPos, key.chunkPos.chunkZPos);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Scan down from the max height value of a chunk until we find a block
                for (int y = chunk.getHeightValue(x, z); y > 0; y--) {
                    int colour = chunk.getBlockState(x, y, z).getMapColor().colorValue;
                    if (colour != 0) {
                        data[x][z] = new MapColourData(y, colour);
                        break;
                    }
                }
            }
        }
    }

    public ZonePlannerMapChunk(PacketBuffer buffer) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int posY = buffer.readInt();
                if (posY > 0) {
                    int colour = buffer.readInt();
                    data[x][z] = new MapColourData(posY, colour);
                }
            }
        }
    }

    public void write(PacketBuffer buffer) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                MapColourData colour = data[x][z];
                if (colour == null) {
                    buffer.writeInt(-1);
                } else {
                    buffer.writeInt(colour.posY);
                    buffer.writeInt(colour.colour);
                }
            }
        }
    }

    public int getColour(int x, int z) {
        MapColourData col = getData(x, z);
        return col == null ? -1 : col.colour;
    }

    @Nullable
    public MapColourData getData(int x, int z) {
        return data[x & 15][z & 15];
    }

    public static final class MapColourData {
        public final int posY;
        public final int colour;

        public MapColourData(int posY, int colour) {
            this.posY = posY;
            this.colour = colour;
        }
    }
}
