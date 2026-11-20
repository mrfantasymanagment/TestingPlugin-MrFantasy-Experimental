package com.mrfantasy.esguitest;

import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

        // ESGUI rechazó la compra por falta de espacio: la completamos nosotros dropeando el exceso
        if (event.getTransactionResult() != Transaction.Result.NO_INVENTORY_SPACE) return;
        if (!isGuiBuy(event.getTransactionType())) return;

        ShopItem shopItem = event.getShopItem();
        if (shopItem == null) return;

        Player player = event.getPlayer();
        int amount = event.getAmount();
        if (amount < 1) return;

        // Un tick después: nuestro mensaje queda debajo del de ESGUI y siempre corremos en el hilo principal
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            PurchaseService.purchase(plugin, player, shopItem, amount, "gui");
        });
    }

    /** Solo compras del jugador desde la GUI. Se excluyen API_BUY (evita bucles) y SHOPSTAND (venta entre jugadores). */
    private static boolean isGuiBuy(Transaction.Type type) {
        return type == Transaction.Type.BUY_SCREEN
                || type == Transaction.Type.BUY_STACKS_SCREEN
                || type == Transaction.Type.QUICK_BUY;
    }
}