package com.mrfantasy.esguitest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/** SOLO DIAGNÓSTICO: registra los clicks en inventarios superiores. No modifica nada. */
public class DebugClickListener implements Listener {

    private final ESGUITest plugin;

    public DebugClickListener(ESGUITest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() == null
                || e.getClickedInventory() != e.getView().getTopInventory()) return;

        ItemStack it = e.getCurrentItem();
        String itemInfo = "null";
        if (it != null) {
            ItemMeta meta = it.getItemMeta();
            String name = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : "-";
            String lore = (meta != null && meta.hasLore()) ? String.valueOf(meta.getLore()) : "-";
            itemInfo = it.getType() + " x" + it.getAmount() + " nombre=" + name + " lore=" + lore;
        }

        long libres = Arrays.stream(p.getInventory().getStorageContents())
                .filter(i -> i == null || i.getType().isAir())
                .count();

        plugin.getLogger().info("[CLICK] jugador=" + p.getName()
                + " titulo=" + e.getView().getTitle()
                + " holder=" + e.getView().getTopInventory().getHolder()
                + " slot=" + e.getSlot()
                + " click=" + e.getClick()
                + " slotsLibres=" + libres
                + " item=" + itemInfo);
    }
}