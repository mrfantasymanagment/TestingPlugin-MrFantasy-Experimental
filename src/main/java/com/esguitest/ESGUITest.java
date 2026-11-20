package com.mrfantasy.esguitest;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ESGUITest extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new TransactionListener(this), this);

        BuyItemCommand buyItem = new BuyItemCommand(this);
        PluginCommand cmd = getCommand("buyitem");
        if (cmd != null) {
            cmd.setExecutor(buyItem);
            cmd.setTabCompleter(buyItem);
        }
        for (java.lang.reflect.Method m : me.gypopo.economyshopgui.api.EconomyShopGUIHook.class.getDeclaredMethods()) {
            if (m.getName().equals("purchaseItem")) getLogger().info("API tiene: " + m);
        }

        getLogger().info("GUITest Activo");
    }
}