package ru.livingcivilizations;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import ru.livingcivilizations.registry.ModEntities;
import ru.livingcivilizations.registry.ModItems;

@Mod(LivingCivilizations.MOD_ID)
public class LivingCivilizations {
    public static final String MOD_ID = "livingcivilizations";

    public LivingCivilizations() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITY_TYPES.register(bus);
        bus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
            event.accept(ModItems.TRAVELERS_STAFF);
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS)
            event.accept(ModItems.TRAVELER_SPAWN_EGG);
    }
}
