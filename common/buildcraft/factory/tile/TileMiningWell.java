package buildcraft.factory.tile;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import buildcraft.api.mj.IMjReceiver;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.mj.MjBatteryReciver;

public class TileMiningWell extends TileMiner {
    @Override
    protected void mine() {
        IBlockState state = worldObj.getBlockState(currentPos);
        if (BlockUtil.isUnbreakableBlock(getWorld(), currentPos) || state.getBlock() == Blocks.BEDROCK) {
            setComplete(true);
            return;
        }

        long target = BlockUtil.computeBlockBreakPower(worldObj, currentPos);
        progress += battery.extractPower(0, target - progress);

        if (progress >= target) {
            progress = 0;
            if (!worldObj.isAirBlock(currentPos)) {
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(worldObj, currentPos, state, FakePlayerUtil.INSTANCE.getBuildCraftPlayer((WorldServer) worldObj).get());
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if (breakEvent.isCanceled()) {
                    setComplete(true);
                    return;
                }
                List<ItemStack> stacks = BlockUtil.getItemStackFromBlock((WorldServer) worldObj, currentPos, pos);
                if (stacks != null) {
                    for (ItemStack stack : stacks) {
                        InventoryUtil.addToBestAcceptor(getWorld(), getPos(), null, stack);
                    }
                }
                worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                worldObj.destroyBlock(currentPos, false);
            }
            currentPos = currentPos.down();
            if (currentPos.getY() < 0) {
                setComplete(true);
            } else {
                goToYLevel(currentPos.getY());
            }
        } else {
            if (!worldObj.isAirBlock(currentPos)) {
                worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
            }
        }
    }

    // @Override
    // public double getTubeOffset() {
    // return -0.8;
    // }

    @Override
    public void onRemove() {
        super.onRemove();
        worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReciver(battery);
    }
}
