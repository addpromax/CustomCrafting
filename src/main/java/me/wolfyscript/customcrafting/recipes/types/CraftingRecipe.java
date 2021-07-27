package me.wolfyscript.customcrafting.recipes.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.gui.recipebook.buttons.IngredientContainerButton;
import me.wolfyscript.customcrafting.recipes.Condition;
import me.wolfyscript.customcrafting.recipes.Conditions;
import me.wolfyscript.customcrafting.recipes.RecipePriority;
import me.wolfyscript.customcrafting.recipes.Types;
import me.wolfyscript.customcrafting.recipes.types.crafting.CraftingRecipeSettings;
import me.wolfyscript.customcrafting.utils.ItemLoader;
import me.wolfyscript.customcrafting.utils.recipe_item.Ingredient;
import me.wolfyscript.customcrafting.utils.recipe_item.Result;
import me.wolfyscript.customcrafting.utils.recipe_item.target.SlotResultTarget;
import me.wolfyscript.utilities.api.inventory.custom_items.CustomItem;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiHandler;
import me.wolfyscript.utilities.api.inventory.gui.GuiUpdate;
import me.wolfyscript.utilities.api.inventory.gui.GuiWindow;
import me.wolfyscript.utilities.api.nms.network.MCByteBuf;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.core.JsonGenerator;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.JsonNode;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.SerializerProvider;
import me.wolfyscript.utilities.util.NamespacedKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CraftingRecipe<C extends CraftingRecipe<C, S>, S extends CraftingRecipeSettings> extends CustomRecipe<C, SlotResultTarget> implements ICraftingRecipe {

    protected static final String INGREDIENTS_KEY = "ingredients";

    private Map<Character, Ingredient> ingredients;
    protected List<Ingredient> ingredientsFlat;

    protected int requiredGridSize;
    protected int bookSquaredGrid;

    private S settings;

    protected CraftingRecipe(NamespacedKey namespacedKey, JsonNode node, int gridSize) {
        super(namespacedKey, node);
        this.requiredGridSize = gridSize;
        this.bookSquaredGrid = requiredGridSize * requiredGridSize;
        setIngredients(Streams.stream(node.path(INGREDIENTS_KEY).fields()).collect(Collectors.toMap(entry -> entry.getKey().charAt(0), entry -> ItemLoader.loadIngredient(entry.getValue()))));
    }

    protected CraftingRecipe(int gridSize) {
        super();
        this.requiredGridSize = gridSize;
        this.bookSquaredGrid = requiredGridSize * requiredGridSize;
        this.ingredients = new HashMap<>();
    }

    protected CraftingRecipe(NamespacedKey namespacedKey, boolean exactMeta, boolean hidden, String group, RecipePriority priority, Conditions conditions, Result<SlotResultTarget> result, Map<Character, Ingredient> ingredients, int requiredGridSize, S settings) {
        super(namespacedKey, exactMeta, hidden, group, priority, conditions, result);
        this.ingredients = ingredients;
        this.requiredGridSize = requiredGridSize;
        this.bookSquaredGrid = requiredGridSize * requiredGridSize;
        this.settings = settings;
    }

    protected CraftingRecipe(CraftingRecipe<?, S> craftingRecipe) {
        super(craftingRecipe);
        this.ingredientsFlat = craftingRecipe.ingredientsFlat != null ? craftingRecipe.ingredientsFlat.stream().map(Ingredient::clone).collect(Collectors.toList()) : null;
        this.requiredGridSize = craftingRecipe.requiredGridSize;
        this.bookSquaredGrid = craftingRecipe.bookSquaredGrid;
        this.ingredients = craftingRecipe.getIngredients();
    }

    @Override
    public Map<Character, Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public Ingredient getIngredient(int slot) {
        return getIngredients(LETTERS.charAt(slot));
    }

    public S getSettings() {
        return settings;
    }

    public void setSettings(S settings) {
        this.settings = settings;
    }

    public List<Ingredient> getFlatIngredients() {
        return ingredientsFlat;
    }

    @Override
    public void setIngredients(Map<Character, Ingredient> ingredients) {
        this.ingredients = ingredients.entrySet().stream().filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, o2) -> o));
        Preconditions.checkArgument(!this.ingredients.isEmpty(), "Invalid ingredients! Recipe must have non-air ingredients!");
    }

    @Override
    public void setIngredient(char key, Ingredient ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            this.ingredients.remove(key);
        } else {
            ingredients.buildChoices();
            this.ingredients.put(key, ingredients);
        }
    }

    @Override
    public void setIngredient(int slot, Ingredient ingredients) {
        setIngredient(LETTERS.charAt(slot), ingredients);
    }

    @Override
    public void prepareMenu(GuiHandler<CCCache> guiHandler, GuiCluster<CCCache> cluster) {
        if (!getIngredients().isEmpty()) {
            ((IngredientContainerButton) cluster.getButton("ingredient.container_" + bookSquaredGrid)).setVariants(guiHandler, this.getResult());
            for (int i = 0; i < bookSquaredGrid; i++) {
                var ingredient = getIngredient(i);
                if (ingredient != null) {
                    ((IngredientContainerButton) cluster.getButton("ingredient.container_" + i)).setVariants(guiHandler, ingredient);
                }
            }
        }
    }

    @Override
    public void renderMenu(GuiWindow<CCCache> guiWindow, GuiUpdate<CCCache> event) {
        if (!getIngredients().isEmpty()) {
            if (Types.WORKBENCH.isInstance(this) && getConditions().getByID("advanced_workbench").getOption().equals(Conditions.Option.EXACT)) {
                var glass = new NamespacedKey("none", "glass_purple");
                for (int i = 0; i < 9; i++) {
                    event.setButton(i, glass);
                }
                for (int i = 36; i < 54; i++) {
                    event.setButton(i, glass);
                }
            }
            List<Condition> conditions = getConditions().values().stream().filter(condition -> !condition.getOption().equals(Conditions.Option.IGNORE) && !condition.getId().equals("advanced_workbench") && !condition.getId().equals("permission")).collect(Collectors.toList());
            int startSlot = 9 / (conditions.size() + 1);
            int slot = 0;
            for (Condition condition : conditions) {
                if (!condition.getOption().equals(Conditions.Option.IGNORE)) {
                    event.setButton(36 + startSlot + slot, new NamespacedKey("recipe_book", "conditions." + condition.getId()));
                    slot += 2;
                }
            }
            boolean elite = Types.ELITE_WORKBENCH.isInstance(this);
            event.setButton(elite ? 24 : 23, new NamespacedKey("recipe_book", isShapeless() ? "workbench.shapeless_on" : "workbench.shapeless_off"));
            startSlot = elite ? 0 : 10;
            for (int i = 0; i < bookSquaredGrid; i++) {
                event.setButton(startSlot + i + (i / requiredGridSize) * (9 - requiredGridSize), new NamespacedKey("recipe_book", "ingredient.container_" + i));
            }
            event.setButton(25, new NamespacedKey("recipe_book", "ingredient.container_" + bookSquaredGrid));
        }
    }

    @Override
    public void writeToJson(JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        super.writeToJson(gen, serializerProvider);
        gen.writeBooleanField("shapeless", isShapeless());
        gen.writeObjectField(KEY_RESULT, result);
        gen.writeObjectField(INGREDIENTS_KEY, ingredients);
    }

    @Override
    public void writeToBuf(MCByteBuf byteBuf) {
        super.writeToBuf(byteBuf);
        byteBuf.writeBoolean(isShapeless());
        byteBuf.writeInt(requiredGridSize);
        byteBuf.writeVarInt(ingredients.size());
        ingredients.forEach((key, ingredient) -> {
            byteBuf.writeInt(LETTERS.indexOf(key));
            byteBuf.writeVarInt(ingredient.size());
            for (CustomItem choice : ingredient.getChoices()) {
                byteBuf.writeItemStack(choice.create());
            }
        });
    }

}
