package buildcraft.transport;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.api.transport.neptune.PipeBehaviour;
import buildcraft.api.transport.neptune.PipePluggable;

import buildcraft.transport.client.model.GateMeshDefinition;
import buildcraft.transport.client.render.RenderPipeHolder;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.container.ContainerFilteredBuffer;
import buildcraft.transport.container.ContainerGate;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiDiamondWoodPipe;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.gui.GuiGate;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.plug.PluggableGate;
import buildcraft.transport.tile.TileFilteredBuffer;
import buildcraft.transport.tile.TilePipeHolder;

public abstract class BCTransportProxy implements IGuiHandler {
    @SidedProxy
    private static BCTransportProxy proxy;

    public static BCTransportProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id == BCTransportGuis.FILTERED_BUFFER.ordinal()) {
            if (tile instanceof TileFilteredBuffer) {
                TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                return new ContainerFilteredBuffer(player, filteredBuffer);
            }
        } else if (id == BCTransportGuis.PIPE_DIAMOND.ordinal()) {
            if (tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                IPipe pipe = holder.getPipe();
                if (pipe == null) return null;
                PipeBehaviour behaviour = pipe.getBehaviour();
                if (behaviour instanceof PipeBehaviourDiamond) {
                    PipeBehaviourDiamond diaPipe = (PipeBehaviourDiamond) behaviour;
                    return new ContainerDiamondPipe(player, diaPipe);
                }
            }
        } else if (id == BCTransportGuis.PIPE_DIAMOND_WOOD.ordinal()) {
            if (tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                IPipe pipe = holder.getPipe();
                if (pipe == null) return null;
                PipeBehaviour behaviour = pipe.getBehaviour();
                if (behaviour instanceof PipeBehaviourWoodDiamond) {
                    PipeBehaviourWoodDiamond diaPipe = (PipeBehaviourWoodDiamond) behaviour;
                    return new ContainerDiamondWoodPipe(player, diaPipe);
                }
            }
        } else if (id == BCTransportGuis.GATE.ordinal()) {
            int ry = y >> 3;
            EnumFacing gateSide = EnumFacing.getFront(y & 0x7);
            tile = world.getTileEntity(new BlockPos(x, ry, z));
            if (tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                PipePluggable plug = holder.getPluggable(gateSide);
                if (plug instanceof PluggableGate) {
                    return new ContainerGate(player, ((PluggableGate) plug).logic);
                }
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {}

    public void fmlInit() {}

    public void fmlPostInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCTransportProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCTransportProxy {
        @Override
        public void fmlPreInit() {
            BCTransportSprites.fmlPreInit();
            BCTransportModels.fmlPreInit();
        }

        @Override
        public void fmlInit() {
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(BCTransportItems.plugGate, GateMeshDefinition.INSTANCE);
            ClientRegistry.bindTileEntitySpecialRenderer(TilePipeHolder.class, new RenderPipeHolder());
        }

        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (id == BCTransportGuis.FILTERED_BUFFER.ordinal()) {
                if (tile instanceof TileFilteredBuffer) {
                    TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                    return new GuiFilteredBuffer(new ContainerFilteredBuffer(player, filteredBuffer));
                }
            } else if (id == BCTransportGuis.PIPE_DIAMOND.ordinal()) {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourDiamond) {
                        PipeBehaviourDiamond diaPipe = (PipeBehaviourDiamond) behaviour;
                        return new GuiDiamondPipe(player, diaPipe);
                    }
                }
            } else if (id == BCTransportGuis.PIPE_DIAMOND_WOOD.ordinal()) {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourWoodDiamond) {
                        PipeBehaviourWoodDiamond diaPipe = (PipeBehaviourWoodDiamond) behaviour;
                        return new GuiDiamondWoodPipe(player, diaPipe);
                    }
                }
            } else if (id == BCTransportGuis.GATE.ordinal()) {
                int ry = y >> 3;
                EnumFacing gateSide = EnumFacing.getFront(y & 0x7);
                tile = world.getTileEntity(new BlockPos(x, ry, z));
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    PipePluggable plug = holder.getPluggable(gateSide);
                    if (plug instanceof PluggableGate) {
                        return new GuiGate(new ContainerGate(player, ((PluggableGate) plug).logic));
                    }
                }
            }
            return null;
        }
    }
}
