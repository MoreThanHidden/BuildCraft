package buildcraft.lib.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.pos.IPositionedElement;

@SideOnly(Side.CLIENT)
public interface IGuiElement extends IPositionedElement, ITooltipElement {
    default void drawBackground(float partialTicks) {}

    default void drawForeground(float partialTicks) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseClicked(int button) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseDragged(int button, long ticksSinceClick) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseReleased(int button) {}
}
