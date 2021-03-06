/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import buildcraft.api.statements.*;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.StringUtilBC;

public class TriggerFluidContainerLevel extends BCStatement implements ITriggerExternal {

    public enum TriggerType {

        BELOW25(0.25F),
        BELOW50(0.5F),
        BELOW75(0.75F);

        public static final TriggerType[] VALUES = values();

        public final float level;

        TriggerType(float level) {
            this.level = level;
        }
    }

    public final TriggerType type;

    public TriggerFluidContainerLevel(TriggerType type) {
        super("buildcraft:fluid." + type.name().toLowerCase(Locale.ROOT), "buildcraft.fluid." + type.name().toLowerCase(Locale.ROOT));
        this.type = type;
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.TRIGGER_FLUID_LEVEL.get(type);
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public String getDescription() {
        return String.format(StringUtilBC.localize("gate.trigger.fluidlevel.below"), (int) (type.level * 100));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
        if (handler == null) {
            return false;
        }
        FluidStack searchedFluid = null;

        if (parameters != null && parameters.length >= 1 && parameters[0] != null && parameters[0].getItemStack() != null) {
            searchedFluid = FluidUtil.getFluidContained(parameters[0].getItemStack());
            if (searchedFluid != null) {
                searchedFluid.amount = 1;
            }
        }

        IFluidTankProperties[] liquids = handler.getTankProperties();
        if (liquids == null || liquids.length == 0) {
            return false;
        }

        for (IFluidTankProperties c : liquids) {
            if (c == null) {
                continue;
            }
            FluidStack fluid = c.getContents();
            if (fluid == null) {
                if (searchedFluid == null) {
                    return true;
                }
                return handler.fill(searchedFluid, false) > 0;
            }

            if (searchedFluid == null || searchedFluid.isFluidEqual(fluid)) {
                float percentage = fluid.amount / (float) c.getCapacity();
                return percentage < type.level;
            }
        }
        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_FLUID_ALL;
    }
}
