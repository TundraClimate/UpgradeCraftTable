package com.tundra.github.upgradecrafttable;

import com.tundra.finelib.FineLib;
import com.tundra.finelib.database.sqlite.SQLite;
import com.tundra.github.upgradecrafttable.event.CraftTableDetector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.NoSuchFileException;

public final class UpgradeCraftTable extends JavaPlugin {
    private static final SQLite sqlite = new SQLite();

    @Override
    public void onEnable() {
        FineLib.setPlugin(this);
        sqlite.connectSQLite("temp.db");
        SQLiteAction.createMainTable();
        ConfigLoader loader = ConfigLoader.getInstance();
        new CraftTableDetector(this);
        startRunnable();
        try {
            SQLiteAction.loadRecipes();
        } catch (NoSuchFileException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        SQLiteAction.dropMainTable();
        sqlite.disconnectSQLite();
    }

    public static SQLite getSqlite() {
        return sqlite;
    }

    private void startRunnable() {
        Bukkit.getScheduler().runTaskTimer(FineLib.getPlugin(), () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.hasMetadata("crafter")) {
                    Inventory inventory = player.getOpenInventory().getTopInventory();
                    ItemStack s = RecipeLocalServer.queryRecipeResult(
                            inventory.getItem(10) != null ? inventory.getItem(10) : new ItemStack(Material.AIR),
                            inventory.getItem(11) != null ? inventory.getItem(11) : new ItemStack(Material.AIR),
                            inventory.getItem(12) != null ? inventory.getItem(12) : new ItemStack(Material.AIR),
                            inventory.getItem(19) != null ? inventory.getItem(19) : new ItemStack(Material.AIR),
                            inventory.getItem(20) != null ? inventory.getItem(20) : new ItemStack(Material.AIR),
                            inventory.getItem(21) != null ? inventory.getItem(21) : new ItemStack(Material.AIR),
                            inventory.getItem(28) != null ? inventory.getItem(28) : new ItemStack(Material.AIR),
                            inventory.getItem(29) != null ? inventory.getItem(29) : new ItemStack(Material.AIR),
                            inventory.getItem(30) != null ? inventory.getItem(30) : new ItemStack(Material.AIR)
                    );
                    if (s != null && inventory.getType() != InventoryType.PLAYER)
                        inventory.setItem(24, s);
                }
            });
        }, 0L, 0L);
    }
}
