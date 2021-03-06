package buildcraft.transport.gui;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.misc.data.IReference;
import buildcraft.transport.gate.ActionWrapper;

public class ElementAction extends ElementStatement<ActionWrapper> {

    public ElementAction(GuiGate gui, IPositionedElement element, IReference<ActionWrapper> reference) {
        super(gui, element, reference);
    }

    @Override
    protected ActionWrapper[] getPossible() {
        ActionWrapper value = reference.get();
        if (value == null) return null;
        ActionWrapper[] possible = value.getPossible();
        if (possible == null) return null;

        List<ActionWrapper> list = new ArrayList<>();
        list.add(null);
        for (ActionWrapper poss : possible) {
            if (gui.container.possibleActions.contains(poss)) {
                list.add(poss);
            }
        }
        return list.toArray(new ActionWrapper[list.size()]);
    }
}
