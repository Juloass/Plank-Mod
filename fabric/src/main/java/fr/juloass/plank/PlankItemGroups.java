package fr.juloass.plank;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupBuilderImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlankItemGroups {

    public static final CreativeModeTab BASE_GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation("plank", "base"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.plank.base"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(Blocks.OAK_PLANKS);
                    })
                    .icon(() -> new ItemStack(Blocks.OAK_PLANKS.asItem()))
                    .build()
    );

    public static void registerItemGroups() {
        Constants.LOG.info("Registering item groups ...");
        HashMap<String, HashSet<RegistryEntry<Item>>>  req = PlankMod.getRequestedItemGroups();

        PlankMod.getItemGroupDefinitions().forEach((itemGroupDefinition) -> {

            CreativeModeTab.Builder builder = FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup." + itemGroupDefinition.getDomain() + "." + itemGroupDefinition.getName()));

            if(req.containsKey(itemGroupDefinition.getDomain() + ":" + itemGroupDefinition.getName())) {
                HashSet<RegistryEntry<Item>> items = req.get(itemGroupDefinition.getDomain() + ":" + itemGroupDefinition.getName());
                items.stream().findFirst().ifPresent((i) -> {
                    builder.icon(() -> new ItemStack(i.get()));
                });
            }

            builder.displayItems((itemDisplayParameters, output) -> {
                if(req.containsKey(itemGroupDefinition.getDomain() + ":" + itemGroupDefinition.getName())) {
                    HashSet<RegistryEntry<Item>> items = req.get(itemGroupDefinition.getDomain() + ":" + itemGroupDefinition.getName());
                    output.acceptAll(items.stream().map(RegistryEntry::get).map(ItemStack::new).collect(Collectors.toList()));
                }
            });

            CreativeModeTab k = Registry.register(
                    BuiltInRegistries.CREATIVE_MODE_TAB,
                    new ResourceLocation(itemGroupDefinition.getDomain(), itemGroupDefinition.getName()),
                    builder.build()
            );

        });

    }

}
