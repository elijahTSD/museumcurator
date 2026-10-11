package xyz.immortius.museumcurator.server;

import com.google.common.base.Strings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import xyz.immortius.museumcurator.common.MuseumCuratorConstants;
import xyz.immortius.museumcurator.common.data.CollectionItem;
import xyz.immortius.museumcurator.common.data.MuseumCollections;
import xyz.immortius.museumcurator.common.network.ChecklistUpdateMessage;
import xyz.immortius.museumcurator.config.MuseumCuratorConfig;
import xyz.immortius.museumcurator.interop.Services;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Persistence for the checklist state, and provides methods for updating it.
 */
public class ChecklistState extends SavedData {
    private MinecraftServer server;
    private final UUID ownerId;
    private final Set<ItemStack> checkedItems = new LinkedHashSet<>();

    private static final Codec<ChecklistState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(state -> new ArrayList<>(state.checkedItems)),
            Codec.STRING.optionalFieldOf("owner", "").forGetter(state -> state.ownerId == null ? "" : state.ownerId.toString())
    ).apply(instance, (items, owner) -> new ChecklistState(null, owner, items)));

    private static SavedDataType<ChecklistState> dataType(String storageName, MinecraftServer server, String ownerId) {
        return new SavedDataType<>(
                Identifier.fromNamespaceAndPath(MuseumCuratorConstants.MOD_ID, storageName),
                () -> new ChecklistState(server, ownerId),
                CODEC,
                null
        );
    }

    public static ChecklistState get(MinecraftServer server, ServerPlayer player) {
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
        if (level == null) {
            return new ChecklistState(server, "");
        }

        String owner = "";
        if (MuseumCuratorConfig.get().gameplayConfig.isIndividualChecklists(MuseumCuratorConfig.get())) {
            owner = Services.GROUP_HELPER.getLeaderId(player);
        }
        String storageName = Strings.isNullOrEmpty(owner) ? "museumchecklist" : "museumchecklist-" + owner;
        ChecklistState state = level.getDataStorage().computeIfAbsent(dataType(storageName, server, owner));
        state.server = server;
        return state;
    }

    private ChecklistState(MinecraftServer server, String ownerId, Collection<ItemStack> items) {
        this(server, ownerId);
        this.checkedItems.addAll(items);
    }

    public ChecklistState(MinecraftServer server, String ownerId) {
        this.server = server;
        this.ownerId = Strings.isNullOrEmpty(ownerId) ? null : UUID.fromString(ownerId);
    }

    public synchronized boolean check(Collection<ItemStack> items) {
        List<ItemStack> toAdd = items.stream()
                .map(MuseumCollections::getCollectionItemStack)
                .filter(Objects::nonNull)
                .map(CollectionItem::itemStack)
                .filter(x -> !checkedItems.contains(x))
                .toList();
        if (!toAdd.isEmpty()) {
            checkedItems.addAll(toAdd);
            ChecklistUpdateMessage msg = ChecklistUpdateMessage.check(toAdd);
            if (MuseumCuratorConfig.get().gameplayConfig.isIndividualChecklists(MuseumCuratorConfig.get())) {
                for (ServerPlayer player : Services.GROUP_HELPER.getGroupPlayers(server, ownerId)) {
                    Services.PLATFORM.sendChecklistUpdate(server, player, msg);
                }
            } else {
                Services.PLATFORM.broadcastChecklistUpdate(server, msg);
            }
            setDirty();
            return true;
        }
        return false;
    }

    public synchronized boolean uncheck(Collection<ItemStack> items) {
        Set<ItemStack> toRemove = items.stream()
                .map(MuseumCollections::getCollectionItemStack)
                .filter(Objects::nonNull)
                .map(CollectionItem::itemStack)
                .filter(checkedItems::contains)
                .collect(Collectors.toSet());
        if (!toRemove.isEmpty()) {
            checkedItems.removeAll(toRemove);
            ChecklistUpdateMessage msg = ChecklistUpdateMessage.uncheck(toRemove);
            if (MuseumCuratorConfig.get().gameplayConfig.isIndividualChecklists(MuseumCuratorConfig.get())) {
                for (ServerPlayer player : Services.GROUP_HELPER.getGroupPlayers(server, ownerId)) {
                    Services.PLATFORM.sendChecklistUpdate(server, player, msg);
                }
            } else {
                Services.PLATFORM.broadcastChecklistUpdate(server, msg);
            }
            setDirty();
            return true;
        }
        return false;
    }

    public synchronized void uncheckAll() {
        if (!checkedItems.isEmpty()) {
            checkedItems.clear();
            ChecklistUpdateMessage msg = ChecklistUpdateMessage.uncheckAll();
            if (MuseumCuratorConfig.get().gameplayConfig.isIndividualChecklists(MuseumCuratorConfig.get())) {
                for (ServerPlayer player : Services.GROUP_HELPER.getGroupPlayers(server, ownerId)) {
                    Services.PLATFORM.sendChecklistUpdate(server, player, msg);
                }
            } else {
                Services.PLATFORM.broadcastChecklistUpdate(server, msg);
            }
            setDirty();
        }
    }

    public synchronized Set<ItemStack> getCheckedItems() {
        return new LinkedHashSet<>(checkedItems);
    }
}
