package me.wolfyscript.customcrafting.gui.recipe_creator;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.TestCache;
import me.wolfyscript.customcrafting.gui.ExtendedGuiWindow;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.conditions.EliteWorkbenchConditionButton;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.conditions.WorldBiomeConditionButton;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.conditions.WorldNameConditionButton;
import me.wolfyscript.customcrafting.recipes.Conditions;
import me.wolfyscript.customcrafting.recipes.conditions.ExperienceCondition;
import me.wolfyscript.customcrafting.recipes.conditions.PermissionCondition;
import me.wolfyscript.customcrafting.recipes.conditions.WeatherCondition;
import me.wolfyscript.customcrafting.recipes.conditions.WorldTimeCondition;
import me.wolfyscript.customcrafting.recipes.types.ICustomRecipe;
import me.wolfyscript.utilities.api.inventory.GuiHandler;
import me.wolfyscript.utilities.api.inventory.GuiUpdateEvent;
import me.wolfyscript.utilities.api.inventory.InventoryAPI;
import me.wolfyscript.utilities.api.inventory.button.ButtonActionRender;
import me.wolfyscript.utilities.api.inventory.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.button.buttons.ActionButton;
import me.wolfyscript.utilities.api.utils.inventory.PlayerHeadUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConditionsMenu extends ExtendedGuiWindow {

    public ConditionsMenu(InventoryAPI inventoryAPI, CustomCrafting customCrafting) {
        super("conditions", inventoryAPI, 45, customCrafting);
    }

    @Override
    public void onInit() {
        registerButton(new ActionButton("back", new ButtonState("none", "back", PlayerHeadUtils.getViaValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0="), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            guiHandler.openPreviousInv();
            return true;
        })));

        registerButton(new ActionButton("conditions.world_time", new ButtonState("world_time", Material.CLOCK, new ButtonActionRender() {
            @Override
            public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int slot, InventoryClickEvent event) {
                Conditions conditions = ((TestCache) guiHandler.getCustomCache()).getRecipe().getConditions();
                if (event.getClick().isRightClick()) {
                    //Change Mode
                    conditions.getByID("world_time").toggleOption();
                } else {
                    //Change Value
                    openChat("world_time", guiHandler, (guiHandler1, player1, s, strings) -> {
                        try {
                            long value = Long.parseLong(s);
                            ((WorldTimeCondition) conditions.getByID("world_time")).setTime(value);
                            ((TestCache) guiHandler.getCustomCache()).getRecipe().setConditions(conditions);
                        } catch (NumberFormatException ex) {
                            api.sendPlayerMessage(player1, "recipe_creator", "valid_number");
                        }
                        return false;
                    });
                }
                ((TestCache) guiHandler.getCustomCache()).getRecipe().setConditions(conditions);
                return true;
            }

            @Override
            public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int slot, boolean b) {
                ICustomRecipe recipeConfig = ((TestCache) guiHandler.getCustomCache()).getRecipe();
                hashMap.put("%VALUE%", ((WorldTimeCondition) recipeConfig.getConditions().getByID("world_time")).getTime());
                hashMap.put("%MODE%", recipeConfig.getConditions().getByID("world_time").getOption().getDisplayString(api));
                return itemStack;
            }
        })));

        registerButton(new ActionButton("conditions.player_experience", new ButtonState("player_experience", Material.EXPERIENCE_BOTTLE, new ButtonActionRender() {
            @Override
            public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int slot, InventoryClickEvent event) {
                Conditions conditions = ((TestCache) guiHandler.getCustomCache()).getRecipe().getConditions();
                if (event.getClick().isRightClick()) {
                    //Change Mode
                    conditions.getByID("player_experience").toggleOption();
                } else {
                    //Change Value
                    openChat("player_experience", guiHandler, (guiHandler1, player1, s, strings) -> {
                        try {
                            int value = Integer.parseInt(s);
                            ((ExperienceCondition) conditions.getByID("player_experience")).setExpLevel(value);
                            ((TestCache) guiHandler.getCustomCache()).getRecipe().setConditions(conditions);
                        } catch (NumberFormatException ex) {
                            api.sendPlayerMessage(player1, "recipe_creator", "valid_number");
                        }
                        return false;
                    });
                }
                ((TestCache) guiHandler.getCustomCache()).getRecipe().setConditions(conditions);
                return true;
            }

            @Override
            public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int slot, boolean b) {
                ICustomRecipe recipeConfig = ((TestCache) guiHandler.getCustomCache()).getRecipe();
                hashMap.put("%VALUE%", ((ExperienceCondition) recipeConfig.getConditions().getByID("player_experience")).getExpLevel());
                hashMap.put("%MODE%", recipeConfig.getConditions().getByID("player_experience").getOption().getDisplayString(api));
                return itemStack;
            }
        })));

        registerButton(new ActionButton("conditions.weather", new ButtonState("weather", Material.WATER_BUCKET, new ButtonActionRender() {
            @Override
            public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int slot, InventoryClickEvent event) {
                ICustomRecipe recipeConfig = ((TestCache) guiHandler.getCustomCache()).getRecipe();
                Conditions conditions = recipeConfig.getConditions();
                if (event.getClick().isRightClick()) {
                    //Change Mode
                    conditions.getByID("weather").toggleOption();
                } else {
                    //Change Value
                    ((WeatherCondition) conditions.getByID("weather")).toggleWeather();
                }
                recipeConfig.setConditions(conditions);
                return true;
            }

            @Override
            public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int slot, boolean b) {
                ICustomRecipe recipeConfig = ((TestCache) guiHandler.getCustomCache()).getRecipe();
                hashMap.put("%VALUE%", ((WeatherCondition) recipeConfig.getConditions().getByID("weather")).getWeather().getDisplay(api));
                hashMap.put("%MODE%", recipeConfig.getConditions().getByID("weather").getOption().getDisplayString(api));
                return itemStack;
            }
        })));

        registerButton(new ActionButton("conditions.advanced_workbench", new ButtonState("advanced_workbench", Material.CRAFTING_TABLE, new ButtonActionRender() {
            @Override
            public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int slot, InventoryClickEvent event) {
                if (event.getClick().isLeftClick()) {
                    //Change Mode
                    Conditions conditions = ((TestCache) guiHandler.getCustomCache()).getRecipe().getConditions();
                    conditions.getByID("advanced_workbench").toggleOption();
                    ((TestCache) guiHandler.getCustomCache()).getRecipe().setConditions(conditions);
                }
                return true;
            }

            @Override
            public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int slot, boolean b) {
                hashMap.put("%MODE%", ((TestCache) guiHandler.getCustomCache()).getRecipe().getConditions().getByID("advanced_workbench").getOption().getDisplayString(api));
                return itemStack;
            }
        })));

        registerButton(new EliteWorkbenchConditionButton());
        registerButton(new WorldBiomeConditionButton());
        registerButton(new WorldNameConditionButton());

        registerButton(new ActionButton("conditions.permission", new ButtonState("permission", Material.REDSTONE, new ButtonActionRender() {
            @Override
            public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int slot, InventoryClickEvent event) {
                ICustomRecipe recipeConfig = ((TestCache) guiHandler.getCustomCache()).getRecipe();
                Conditions conditions = recipeConfig.getConditions();
                if (event.getClick().isRightClick()) {
                    //Change Mode
                    conditions.getByID("permission").toggleOption();
                    recipeConfig.setConditions(conditions);
                } else {
                    //SET Custom Permission String
                    openChat("permission", guiHandler, (guiHandler1, player1, s, strings) -> {
                        ((PermissionCondition) conditions.getByID("permission")).setPermission(s.trim());
                        recipeConfig.setConditions(conditions);
                        return false;
                    });
                }
                return true;
            }

            @Override
            public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int slot, boolean b) {
                ICustomRecipe recipeConfig = ((TestCache) guiHandler.getCustomCache()).getRecipe();
                hashMap.put("%VALUE%", ((PermissionCondition) recipeConfig.getConditions().getByID("permission")).getPermission());
                hashMap.put("%MODE%", recipeConfig.getConditions().getByID("permission").getOption().getDisplayString(api));
                return itemStack;
            }
        })));

    }

    @EventHandler
    public void onUpdate(GuiUpdateEvent event) {
        if (event.verify(this)) {
            TestCache cache = (TestCache) event.getGuiHandler().getCustomCache();
            event.setButton(0, "back");

            List<String> values = new ArrayList<>();
            values.add("conditions.world_time");
            values.add("conditions.world_name");
            values.add("conditions.world_biome");
            values.add("conditions.weather");
            switch (cache.getRecipeType()) {
                case WORKBENCH:
                    values.add("conditions.permission");
                    values.add("conditions.player_experience");
                    values.add("conditions.advanced_workbench");
                    break;
                case ELITE_WORKBENCH:
                    values.add("conditions.permission");
                    values.add("conditions.player_experience");
                    values.add("conditions.elite_workbench");
                    break;
                case BREWING_STAND:
                case GRINDSTONE:
                    values.add("conditions.permission");
                    values.add("conditions.player_experience");
            }
            int item = 9;
            for (int i = 0; i < values.size() && item < 45; i++) {
                event.setButton(item++, values.get(i));
            }
        }
    }
}
