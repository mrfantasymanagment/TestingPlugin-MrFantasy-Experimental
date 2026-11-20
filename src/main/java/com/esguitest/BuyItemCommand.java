package com.mrfantasy.esguitest;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.api.objects.TransactionResult;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * /buyitem <item> [cantidad]
 * Compra un item de EconomyShopGUI delegando toda la transacción en la API.
 */
public class BuyItemCommand implements CommandExecutor, TabCompleter {

    // Tope de seguridad: un inventario completo de stacks de 64
    private static final int MAX_AMOUNT = 36 * 64;

    private final ESGUITest plugin;

    public BuyItemCommand(ESGUITest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }
        if (args.length < 1 || args.length > 2) {
            player.sendMessage("§cUso: /" + label + " <item> [cantidad]");
            return true;
        }

        // 1. Item
        Material mat = Material.matchMaterial(args[0]);
        if (mat == null || mat.isLegacy() || mat.isAir() || !mat.isItem()) {
            player.sendMessage("§cItem inválido: " + args[0]);
            return true;
        }

        // 2. Cantidad
        int amount = 1;
        if (args.length == 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cLa cantidad debe ser un número entero.");
                return true;
            }
        }
        if (amount < 1 || amount > MAX_AMOUNT) {
            player.sendMessage("§cLa cantidad debe estar entre 1 y " + MAX_AMOUNT + ".");
            return true;
        }

        // 3. Buscar el item en la tienda (sin jugador = solo "¿existe?")
        ItemStack stack = new ItemStack(mat);
        if (EconomyShopGUIHook.getShopItem(stack) == null) {
            player.sendMessage("§cEse item no está en la tienda.");
            return true;
        }

        // Con jugador: valida permisos de sección/item, requisitos y display items
        ShopItem shopItem = EconomyShopGUIHook.getShopItem(player, stack);
        if (shopItem == null) {
            player.sendMessage("§cNo puedes comprar ese item (sin permiso o no cumples los requisitos).");
            return true;
        }
        if (!EconomyShopGUIHook.isBuyAble(shopItem)) {
            player.sendMessage("§cEse item no se puede comprar.");
            return true;
        }

        // 4. Transacción completa a cargo de EconomyShopGUI
        TransactionResult result = EconomyShopGUIHook.purchaseItem(
                player,
                shopItem,
                amount,
                true,   // checkPermissions
                true,   // checkRequirements
                false,  // allowQuantityChange
                true    // playerTriggered
        );

        // 5. Resultados que ESGUI NO le avisa al jugador
        switch (result.getResult()) {
            case SUCCESS, SUCCESS_COMMANDS_EXECUTED,
                 INSUFFICIENT_FUNDS, NO_INVENTORY_SPACE, NO_ITEM_STOCK_LEFT -> { /* ya notificado por ESGUI */ }
            case NO_PERMISSIONS -> player.sendMessage("§cNo tienes permiso para comprar ese item.");
            case REQUIREMENTS_FAILED -> player.sendMessage("§cNo cumples los requisitos para comprar ese item.");
            case TO_MANY_ITEMS -> player.sendMessage("§cEsa cantidad supera el máximo de compra permitido para este item.");
            case NOT_ENOUGH_ITEMS -> player.sendMessage("§cEsa cantidad es menor al mínimo de compra de este item.");
            case TRANSACTION_CANCELLED -> player.sendMessage("§cLa compra fue cancelada.");
            case ITEM_ERROR, DISPLAY_ITEM, NEGATIVE_ITEM_PRICE ->
                    player.sendMessage("§cEse item no está disponible para compra.");
            default -> player.sendMessage("§cNo se pudo completar la compra (" + result.getResult() + ").");
        }

        plugin.getLogger().info("[BUYITEM] jugador=" + player.getName()
                + " item=" + mat.name()
                + " pedido=" + amount
                + " resultado=" + result.getResult()
                + " entregado=" + result.getAmount()
                + " precios=" + result.getPrices());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            String start = args[0].toLowerCase(Locale.ROOT);
            // Primero el filtro barato (prefijo) y recién después la consulta a la tienda
            return Stream.of(Material.values())
                    .filter(m -> !m.isLegacy() && !m.isAir() && m.isItem())
                    .map(m -> m.name().toLowerCase(Locale.ROOT))
                    .filter(n -> n.startsWith(start))
                    .filter(n -> {
                        Material m = Material.matchMaterial(n);
                        if (m == null) return false;
                        ShopItem si = EconomyShopGUIHook.getShopItem(new ItemStack(m));
                        return si != null && EconomyShopGUIHook.isBuyAble(si);
                    })
                    .limit(50)
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return List.of("1", "16", "32", "64");
        }
        return Collections.emptyList();
    }
}
