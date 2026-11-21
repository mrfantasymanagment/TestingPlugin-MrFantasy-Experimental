package com.mrfantasy.esguitest;

import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TransactionListener implements Listener {

    private final ESGUITest plugin;

    public TransactionListener(ESGUITest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPre(PreTransactionEvent event) {
        plugin.getLogger().info("[PRE] tipo=" + event.getTransactionType()
                + " jugador=" + event.getPlayer().getName()
                + " cantidad=" + event.getAmount());
    }

    @EventHandler
    public void onPost(PostTransactionEvent event) {
        plugin.getLogger().info("[POST] tipo=" + event.getTransactionType()
                + " jugador=" + event.getPlayer().getName()
                + " resultado=" + event.getTransactionResult());
    }
}