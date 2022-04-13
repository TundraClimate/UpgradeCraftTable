package com.tundra.github.upgradecrafttable;

import com.tundra.finelib.FineLib;
import com.tundra.finelib.database.sqlite.SQLite;
import com.tundra.finelib.util.Base64Connector;
import com.tundra.github.upgradecrafttable.event.CraftTableDetector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UpgradeCraftTable extends JavaPlugin {
    private static final SQLite sqlite = new SQLite();
    private static final Map<Player, List<ItemStack>> craftSlot = new HashMap<>();

    @Override
    public void onEnable() {
        FineLib.setPlugin(this);
        sqlite.connectSQLite("temp.db");
        SQLiteAction.createMainTable();
        ConfigLoader loader = ConfigLoader.getInstance();
        new CraftTableDetector(this);
        FineLib.testLogging(Base64Connector.encode(new ItemStack(Material.COBBLESTONE,32)));
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
                    Map.Entry<Player,Map.Entry<List<ItemStack>, ItemStack>> s = RecipeLocalServer.queryRecipeResult(
                            player,
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
                    craftSlot.put(player, s.getValue().getKey());
                    if (s.getValue().getValue() != null && inventory.getType() != InventoryType.PLAYER)
                        inventory.setItem(24, s.getValue().getValue());
                }
            });
        }, 0L, 0L);
    }

    public static Map<Player, List<ItemStack>> getCraftSlot() {
        return craftSlot;
    }
}
