package buildcraft.lib.client.guide.parts;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.GuiGuide;

public class GuideCraftingFactory implements GuidePartFactory {
    private static final Field SHAPED_ORE_RECIPE___WIDTH;
    private static final Field SHAPED_ORE_RECIPE___HEIGHT;

    private final ItemStack[][] input;
    private final ItemStack output;
    private final int hash;

    public GuideCraftingFactory(ItemStack[][] input, ItemStack output) {
        this.input = input;
        this.output = output;
        NBTTagCompound hashNbt = new NBTTagCompound();
        hashNbt.setTag("output", output.serializeNBT());
        if (input != null) {
            for (int i = 0; i < input.length; i++) {
                if (input[i] != null) {
                    for (int j = 0; j < input[i].length; j++) {
                        if (input[i][j] != null) {
                            hashNbt.setTag("in[" + i + "," + j + "]", input[i][j].serializeNBT());
                        }
                    }
                }
            }
        }
        this.hash = hashNbt.hashCode();
    }

    static {
        try {
            SHAPED_ORE_RECIPE___WIDTH = ShapedOreRecipe.class.getDeclaredField("width");
            SHAPED_ORE_RECIPE___WIDTH.setAccessible(true);

            SHAPED_ORE_RECIPE___HEIGHT = ShapedOreRecipe.class.getDeclaredField("height");
            SHAPED_ORE_RECIPE___HEIGHT.setAccessible(true);
        } catch (Throwable t) {
            throw new RuntimeException("Could not find the width field!", t);
        }
    }

    public static GuideCraftingFactory create(ItemStack stack) {
        for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
            if (OreDictionary.itemMatches(stack, recipe.getRecipeOutput(), false)) {
                GuideCraftingFactory val = getFactory(recipe);
                if (val != null) {
                    return val;
                } else {
                    BCLog.logger.warn("[lib.guide.crafting] Found a matching recipe, but of an unknown " + recipe.getClass() + " for " + stack.getDisplayName());
                }
            }
        }
        return null;
    }

    public static GuideCraftingFactory getFactory(IRecipe recipe) {
        GuideCraftingFactory val = null;
        if (recipe instanceof ShapedRecipes) {
            ShapedRecipes shaped = (ShapedRecipes) recipe;
            ItemStack[] input = shaped.recipeItems;
            ItemStack[][] dimInput = new ItemStack[shaped.recipeWidth][shaped.recipeHeight];
            for (int x = 0; x < dimInput.length; x++) {
                for (int y = 0; y < dimInput[x].length; y++) {
                    dimInput[x][y] = ItemStack.copyItemStack(input[x + y * dimInput.length]);
                }
            }
            val = new GuideCraftingFactory(dimInput, recipe.getRecipeOutput());
        } else if (recipe instanceof ShapedOreRecipe) {
            Object[] input = ((ShapedOreRecipe) recipe).getInput();
            ItemStack[][] dimInput = getStackSizeArray(recipe);
            for (int x = 0; x < dimInput.length; x++) {
                for (int y = 0; y < dimInput[x].length; y++) {
                    dimInput[x][y] = oreConvert(input[x + y * dimInput.length]);
                }
            }
            val = new GuideCraftingFactory(dimInput, recipe.getRecipeOutput());
        } else if (recipe instanceof ShapelessOreRecipe) {
            List<Object> input = ((ShapelessOreRecipe) recipe).getInput();
            ItemStack[][] dimInput = getStackSizeArray(recipe);
            for (int x = 0; x < dimInput.length; x++) {
                for (int y = 0; y < dimInput[x].length; y++) {
                    int index = x + y * dimInput.length;
                    if (index < input.size()) {
                        dimInput[x][y] = oreConvert(input.get(index));
                    }
                }
            }
            val = new GuideCraftingFactory(dimInput, recipe.getRecipeOutput());
        } else if (recipe instanceof ShapelessRecipes) {
            List<ItemStack> input = ((ShapelessRecipes) recipe).recipeItems;
            ItemStack[][] dimInput = getStackSizeArray(recipe);
            for (int x = 0; x < dimInput.length; x++) {
                for (int y = 0; y < dimInput[x].length; y++) {
                    int index = x + y * dimInput.length;
                    if (index < input.size()) {
                        dimInput[x][y] = ItemStack.copyItemStack(input.get(index));
                    }
                }
            }
            val = new GuideCraftingFactory(dimInput, recipe.getRecipeOutput());
        } else if (recipe instanceof IRecipeViewable) {
            
        } else {
            BCLog.logger.warn("[lib.guide.crafting] Found an unknown recipe " + recipe.getClass());
        }
        return val;
    }

    private static ItemStack[][] getStackSizeArray(IRecipe recipe) {
        if (recipe instanceof ShapedRecipes) {
            return new ItemStack[((ShapedRecipes) recipe).recipeWidth][((ShapedRecipes) recipe).recipeHeight];
        } else if (recipe instanceof ShapedOreRecipe) {
            // YAAAAY REFLECTION :(
            int width = 3;
            int height = 3;
            try {
                width = SHAPED_ORE_RECIPE___WIDTH.getInt(recipe);
                height = SHAPED_ORE_RECIPE___HEIGHT.getInt(recipe);
            } catch (Throwable t) {
                BCLog.logger.error("Could not access the required shaped ore recipe fields!", t);
            }
            return new ItemStack[width][height];
        }
        return new ItemStack[3][3];
    }

    private static ItemStack oreConvert(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof ItemStack) {
            return ((ItemStack) object).copy();
        }
        if (object instanceof String) {
            List<ItemStack> stacks = OreDictionary.getOres((String) object);
            // It will be sorted out below
            object = stacks;
        }
        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            if (list.isEmpty()) {
                return null;
            }
            Object first = list.get(0);
            if (first == null) {
                return null;
            }
            if (first instanceof ItemStack) {
                // Technically a safe cast as the first one WAS an Item Stack and we never add to the list
                @SuppressWarnings("unchecked")
                List<ItemStack> stacks = (List<ItemStack>) list;
                if (stacks.size() == 0) {
                    return null;
                }
                ItemStack best = stacks.get(0);
                for (ItemStack stack : stacks) {
                    // The lower the ID of an item, the closer it is to minecraft. Hmmm.
                    if (Item.getIdFromItem(stack.getItem()) < Item.getIdFromItem(best.getItem())) {
                        best = stack;
                    }
                }
                return best.copy();
            }
            BCLog.logger.warn("Found a list with unknown contents! " + first.getClass());
        }
        BCLog.logger.warn("Found an ore with an unknown " + object.getClass());
        return null;
    }

    public static GuideCraftingFactory create(Item output) {
        return create(new ItemStack(output));
    }

    @Override
    public GuideCrafting createNew(GuiGuide gui) {
        return new GuideCrafting(gui, input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GuideCraftingFactory other = (GuideCraftingFactory) obj;
        // Shortcut out of this full itemstack comparison as its really expensive
        if (hash != other.hash) return false;

        if (input.length != other.input.length) return false;
        for (int i = 0; i < input.length; i++) {
            ItemStack[] compA = input[i];
            ItemStack[] compB = other.input[i];
            if (compA.length != compB.length) return false;
            for (int j = 0; j < input.length; j++) {
                if (!ItemStack.areItemStacksEqual(compA[j], compB[j])) {
                    return false;
                }
            }
        }
        return ItemStack.areItemStacksEqual(output, other.output);
    }
}
