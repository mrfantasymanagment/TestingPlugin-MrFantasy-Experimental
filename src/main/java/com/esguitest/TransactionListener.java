package com.mrfantasy.esguitest;

import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class TransactionListener implements Listener {

    // SELECTED_ITEM de buy-screen / buy-stacks-screen en el config de ESGUI
    private static final int SELECTED_SLOT = 22;

    private final ESGUITest plugin;

    public TransactionListener(ESGUITest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPost(PostTransactionEvent event) {
        // Solo: compra desde la GUI que ESGUI rechazó por 0 espacio (la parcial ya la gestiona ESGUI)
        if (event.getTransactionResult() != Transaction.Result.NO_INVENTORY_SPACE) return;

        Transaction.Type type = event.getTransactionType();
        if (type != Transaction.Type.BUY_SCREEN && type != Transaction.Type.BUY_STACKS_SCREEN) return;

        ShopItem shopItem = event.getShopItem();
        if (shopItem == null) return;

        Player player = event.getPlayer();

        // Se lee ahora, de forma síncrona: es la cantidad elegida en la GUI (slot 22)
        Inventory top = player.getOpenInventory().getTopInventory();
        if (top.getSize() <= SELECTED_SLOT) return;
        ItemStack selected = top.getItem(SELECTED_SLOT);
        if (selected == null || selected.getAmount() < 1) return;

        // El slot cuenta stacks en BUY_STACKS_SCREEN e items en BUY_SCREEN
        String unit = type == Transaction.Type.BUY_STACKS_SCREEN ? "stacks" : "items";
        String material = shopItem.getItemToGive().getType().name().toLowerCase(Locale.ROOT);
        String command = "buyitem " + material + " " + selected.getAmount() + " " + unit;

        // Un tick después, para no ejecutarlo dentro del flujo de ESGUI
        Bukkit.getScheduler().runTask(plugin, () -> player.performCommand(command));
    }
}