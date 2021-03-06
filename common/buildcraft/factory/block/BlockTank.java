package buildcraft.factory.block;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.api.transport.ICustomPipeConnection;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

public class BlockTank extends BlockBCTile_Neptune implements ICustomPipeConnection {
    private static final BuildCraftProperty<Boolean> JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(2.0 / 16.0, 0.0 / 16.0, 2.0 / 16.0, 14.0 / 16.0, 16.0 / 16.0, 14.0 / 16.0);

    public BlockTank(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileTank();
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(JOINED_BELOW);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (side.getAxis() == EnumFacing.Axis.Y) {
            return !(world.getBlockState(pos.offset(side)).getBlock() instanceof BlockTank);
        } else {
            return super.shouldSideBeRendered(blockState, world, pos, side);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(JOINED_BELOW, world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock() instanceof BlockTank);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileTank tile = (TileTank) world.getTileEntity(pos);
        if (heldItem == null || tile == null) {
            return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
        }
        if (FluidUtil.interactWithFluidHandler(heldItem, tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null), player)) {
            tile.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
            player.inventoryContainer.detectAndSendChanges();
            return true;
        }
        return false;
    }

    @Override
    public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
        return face.getAxis() == Axis.Y ? 0 : 2 / 16f;
    }
}
