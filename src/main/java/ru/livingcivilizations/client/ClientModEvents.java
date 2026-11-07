package ru.livingcivilizations.client;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.livingcivilizations.LivingCivilizations;
import ru.livingcivilizations.entity.TravelerEntity;
import ru.livingcivilizations.registry.ModEntities;

@Mod.EventBusSubscriber(modid = LivingCivilizations.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> EntityRenderers.register(ModEntities.TRAVELER.get(), TravelerRenderer::new));
    }

    private static class TravelerRenderer extends MobRenderer<TravelerEntity, VillagerModel<TravelerEntity>> {
        private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/villager/villager.png");

        private TravelerRenderer(EntityRendererProvider.Context context) {
            super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(TravelerEntity entity) {
            return TEXTURE;
        }
    }
}
