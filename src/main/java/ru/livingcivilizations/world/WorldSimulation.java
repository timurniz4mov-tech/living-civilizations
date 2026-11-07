package ru.livingcivilizations.world;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.livingcivilizations.LivingCivilizations;
import ru.livingcivilizations.entity.CivilizationNpc;
import ru.livingcivilizations.registry.ModEntities;

@Mod.EventBusSubscriber(modid = LivingCivilizations.MOD_ID)
public class WorldSimulation {
    private static final List<Supplier<EntityType<CivilizationNpc>>> AMBIENT = List.of(
            ModEntities.TRAVELER, ModEntities.CARAVAN_MERCHANT,
            ModEntities.VILLAGE_MERCHANT, ModEntities.WORKER,
            ModEntities.GUARD);

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;
        long time = level.getGameTime();

        // Ambient NPCs appear around explored areas, with a hard local cap.
        if (time % 1200L == 300L) {
            for (Player player : level.players()) spawnAmbient(level, player);
        }

        // Try at most once every 30 seconds until a settlement is successfully created.
        // Using loaded chunks close to the player avoids failed attempts in unloaded terrain.
        if (time % 600L == 20L) {
            for (Player player : level.players()) {
                if (!SettlementSavedData.get(level).hasNearby(player.blockPosition(), 560)) {
                    if (SettlementGenerator.generateNear(level, player.blockPosition(), false)) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                                "§6Живые цивилизации: §fнеподалёку основано новое поселение!"), false);
                    }
                    break;
                }
            }
        }

        // Bandit hideouts are occasional, persistent structures in the wilderness.
        if (time % 12000L == 600L) {
            for (Player player : level.players()) {
                if (level.random.nextInt(3) == 0) buildBanditCamp(level, player);
            }
        }
    }

    private static void spawnAmbient(ServerLevel level, Player player) {
        AABB area = player.getBoundingBox().inflate(64);
        if (level.getEntitiesOfClass(CivilizationNpc.class, area).size() >= 14) return;
        BlockPos air = randomSurface(level, player.blockPosition(), 22, 42);
        if (air == null) return;
        EntityType<CivilizationNpc> type = AMBIENT.get(level.random.nextInt(AMBIENT.size())).get();
        spawn(level, type, air);
    }

    private static void buildBanditCamp(ServerLevel level, Player player) {
        BlockPos air = randomSurface(level, player.blockPosition(), 48, 76);
        if (air == null) return;
        BlockPos ground = air.below();
        // Ground all camp blocks on the terrain surface, not in the air above it.
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            level.setBlock(ground.offset(x, 0, z), Blocks.COARSE_DIRT.defaultBlockState(), 3);
        }
        for (int x = -1; x <= 1; x++) {
            level.setBlock(ground.offset(x, 1, -1), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
            level.setBlock(ground.offset(x, 1, 1), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
            level.setBlock(ground.offset(x, 2, 0), Blocks.RED_WOOL.defaultBlockState(), 3);
        }
        level.setBlock(ground.offset(0, 1, 0), Blocks.CAMPFIRE.defaultBlockState(), 3);
        level.setBlock(ground.offset(2, 1, 0), Blocks.CHEST.defaultBlockState(), 3);
        for (int x = -2; x <= 2; x += 4) for (int z = -2; z <= 2; z += 4)
            level.setBlock(ground.offset(x, 1, z), Blocks.OAK_FENCE.defaultBlockState(), 3);

        spawn(level, ModEntities.BANDIT.get(), ground.offset(1, 1, 2));
        spawn(level, ModEntities.BANDIT.get(), ground.offset(-1, 1, 2));
        if (level.random.nextBoolean()) spawn(level, ModEntities.BANDIT.get(), ground.offset(0, 1, -2));
    }

    private static void spawn(ServerLevel level, EntityType<CivilizationNpc> type, BlockPos feetBlock) {
        CivilizationNpc npc = type.create(level);
        if (npc == null) return;
        BlockPos supportPos = feetBlock.below();
        double supportHeight = level.getBlockState(supportPos)
                .getCollisionShape(level, supportPos, net.minecraft.world.phys.shapes.CollisionContext.empty())
                .max(net.minecraft.core.Direction.Axis.Y);
        if (supportHeight <= 0.0D) supportHeight = 1.0D;
        double y = supportPos.getY() + supportHeight;
        npc.moveTo(feetBlock.getX() + 0.5D, y, feetBlock.getZ() + 0.5D,
                level.random.nextFloat() * 360F, 0F);
        npc.setPersistenceRequired();
        level.addFreshEntity(npc);
    }

    /** Returns the first air block above a safe solid surface. */
    private static BlockPos randomSurface(ServerLevel level, BlockPos origin, int min, int max) {
        for (int attempt = 0; attempt < 18; attempt++) {
            int dx = level.random.nextInt(max * 2 + 1) - max;
            int dz = level.random.nextInt(max * 2 + 1) - max;
            if (dx * dx + dz * dz < min * min) continue;
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(origin.getX() + dx, 0, origin.getZ() + dz));
            BlockPos ground = top.below();
            if (!level.getBlockState(top).isAir()) continue;
            if (!level.getBlockState(ground).isSolidRender(level, ground)) continue;
            if (!level.getFluidState(ground).isEmpty()) continue;
            return top;
        }
        return null;
    }
}
