package buildcraft.lib.list;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import buildcraft.api.lists.ListMatchHandler;

import buildcraft.lib.misc.StackUtil;

public class ListMatchHandlerFluid extends ListMatchHandler {
    @Override
    public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            if (FluidContainerRegistry.isContainer(stack) && FluidContainerRegistry.isContainer(target)) {
                ItemStack emptyContainerStack = FluidContainerRegistry.drainFluidContainer(stack);
                ItemStack emptyContainerTarget = FluidContainerRegistry.drainFluidContainer(target);
                if (StackUtil.isMatchingItem(emptyContainerStack, emptyContainerTarget, true, true)) {
                    return true;
                }
            }
        } else if (type == Type.MATERIAL) {
            FluidStack fStack = FluidUtil.getFluidContained(stack);
            FluidStack fTarget = FluidUtil.getFluidContained(target);
            if (fStack != null && fTarget != null) {
                return fStack.isFluidEqual(fTarget);
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, ItemStack stack) {
        if (type == Type.TYPE) {
            return FluidContainerRegistry.isContainer(stack);
        } else if (type == Type.MATERIAL) {
            return FluidUtil.getFluidContained(stack) != null;
        }
        return false;
    }

    @Override
    public List<ItemStack> getClientExamples(Type type, ItemStack stack) {
        if (type == Type.MATERIAL) {
            FluidStack fStack = FluidUtil.getFluidContained(stack);
            if (fStack != null) {
                List<ItemStack> examples = new ArrayList<>();
                for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
                    if (fStack.isFluidEqual(data.fluid)) {
                        examples.add(data.filledContainer);
                    }
                }
                return examples;
            }
        } else if (type == Type.TYPE) {
            if (FluidContainerRegistry.isContainer(stack)) {
                List<ItemStack> examples = new ArrayList<>();
                ItemStack emptyContainerStack = FluidContainerRegistry.drainFluidContainer(stack);
                examples.add(stack);
                examples.add(emptyContainerStack);
                for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
                    if (StackUtil.isMatchingItem(data.emptyContainer, emptyContainerStack, true, true)) {
                        examples.add(data.filledContainer);
                    }
                }
                return examples;
            }
        }
        return null;
    }
}
