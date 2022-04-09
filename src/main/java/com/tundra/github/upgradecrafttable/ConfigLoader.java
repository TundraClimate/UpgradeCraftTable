package com.tundra.github.upgradecrafttable;

import com.tundra.finelib.FineLib;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigLoader {
    private final FileConfiguration config;

    private ConfigLoader() {
        FineLib.getPlugin().saveDefaultConfig();
        this.config =  FineLib.getPlugin().getConfig();
        File recipe = new File(FineLib.getPlugin().getDataFolder(), "recipes");
        if (recipe.mkdirs())
            Bukkit.getLogger().info("Generated " + FineLib.getPlugin().getDataFolder() + "\\recipes");
    }

    public boolean getAutoOpen() {
        return config.getBoolean("AutoOpen", true);
    }

    public static ConfigLoader getInstance() {
        return new ConfigLoader();
    }
}
