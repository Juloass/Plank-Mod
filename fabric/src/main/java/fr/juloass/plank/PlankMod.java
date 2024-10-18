package fr.juloass.plank;


import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

public class PlankMod implements ModInitializer {

    private static HashMap<String, Registrate> REGS = new HashMap<>();
    private static HashMap<String, HashSet<RegistryEntry<Item>>> REQUESTED_ITEM_GROUPS = new HashMap<>();
    private static HashSet<ItemGroupDefinition> ITEM_GROUPS = new HashSet<>();

    public static HashSet<ItemGroupDefinition> getItemGroupDefinitions() {
        return ITEM_GROUPS;
    }

    public static HashMap<String, HashSet<RegistryEntry<Item>>> getRequestedItemGroups() {
        return REQUESTED_ITEM_GROUPS;
    }

    @Override
    public void onInitialize() {
        CommonClass.init();
        PlankItemGroups.registerItemGroups();
        REGS.forEach((s, r) -> r.register());
    }

    public static void createItem(ItemDefinition itemDefinition) {

        Constants.LOG.info("Registering item !");
        String domain = itemDefinition.getDomain();
        Registrate REG = REGS.computeIfAbsent(domain, Registrate::create);

        if(itemDefinition.isAutoGenerate()) {
            RegistryEntry<Item> item = REG.item(itemDefinition.getName(), Item::new).defaultModel().defaultLang().register();
            HashSet<RegistryEntry<Item>> set = getRequestedItemGroups().computeIfAbsent(itemDefinition.getItemGroup(), s -> new HashSet<RegistryEntry<Item>>());
            set.add(item);
        }

    }

    public static void createItemGroup(ItemGroupDefinition itemGroupDefinition) {
        ITEM_GROUPS.add(itemGroupDefinition);
    }

}
