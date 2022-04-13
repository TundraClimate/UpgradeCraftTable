package com.tundra.github.upgradecrafttable.object;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class RecipeObject {
    public RecipeObject(String result, RecipeType type, List<String> recipe, Map<Character, String> ingredients) {
        if (recipe.size() > 3) throw new IllegalArgumentException("recipe size is Illegal");
        this.result = result;
        this.type = type;
        this.recipe = recipe;
        this.ingredients = ingredients;
    }

    private final String  result;
    private final RecipeType type;
    private final List<String> recipe;
    private final Map<Character, String> ingredients;

    public String  getResult() {
        return result;
    }

    public RecipeType getType() {
        return type;
    }

    public List<String> getRecipe() {
        return recipe;
    }

    public Map<Character, String> getIngredients() {
        return ingredients;
    }

    public enum RecipeType {
        SHAPED,
        SHAPELESS
    }
}
