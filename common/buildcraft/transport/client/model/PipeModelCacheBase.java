package buildcraft.transport.client.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.EnumDyeColor;

import buildcraft.api.transport.neptune.PipeDefinition;

import buildcraft.lib.client.model.IModelCache;
import buildcraft.lib.client.model.ModelCache;
import buildcraft.transport.client.model.key.PipeModelKey;

public class PipeModelCacheBase {
    public static IPipeBaseModelGen generator = PipeBaseModelGenStandard.INSTANCE;

    static final IModelCache<PipeBaseCutoutKey> cacheCutout;
    static final IModelCache<PipeBaseTransclucentKey> cacheTranslucent;

    static {
        cacheCutout = new ModelCache<>(PipeModelCacheBase::generateCutout);
        cacheTranslucent = new ModelCache<>(PipeModelCacheBase::generateTranslucent);
    }

    private static List<BakedQuad> generateCutout(PipeBaseCutoutKey key) {
        return generator.generateCutout(key);
    }

    private static List<BakedQuad> generateTranslucent(PipeBaseTransclucentKey key) {
        return generator.generateTranslucent(key);
    }

    public static final class PipeBaseCutoutKey {
        public final PipeDefinition definition;
        public final int centerSprite;
        public final int[] sideSprites;
        public final float[] connections;
        private final int hashCode;

        public PipeBaseCutoutKey(PipeModelKey key) {
            definition = key.definition;
            centerSprite = key.center;
            sideSprites = key.sides;
            connections = key.connected;
            hashCode = Objects.hash(System.identityHashCode(definition), centerSprite, Arrays.hashCode(sideSprites), Arrays.hashCode(connections));
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            PipeBaseCutoutKey other = (PipeBaseCutoutKey) obj;
            if (definition != other.definition) return false;
            if (centerSprite != other.centerSprite) return false;
            if (!Arrays.equals(connections, other.connections)) return false;
            if (!Arrays.equals(sideSprites, other.sideSprites)) return false;
            return true;
        }

        @Override
        public String toString() {
            return "PipeBaseCutoutKey [center=" + centerSprite + ", sides=" + Arrays.toString(sideSprites) + ", connections=" + Arrays.toString(connections) + "]";
        }
    }

    public static final class PipeBaseTransclucentKey {
        public final EnumDyeColor colour;
        public final float[] connections;
        private final int hashCode;

        public PipeBaseTransclucentKey(PipeModelKey key) {
            this.colour = key.colour;
            if (colour == null) {
                connections = null;
                hashCode = 0;
            } else {
                connections = key.connected;
                hashCode = Objects.hash(colour, Arrays.hashCode(connections));
            }
        }

        public boolean shouldRender() {
            return colour != null;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof PipeBaseTransclucentKey)) return false;
            PipeBaseTransclucentKey other = (PipeBaseTransclucentKey) obj;
            /* If we don't have any translucency and neither does the other then we don't care what the other variables
             * are and are considered equal to the other one. */
            if (!shouldRender() && !other.shouldRender()) return true;
            if (!Arrays.equals(connections, other.connections)) return false;
            return colour == other.colour;
        }

        @Override
        public String toString() {
            return "PipeBaseTransclucentKey [colour=" + colour + ", connections=" + Arrays.toString(connections) + "]";
        }
    }
}
