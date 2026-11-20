package com.mrfantasy.esguitest;

import org.bukkit.plugin.java.JavaPlugin;

public class ESGUITest extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new TransactionListener(this), this);
        getLogger().info("GUITest Activo");
    } 

}