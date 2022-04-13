package com.tundra.github.upgradecrafttable.event;

import com.tundra.finelib.FineLib;
import com.tundra.github.upgradecrafttable.ConfigLoader;
import com.tundra.github.upgradecrafttable.CraftTableFactory;
import com.tundra.github.upgradecrafttable.RecipeLocalServer;
import com.tundra.github.upgradecrafttable.UpgradeCraftTable;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CraftTableDetector implements Listener {
    public CraftTableDetector(JavaPlugin plugin) {plugin.getServer().getPluginManager().registerEvents(this, plugin);}

    @EventHandler
    public void detectOpen(InventoryOpenEvent e) {
        if (!ConfigLoader.getInstance().getAutoOpen()) return;
        if (e.getPlayer().hasPermission("uct.open")) {
            if (e.getInventory().getType() == InventoryType.WORKBENCH) {
                e.setCancelled(true);
                e.getPlayer().setMetadata("crafter", new FixedMetadataValue(FineLib.getPlugin(), true));
                e.getPlayer().openInventory(CraftTableFactory.generateCraftTable());
            }
        }
    }

    @EventHandler
    public void detectClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            if (player.hasMetadata("crafter")) {
                player.removeMetadata("crafter", FineLib.getPlugin());
                List<Integer> index = List.of(10, 11, 12, 19, 20, 21, 28, 29, 30);
                index.forEach(i -> {
                    if (e.getInventory().getItem(i) != null)
                        e.getPlayer().getInventory().addItem(e.getInventory().getItem(i));
                });
            }
        }
    }

    @EventHandler
    public void detectClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;
        Inventory inv = e.getClickedInventory();
        if (inv == null) return;
        PersistentDataContainer container = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
        if (container.has(new NamespacedKey(FineLib.getPlugin(), "cancel"), PersistentDataType.STRING)) {
            e.setCancelled(true);
        } else if (e.getSlot() == 24 && e.getClickedInventory().getType() != InventoryType.PLAYER) {
            List<Integer> index = List.of(10, 11, 12, 19, 20, 21, 28, 29, 30);
            List<ItemStack> slot = UpgradeCraftTable.getCraftSlot().get((Player) e.getWhoClicked());
            for (int i = 0; i < index.size(); i++) {
                inv.setItem(index.get(i), slot.get(i));
            }
            if (e.getCursor() == null) return;
            if (RecipeLocalServer.equalsOfAmount(e.getCursor(), e.getCurrentItem())){
                e.setCancelled(true);
                e.getCursor().setAmount(e.getCursor().getAmount() + e.getCurrentItem().getAmount());
            }
        }
    }
}
