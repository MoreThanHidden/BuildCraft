package buildcraft.lib.nbt;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.data.NBTSquishConstants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NbtSquisher {
    /* Defines a compression program that can turn large, mostly-similar, dense, NBTTagCompounds into much smaller
     * variants.
     * 
     * Compression has the following steps:
     * 
     * - 1: */

    public static byte[] squish(NBTTagCompound nbt, int type) {
        switch (type) {
            case NBTSquishConstants.VANILLA:
                return squishVanillaUncompressed(nbt);
            case NBTSquishConstants.VANILLA_COMPRESSED:
                return squishVanilla(nbt);
            case NBTSquishConstants.BUILDCRAFT_V1:
                return squishBuildCraftV1Uncompressed(nbt);
            case NBTSquishConstants.BUILDCRAFT_V1_COMPRESSED:
                return squishBuildCraftV1(nbt);
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    public static byte[] squishVanilla(NBTTagCompound nbt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(NBTSquishConstants.VANILLA_COMPRESSED);
        DataOutputStream output = new DataOutputStream(baos);
        try {
            CompressedStreamTools.writeCompressed(nbt, output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write a safe NBTTagCompound!");
        }
        return baos.toByteArray();
    }

    public static byte[] squishVanillaUncompressed(NBTTagCompound nbt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(NBTSquishConstants.VANILLA);
        DataOutputStream output = new DataOutputStream(baos);
        try {
            CompressedStreamTools.write(nbt, output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write a safe NBTTagCompound!");
        }
        return baos.toByteArray();
    }

    public static byte[] squishBuildCraftV1(NBTTagCompound nbt) {
        byte[] bytes = squishBuildCraftV1_Internal(nbt, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(NBTSquishConstants.BUILDCRAFT_V1_COMPRESSED);
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress a perfectly good ByteArrayOutputStream!", e);
        }
        return baos.toByteArray();
    }

    public static byte[] squishBuildCraftV1Uncompressed(NBTTagCompound nbt) {
        return squishBuildCraftV1_Internal(nbt, true);
    }

    private static byte[] squishBuildCraftV1_Internal(NBTTagCompound nbt, boolean writeId) {
        NBTSquishMap map = new NBTSquishMap();
        map.addTag(nbt);
        ByteBuf buf = Unpooled.buffer();
        if (NBTSquishDebugging.debug) {
            buf = new PrintingByteBuf(buf);
        }

        if (writeId) {
            buf.writeByte(NBTSquishConstants.BUILDCRAFT_V1);
        }

        NBTSquishMapWriter.write(map, buf);
        WrittenType type = map.getWrittenType();
        type.writeIndex(buf, map.indexOfTag(nbt));
        NBTSquishDebugging.log("\nUsed type " + type + " (as there are " + map.size() + " object types)");
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public static NBTTagCompound expand(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        int nbtWrittenType = bais.read();
        if (nbtWrittenType == NBTSquishConstants.BUILDCRAFT_V1 || nbtWrittenType == NBTSquishConstants.BUILDCRAFT_V1_COMPRESSED) {

            ByteBuf buf;
            if (nbtWrittenType == NBTSquishConstants.BUILDCRAFT_V1_COMPRESSED) {
                try (GZIPInputStream gzip = new GZIPInputStream(bais)) {
                    buf = Unpooled.wrappedBuffer(IOUtils.toByteArray(gzip));
                }
            } else {
                buf = Unpooled.wrappedBuffer(bytes);
                buf.readByte();
            }

            try {
                NBTSquishMap map = NBTSquishMapReader.read(buf);
                WrittenType type = map.getWrittenType();
                int index = type.readIndex(buf);
                NBTBase nbt = map.getTagForReading(index);
                return (NBTTagCompound) nbt;
            } catch (IndexOutOfBoundsException ioobe) {
                throw new IOException("The byte buf was not big enough!", ioobe);
            }
        }
        if (nbtWrittenType == NBTSquishConstants.VANILLA) {

            return CompressedStreamTools.read(new DataInputStream(bais));
        }
        if (nbtWrittenType == NBTSquishConstants.VANILLA_COMPRESSED) {

            return CompressedStreamTools.readCompressed(new DataInputStream(bais));
        } else {
            throw new IOException("Unknown NBT storage type " + nbtWrittenType);
        }
    }
}
