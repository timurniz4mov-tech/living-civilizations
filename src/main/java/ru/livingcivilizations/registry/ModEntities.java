package ru.livingcivilizations.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.livingcivilizations.LivingCivilizations;
import ru.livingcivilizations.entity.TravelerEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LivingCivilizations.MOD_ID);

    public static final RegistryObject<EntityType<TravelerEntity>> TRAVELER =
        ENTITY_TYPES.register("traveler", () -> EntityType.Builder
            .of(TravelerEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.95F).clientTrackingRange(8).build("traveler"));
}
