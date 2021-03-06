/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.statements.containers.ISidedStatementContainer;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.StringUtilBC;

public class TriggerRedstoneInput extends BCStatement implements ITriggerInternal {

    boolean active;

    public TriggerRedstoneInput(boolean active) {
        super("buildcraft:redstone.input." + (active ? "active" : "inactive"),//
                "buildcraft.redtone.input." + (active ? "active" : "inactive"));
        this.active = active;
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return active ? BCCoreSprites.TRIGGER_REDSTONE_ACTIVE : BCCoreSprites.TRIGGER_REDSTONE_INACTIVE;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.localize("gate.trigger.redstone.input." + (active ? "active" : "inactive"));
    }

    @Override
    public IStatementParameter createParameter(int index) {
        IStatementParameter param = null;

        if (index == 0) {
            param = new StatementParamGateSideOnly();
        }

        return param;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if (container instanceof IRedstoneStatementContainer) {
            int level = ((IRedstoneStatementContainer) container).getRedstoneInput(null);
            if (parameters.length > 0 && parameters[0] instanceof StatementParamGateSideOnly && ((StatementParamGateSideOnly) parameters[0]).isOn && container instanceof ISidedStatementContainer) {
                level = ((IRedstoneStatementContainer) container).getRedstoneInput(((ISidedStatementContainer) container).getSide());
            }

            return active ? level > 0 : level == 0;
        } else {
            return false;
        }
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_REDSTONE;
    }
}
