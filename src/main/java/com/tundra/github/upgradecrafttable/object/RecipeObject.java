package com.tundra.github.upgradecrafttable.object;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class RecipeObject {
    public RecipeObject(ItemStack result, RecipeType type, List<String> recipe, Map<Character, ItemStack> ingredients) {
        if (recipe.size() > 3) throw new IllegalArgumentException("recipe size is Illegal");
        this.result = result;
        this.type = type;
        this.recipe = recipe;
        this.ingredients = ingredients;
    }

    private final ItemStack result;
    private final RecipeType type;
    private final List<String> recipe;
    private final Map<Character, ItemStack> ingredients;

    public ItemStack getResult() {
        return result;
    }

    public RecipeType getType() {
        return type;
    }

    public List<String> getRecipe() {
        return recipe;
    }

    public Map<Character, ItemStack> getIngredients() {
        return ingredients;
    }

    public enum RecipeType {
        SHAPED,
        SHAPELESS
    }
}
