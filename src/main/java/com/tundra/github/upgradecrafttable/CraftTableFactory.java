package com.tundra.github.upgradecrafttable;

import com.tundra.finelib.builder.InventoryBuilder;
import com.tundra.finelib.builder.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CraftTableFactory {
    public static Inventory generateCraftTable(){
        ItemStack blackPane = new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .addContainerData("cancel", PersistentDataType.STRING, "cancel")
                .toItemStack();
        ItemStack barrier = new ItemStackBuilder(Material.BARRIER)
                .setDisplayName("§cNot Found")
                .addContainerData("cancel", PersistentDataType.STRING, "cancel")
                .toItemStack();
        return new InventoryBuilder(5, "Craft Table")
                .fillItem(blackPane)
                .remove(10).remove(11).remove(12).remove(19).remove(20).remove(21).remove(28).remove(29).remove(30)
                .setItem(24, barrier)
                .toInventory();
    }
}
