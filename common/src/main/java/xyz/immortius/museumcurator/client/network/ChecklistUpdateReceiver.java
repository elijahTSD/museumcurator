package xyz.immortius.museumcurator.client.network;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.museumcurator.common.data.MuseumCollections;
import xyz.immortius.museumcurator.common.network.ChecklistUpdateMessage;

/** Processes server checklist updates on a client. */
public class ChecklistUpdateReceiver {
    public static void receive(LocalPlayer player, ChecklistUpdateMessage msg) {
        if (msg.isClearAll()) {
            MuseumCollections.clearCheckedItems();
        }
        MuseumCollections.uncheckItems(msg.getUncheckedItems());
        MuseumCollections.checkItems(msg.getCheckedItems());
    }
}
