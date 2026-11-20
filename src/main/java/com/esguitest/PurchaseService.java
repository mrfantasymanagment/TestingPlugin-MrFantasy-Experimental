package com.mrfantasy.esguitest;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.providers.EconomyProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Map;

/**
 * Lógica de compra manual compartida entre el comando /buyitem y la compra desde la GUI.
 * Cobra, entrega lo que entre en el inventario y dropea el exceso a los pies del jugador.
 */
public final class PurchaseService {

    private PurchaseService() {
    }

    /**
     * Debe llamarse desde el hilo principal.
     *
     * @return true si la compra se completó
     */
    public static boolean purchase(ESGUITest plugin, Player player, ShopItem shopItem, int amount, String source) {
        double price = EconomyShopGUIHook.getItemBuyPrice(shopItem, player, amount);
        EconomyProvider eco = EconomyShopGUIHook.getEcon(shopItem.getEcoType());
        if (eco == null) {
            player.sendMessage("§cNo se encontró la economía de este item.");
            return false;
        }
        if (eco.getBalance(player) < price) {
            player.sendMessage("§cNo tienes fondos suficientes (necesitas " + eco.formatPrice(price) + ").");
            return false;
        }

        ItemStack toGive = shopItem.getItemToGive().clone();
        toGive.setAmount(amount);

        eco.withdrawBalance(player, price);
        int dropped = giveOrDrop(player, toGive);

        if (dropped > 0) {
            player.sendMessage("§eTu inventario está lleno: " + dropped + " items se dropearon al suelo.");
        }
        String name = toGive.getType().name().toLowerCase(Locale.ROOT);
        player.sendMessage("§aComprado x" + amount + " " + name + " por " + eco.formatPrice(price) + ".");

        plugin.getLogger().info("[BUYITEM:" + source + "] jugador=" + player.getName()
                + " item=" + toGive.getType().name() + " cantidad=" + amount
                + " precio=" + price + " dropeados=" + dropped);
        return true;
    }

    /** Entrega al inventario y dropea el resto. Devuelve la cantidad dropeada. */
    private static int giveOrDrop(Player player, ItemStack toGive) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(toGive);
        Location loc = player.getLocation();
        int dropped = 0;
        for (ItemStack left : leftovers.values()) {
            // Se dropea en stacks válidos (no una sola entidad con amount > maxStack)
            int max = Math.max(1, left.getMaxStackSize());
            int remaining = left.getAmount();
            while (remaining > 0) {
                int n = Math.min(max, remaining);
                ItemStack chunk = left.clone();
                chunk.setAmount(n);
                player.getWorld().dropItemNaturally(loc, chunk);
                remaining -= n;
            }
            dropped += left.getAmount();
        }
        return dropped;
    }
}
