package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.factory.tile.TileHeatExchange;

public class GuiHeatExchange extends GuiAdvancedInterface {

    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftfactory:textures/gui/heat_exchanger.png");

    private final TileHeatExchange heatExchange;
    private int inCoolableTicks, inHeatableTicks, outCooledTicks, outHeatedTicks, craftTicks;

    public GuiHeatExchange(EntityPlayer player, TileHeatExchange heatExchange) {
        super(new ContainerHeatExchange(player, heatExchange), heatExchange, TEXTURE);
        this.heatExchange = heatExchange;
        xSize = 176;
        ySize = 171;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        inCoolableTicks--;
        inHeatableTicks--;
        outCooledTicks--;
        outHeatedTicks--;
        craftTicks--;
        boolean crafted = heatExchange.hasCraftedRecently();
        if (crafted) craftTicks = 20;
        if ((heatExchange.getInputCoolable() != null && heatExchange.getInputCoolable().amount > 0) || crafted) inCoolableTicks = 20;
        if ((heatExchange.getInputHeatable() != null && heatExchange.getInputHeatable().amount > 0) || crafted) inHeatableTicks = 20;

        if ((heatExchange.getOutputCooled() != null && heatExchange.getOutputCooled().amount > 0) || crafted) outCooledTicks = 20;
        if ((heatExchange.getOutputHeated() != null && heatExchange.getOutputHeated().amount > 0) || crafted) outHeatedTicks = 20;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (heatExchange != null) {

            drawFluid(heatExchange.getInputCoolable(), guiLeft + 44, guiTop + 12, 16, 38, 500);
            drawFluid(heatExchange.getInputHeatable(), guiLeft + 44, guiTop + 64, 34, 17, 500);
            drawFluid(heatExchange.getOutputCooled(), guiLeft + 116, guiTop + 43, 16, 38, 500);
            drawFluid(heatExchange.getOutputHeated(), guiLeft + 98, guiTop + 12, 34, 17, 500);

            mc.renderEngine.bindTexture(TEXTURE);

            if (craftTicks > 0) {
                drawTexturedModalRect(guiLeft + 61, guiTop + 11, 176, 71, 54, 71);
            } else {
                if (inCoolableTicks > 0) {
                    drawTexturedModalRect(guiLeft + 79, guiTop + 67, 194, 56, 11, 11);
                }
                if (inHeatableTicks > 0) {
                    drawTexturedModalRect(guiLeft + 61, guiTop + 41, 176, 30, 11, 11);
                }
                if (outCooledTicks > 0) {
                    drawTexturedModalRect(guiLeft + 104, guiTop + 41, 219, 30, 11, 11);
                }
                if (outHeatedTicks > 0) {
                    drawTexturedModalRect(guiLeft + 86, guiTop + 15, 201, 4, 11, 11);
                }
            }
        }
    }
}