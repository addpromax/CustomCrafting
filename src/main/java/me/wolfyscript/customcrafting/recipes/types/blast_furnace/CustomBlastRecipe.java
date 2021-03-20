package me.wolfyscript.customcrafting.recipes.types.blast_furnace;

import me.wolfyscript.customcrafting.recipes.RecipeType;
import me.wolfyscript.customcrafting.recipes.Types;
import me.wolfyscript.customcrafting.recipes.types.CustomCookingRecipe;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.JsonNode;
import me.wolfyscript.utilities.util.NamespacedKey;
import org.bukkit.inventory.BlastingRecipe;

public class CustomBlastRecipe extends CustomCookingRecipe<CustomBlastRecipe, BlastingRecipe> {

    public CustomBlastRecipe(NamespacedKey namespacedKey, JsonNode node) {
        super(namespacedKey, node);
    }

    public CustomBlastRecipe() {
        super();
    }

    public CustomBlastRecipe(CustomBlastRecipe customBlastRecipe){
        super(customBlastRecipe);
    }

    @Override
    public BlastingRecipe getVanillaRecipe() {
        if (!getSource().isEmpty()) {
            return new BlastingRecipe(getNamespacedKey().toBukkit(), getResult().getItemStack(), getRecipeChoice(), getExp(), getCookingTime());
        }
        return null;
    }

    @Override
    public RecipeType<CustomBlastRecipe> getRecipeType() {
        return Types.BLAST_FURNACE;
    }

    @Override
    public CustomBlastRecipe clone() {
        return new CustomBlastRecipe(this);
    }
}
