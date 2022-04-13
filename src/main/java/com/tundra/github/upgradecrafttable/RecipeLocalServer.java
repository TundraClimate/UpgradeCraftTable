package com.tundra.github.upgradecrafttable;

import com.tundra.finelib.FineLib;
import com.tundra.finelib.builder.ItemStackBuilder;
import com.tundra.finelib.database.sqlite.SQLite;
import com.tundra.finelib.util.Base64Connector;
import com.tundra.github.upgradecrafttable.object.RecipeObject;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RecipeLocalServer {

    public static Map.Entry<Player, Map.Entry<List<ItemStack>, ItemStack>> queryRecipeResult(Player player, ItemStack... stacks) {
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
        Map.Entry<List<ItemStack>, ItemStack> entry = null;
        try {
            PreparedStatement ps = db.prepareStatement("SELECT result, recipe, ingredients FROM temp WHERE type = ?");
            entry = callShapedRecipe(ps, slot);
            if (entry.getValue().equals(barrier)) {
                entry = callShapelessRecipe(ps, slot);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Map.entry(player, Objects.requireNonNull(entry));
    }

    private static Map.Entry<List<ItemStack>, ItemStack> callShapedRecipe(PreparedStatement ps, ItemStack[] slot) throws SQLException {
        ps.setString(1, Base64Connector.encode(RecipeObject.RecipeType.SHAPED));
        ItemStack result = new ItemStackBuilder(Material.BARRIER)
                .setDisplayName("§cNot Found")
                .addContainerData("cancel", PersistentDataType.STRING, "cancel")
                .toItemStack();
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
                if (ingredients.containsKey(recipe[i])) {
                    stackRecipe[i] = ingredients.get(recipe[i]);
                }
            }
            if (equalsOfAmount(slot, stackRecipe))
                result = (ItemStack) Base64Connector.decode(rs.getString("result"));
        }
        rs.close();
        List<ItemStack> slotOf = new ArrayList<>();
        int slotA;
        int slotB;
        ItemStack s;
        for (int i = 0; i < 9; i++) {
            slotA = slot[i].getAmount();
            slotB = stackRecipe[i].getAmount();
            s = new ItemStack(slot[i]);
            s.setAmount(slotA - slotB);
            slotOf.add(s);
        }
        return Map.entry(slotOf, result);
    }

    private static Map.Entry<List<ItemStack>, ItemStack> callShapelessRecipe(PreparedStatement ps, ItemStack[] slot) throws SQLException {
        ps.setString(1, Base64Connector.encode(RecipeObject.RecipeType.SHAPELESS));
        ItemStack result = new ItemStackBuilder(Material.BARRIER)
                .setDisplayName("§cNot Found")
                .addContainerData("cancel", PersistentDataType.STRING, "cancel")
                .toItemStack();
        List<ItemStack> slotOf = new ArrayList<>();
        char[] recipe = new char[9];
        ItemStack[] stackRecipe;
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
                if (ingredients.containsKey(recipe[i])) {
                    stackRecipe[i] = ingredients.get(recipe[i]);
                }
            }
            List<ItemStack> stackList = new ArrayList<>(List.of(stackRecipe));
            List<ItemStack> slotList = new ArrayList<>(List.of(slot));
            for (int i = 0; i < 9; i++){
                for (ItemStack stack : stackList) {
                    if (slotList.get(i).isSimilar(stack) && slotList.get(i).getAmount() >= stack.getAmount()) {
                        ItemStack s = new ItemStack(slotList.get(i));
                        s.setAmount(s.getAmount() - stack.getAmount());
                        slotOf.add(s);
                        break;
                    }
                    if (slotList.stream().filter(s -> !s.getType().isAir()).count() != Arrays.stream(stackRecipe).filter(s -> !s.getType().isAir()).count())
                        return Map.entry(slotOf, result);
                }
            }
            result = (ItemStack) Base64Connector.decode(rs.getString("result"));
        }
        rs.close();
        return Map.entry(slotOf, result);
    }

    private static Map<Character, ItemStack> getIng(PreparedStatement ps) {
        Map<Character, ItemStack> ingredients = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (Base64Connector.decode(rs.getString("ingredients")) instanceof Map ing) {
                    ((Map<?, ?>) ing).forEach((o, ob) -> {
                        if (!(o instanceof Character))
                            throw new NoSuchElementException("Found Error to Ingredients key in RecipeFile");
                        if (!(Base64Connector.decode(ob.toString()) instanceof ItemStack itemStack))
                            throw new NoSuchElementException("Found Error to Ingredients value in RecipeFile");
                        ingredients.put((Character) o, itemStack);
                    });
                }
            }
            return ingredients;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean equalsOfAmount(ItemStack[] slot, ItemStack[] out) {
        boolean temp;
        for (int i = 0; i < 9; i++) {
            ItemStack s = new ItemStack(slot[i]);
            ItemStack o = new ItemStack(out[i]);
            if (s.equals(o)) temp = true;
            else {
                if (s.getAmount() >= o.getAmount()) {
                    s.setAmount(o.getAmount());
                    temp = s.equals(o);
                } else temp = false;
            }
            if (!temp)
                return false;
        }
        return true;
    }

    public static boolean equalsOfAmount(ItemStack slot, ItemStack out) {
        ItemStack s = new ItemStack(slot);
        ItemStack o = new ItemStack(out);
        if (s.equals(o)) return true;
        else {
            if (s.getAmount() >= o.getAmount()) {
                s.setAmount(o.getAmount());
                return s.equals(o);
            } else return false;
        }
    }
}
