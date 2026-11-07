package ru.livingcivilizations.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.livingcivilizations.LivingCivilizations;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, LivingCivilizations.MOD_ID);

    public static final RegistryObject<Item> TRAVELERS_STAFF =
        ITEMS.register("travelers_staff", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TRAVELER_SPAWN_EGG =
        ITEMS.register("traveler_spawn_egg", () -> new ForgeSpawnEggItem(
            ModEntities.TRAVELER, 0x6B4935, 0xD6B77A, new Item.Properties()));
}
