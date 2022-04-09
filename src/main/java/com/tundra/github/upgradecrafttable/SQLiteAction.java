package com.tundra.github.upgradecrafttable;

import com.google.gson.Gson;
import com.tundra.finelib.FineLib;
import com.tundra.finelib.util.Base64Connector;
import com.tundra.github.upgradecrafttable.object.RecipeObject;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.NoSuchFileException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class SQLiteAction {
    public static void createMainTable() {
        try {
            if (!UpgradeCraftTable.getSqlite().hasTable("temp"))
                UpgradeCraftTable.getSqlite().executeUpdate("CREATE TABLE temp(type TEXT, result TEXT, recipe TEXT, ingredients TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void dropMainTable() {
        try {
            if (UpgradeCraftTable.getSqlite().hasTable("temp"))
                UpgradeCraftTable.getSqlite().executeUpdate("DROP TABLE temp");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadRecipes() throws NoSuchFileException {
        File file = new File(FineLib.getPlugin().getDataFolder(), "recipes");
        if (!file.exists()) throw new NoSuchFileException("\"recipes\" is Not Found");
        Gson gson = new Gson();
        try {
            PreparedStatement ps = UpgradeCraftTable.getSqlite().prepareStatement("INSERT INTO temp VALUES(?, ?, ?, ?)");
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                RecipeObject object = gson.fromJson(new FileReader(listFile), RecipeObject.class);
                ps.setString(1, Base64Connector.encode(object.getType()));
                ps.setString(2, Base64Connector.encode(object.getResult()));
                ps.setString(3, Base64Connector.encode(object.getRecipe()));
                ps.setString(4, Base64Connector.encode(object.getIngredients()));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Bukkit.getLogger().warning("File in \"recipes\" Not Found");
        }
    }
}