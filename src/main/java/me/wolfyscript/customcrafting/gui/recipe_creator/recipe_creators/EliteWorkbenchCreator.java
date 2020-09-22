package me.wolfyscript.customcrafting.gui.recipe_creator.recipe_creators;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.TestCache;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.CraftingIngredientButton;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.ExactMetaButton;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.PriorityButton;
import me.wolfyscript.customcrafting.recipes.types.elite_workbench.EliteCraftingRecipe;
import me.wolfyscript.customcrafting.recipes.types.elite_workbench.ShapedEliteCraftRecipe;
import me.wolfyscript.customcrafting.recipes.types.elite_workbench.ShapelessEliteCraftRecipe;
import me.wolfyscript.utilities.api.inventory.GuiUpdate;
import me.wolfyscript.utilities.api.inventory.InventoryAPI;
import me.wolfyscript.utilities.api.inventory.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.button.buttons.ToggleButton;
import me.wolfyscript.utilities.api.utils.inventory.InventoryUtils;
import me.wolfyscript.utilities.api.utils.inventory.PlayerHeadUtils;

public class EliteWorkbenchCreator extends RecipeCreator {

    public EliteWorkbenchCreator(InventoryAPI inventoryAPI, CustomCrafting customCrafting) {
        super("elite_workbench", inventoryAPI, 54, customCrafting);
    }

    @Override
    public void onInit() {
        super.onInit();

        registerButton(new ExactMetaButton());
        registerButton(new PriorityButton());

        for (int i = 0; i < 37; i++) {
            registerButton(new CraftingIngredientButton(i, customCrafting));
        }

        registerButton(new ToggleButton("workbench.shapeless", false, new ButtonState("recipe_creator", "workbench.shapeless.enabled", PlayerHeadUtils.getViaURL("f21d93da43863cb3759afefa9f7cc5c81f34d920ca97b7283b462f8b197f813"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((TestCache) guiHandler.getCustomCache()).setCustomRecipe(new ShapedEliteCraftRecipe(((TestCache) guiHandler.getCustomCache()).getEliteCraftingRecipe()));
            return true;
        }), new ButtonState("recipe_creator", "workbench.shapeless.disabled", PlayerHeadUtils.getViaURL("1aae7e8222ddbee19d184b97e79067814b6ba3142a3bdcce8b93099a312"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((TestCache) guiHandler.getCustomCache()).setCustomRecipe(new ShapelessEliteCraftRecipe(((TestCache) guiHandler.getCustomCache()).getEliteCraftingRecipe()));
            return true;
        })));

        registerButton(new ToggleButton("workbench.mirrorHorizontal", false, new ButtonState("recipe_creator", "workbench.mirrorHorizontal.enabled", PlayerHeadUtils.getViaURL("956a3618459e43b287b22b7e235ec699594546c6fcd6dc84bfca4cf30ab9311"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((ShapedEliteCraftRecipe) ((TestCache) guiHandler.getCustomCache()).getEliteCraftingRecipe()).setMirrorHorizontal(false);
            return true;
        }), new ButtonState("recipe_creator", "workbench.mirrorHorizontal.disabled", PlayerHeadUtils.getViaURL("956a3618459e43b287b22b7e235ec699594546c6fcd6dc84bfca4cf30ab9311"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((ShapedEliteCraftRecipe) ((TestCache) guiHandler.getCustomCache()).getEliteCraftingRecipe()).setMirrorHorizontal(true);
            return true;
        })));
        registerButton(new ToggleButton("workbench.mirrorVertical", false, new ButtonState("recipe_creator", "workbench.mirrorVertical.enabled", PlayerHeadUtils.getViaURL("882faf9a584c4d676d730b23f8942bb997fa3dad46d4f65e288c39eb471ce7"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((ShapedEliteCraftRecipe) ((TestCache) guiHandler.getCustomCache()).getEliteCraftingRecipe()).setMirrorVertical(false);
            return true;
        }), new ButtonState("recipe_creator", "workbench.mirrorVertical.disabled", PlayerHeadUtils.getViaURL("882faf9a584c4d676d730b23f8942bb997fa3dad46d4f65e288c39eb471ce7"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((ShapedEliteCraftRecipe) ((TestCache) guiHandler.getCustomCache()).getEliteCraftingRecipe()).setMirrorVertical(true);
            return true;
        })));
        registerButton(new ToggleButton("workbench.mirrorRotation", false, new ButtonState("recipe_creator", "workbench.mirrorRotation.enabled", PlayerHeadUtils.getViaURL("e887cc388c8dcfcf1ba8aa5c3c102dce9cf7b1b63e786b34d4f1c3796d3e9d61"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((ShapedEliteCraftRecipe) ((TestCache) guiHandler.getCustomCache()).getEliteCraftingRecipe()).setMirrorRotation(false);
            return true;
        }), new ButtonState("recipe_creator", "workbench.mirrorRotation.disabled", PlayerHeadUtils.getViaURL("e887cc388c8dcfcf1ba8aa5c3c102dce9cf7b1b63e786b34d4f1c3796d3e9d61"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((ShapedEliteCraftRecipe) ((TestCache) guiHandler.getCustomCache()).getEliteCraftingRecipe()).setMirrorRotation(true);
            return true;
        })));
    }

    @Override
    public void onUpdateAsync(GuiUpdate update) {
        update.setButton(6, "back");
        TestCache cache = (TestCache) update.getGuiHandler().getCustomCache();
        EliteCraftingRecipe workbench = cache.getEliteCraftingRecipe();

        ((ToggleButton) update.getGuiWindow().getButton("workbench.shapeless")).setState(update.getGuiHandler(), workbench.isShapeless());
        ((ToggleButton) update.getGuiWindow().getButton("exact_meta")).setState(update.getGuiHandler(), workbench.isExactMeta());
        ((ToggleButton) update.getGuiWindow().getButton("hidden")).setState(update.getGuiHandler(), workbench.isHidden());

        if (!workbench.isShapeless()) {
            ((ToggleButton) update.getGuiWindow().getButton("workbench.mirrorHorizontal")).setState(update.getGuiHandler(), ((ShapedEliteCraftRecipe) workbench).mirrorHorizontal());
            ((ToggleButton) update.getGuiWindow().getButton("workbench.mirrorVertical")).setState(update.getGuiHandler(), ((ShapedEliteCraftRecipe) workbench).mirrorVertical());
            ((ToggleButton) update.getGuiWindow().getButton("workbench.mirrorRotation")).setState(update.getGuiHandler(), ((ShapedEliteCraftRecipe) workbench).mirrorRotation());

            if (((ShapedEliteCraftRecipe) workbench).mirrorHorizontal() && ((ShapedEliteCraftRecipe) workbench).mirrorVertical()) {
                update.setButton(33, "workbench.mirrorRotation");
            }
            update.setButton(34, "workbench.mirrorHorizontal");
            update.setButton(35, "workbench.mirrorVertical");
        }

        int slot;
        for (int i = 0; i < 36; i++) {
            slot = i + (i / 6) * 3;
            update.setButton(slot, "crafting.container_" + i);
        }
        update.setButton(25, "crafting.container_36");
        update.setButton(24, "workbench.shapeless");

        update.setButton(42, "hidden");
        update.setButton(43, "recipe_creator", "conditions");
        update.setButton(44, "exact_meta");
        update.setButton(51, "priority");

        if(workbench.hasNamespacedKey()){
            update.setButton(52, "save");
        }
        update.setButton(53, "save_as");
    }

    @Override
    public boolean validToSave(TestCache cache) {
        EliteCraftingRecipe workbench = cache.getEliteCraftingRecipe();
        return workbench.getIngredients() != null && !InventoryUtils.isCustomItemsListEmpty(workbench.getResults());
    }
}
