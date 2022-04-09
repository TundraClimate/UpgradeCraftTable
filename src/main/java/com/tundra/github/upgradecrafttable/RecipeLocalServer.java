package com.tundra.github.upgradecrafttable;

import com.tundra.finelib.FineLib;
import com.tundra.finelib.builder.ItemStackBuilder;
import com.tundra.finelib.database.sqlite.SQLite;
import com.tundra.finelib.util.Base64Connector;
import com.tundra.github.upgradecrafttable.object.RecipeObject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RecipeLocalServer {

    @Nullable
    public static ItemStack queryRecipeResult(ItemStack... stacks) {
        ItemStack barrier = new ItemStackBuilder(Material.BARRIER)
                .setDisplayName("§cNot Found")
                .addContainerData("cancel", PersistentDataType.STRING, "cancel")
                .toItemStack();

        if (stacks.length > 9 || stacks.length == 0) return null;
        ItemStack[] slot = new ItemStack[9];
        List<ItemStack> stackList = new ArrayList<>(List.of(stacks));
        while (stackList.size() <= 9) {
            stackList.add(new ItemStack(Material.AIR));
        }
        for (int i = 0; i < 9; i++) {
            slot[i] = stackList.get(i) != null ? stackList.get(i) : new ItemStack(Material.AIR);
        }
        SQLite db = UpgradeCraftTable.getSqlite();
        try {
            String shape = Base64Connector.encode(RecipeObject.RecipeType.SHAPED);
            PreparedStatement ps = db.prepareStatement("SELECT result, recipe, ingredients FROM temp WHERE type = ?");
            ps.setString(1, shape);
            ItemStack result = null;
            char[] recipe = new char[9];
            ItemStack[] stackRecipe = new ItemStack[9];
            Map<Character, ItemStack> ingredients = getIng(ps);
            ingredients.put(' ', new ItemStack(Material.AIR));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<String> l = new ArrayList<>();
                char[][] r = new char[3][3];
                if (Base64Connector.decode(rs.getString("recipe")) instanceof List ls) {
                    ((List<?>) ls).forEach(o -> {
                        if (!(o instanceof String))
                            throw new NoSuchElementException("Found Error Recipe");
                        l.add((String) o);
                    });
                }
                for (int i = 0; i < l.size(); i++) {
                    r[i] = l.get(i).toCharArray();
                }
                int temp = 0;
                for (char[] chars : r) {
                    for (char aChar : chars) {
                        recipe[temp] = aChar;
                        temp++;
                    }
                }
                stackRecipe = new ItemStack[recipe.length];
                for (int i = 0; i < stackRecipe.length; i++) {
                    if (ingredients.containsKey(recipe[i])){
                        stackRecipe[i] = ingredients.get(recipe[i]);
                        result = (ItemStack) Base64Connector.decode(rs.getString("result"));
                    }
                }
            }
            rs.close();
            for (int i = 0; i < slot.length; i++) {
                if (slot[i].getType() != stackRecipe[i].getType())
                    return barrier;
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return barrier;
    }

    private static Map<Character, ItemStack> getIng(PreparedStatement ps) {
        Map<Character, ItemStack> ingredients = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (Base64Connector.decode(rs.getString("ingredients")) instanceof Map ing) {
                    ((Map<?, ?>) ing).forEach((o, ob) -> {
                        if (!(o instanceof Character))
                            throw new NoSuchElementException("Found Error to Ingredients key in RecipeFile");
                        if (!(ob instanceof ItemStack))
                            throw new NoSuchElementException("Found Error to Ingredients value in RecipeFile");
                        ingredients.put((Character) o, (ItemStack) ob);
                    });
                }
            }
            return ingredients;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
