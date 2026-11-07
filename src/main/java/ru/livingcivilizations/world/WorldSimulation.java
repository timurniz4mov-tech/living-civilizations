package ru.livingcivilizations.world;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.livingcivilizations.LivingCivilizations;
import ru.livingcivilizations.entity.CivilizationNpc;
import ru.livingcivilizations.kingdom.KingdomSavedData;
import ru.livingcivilizations.registry.ModBlocks;
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
        long time = level.getGameTime();
        if (time % 1200L == 0L) {
            for (Player player : level.players()) {
                if (level.random.nextInt(3) == 0) spawnAmbient(level, player);
            }
        }
        if (time % 24000L == 18000L) {
            for (Player player : level.players()) {
                if (level.random.nextInt(3) == 0) buildBanditCamp(level, player);
                long day = time / 24000L;
                if (day > 0 && day % 5 == 0 && level.random.nextInt(3) == 0) buildSettlement(level, player);
                if (day > 0 && day % 9 == 0 && level.random.nextInt(4) == 0) buildKingdomCastle(level, player);
            }
        }
    }

    private static void spawnAmbient(ServerLevel level, Player player) {
        AABB area = player.getBoundingBox().inflate(64);
        if (level.getEntitiesOfClass(CivilizationNpc.class, area).size() >= 14) return;
        BlockPos pos = randomSurface(level, player.blockPosition(), 22, 42);
        if (pos == null) return;
        EntityType<CivilizationNpc> type = AMBIENT.get(level.random.nextInt(AMBIENT.size())).get();
        spawn(level, type, pos);
    }

    private static void buildBanditCamp(ServerLevel level, Player player) {
        BlockPos base = randomSurface(level, player.blockPosition(), 40, 64);
        if (base == null) return;
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++)
            level.setBlock(base.offset(x, 0, z), Blocks.COARSE_DIRT.defaultBlockState(), 3);
        for (int x = -1; x <= 1; x++) {
            level.setBlock(base.offset(x, 1, -1), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 1, 1), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 2, 0), Blocks.RED_WOOL.defaultBlockState(), 3);
        }
        level.setBlock(base.offset(0, 1, 0), Blocks.CAMPFIRE.defaultBlockState(), 3);
        level.setBlock(base.offset(2, 1, 0), Blocks.CHEST.defaultBlockState(), 3);
        for (int x = -2; x <= 2; x += 4) for (int z = -2; z <= 2; z += 4)
            level.setBlock(base.offset(x, 1, z), Blocks.OAK_FENCE.defaultBlockState(), 3);
        spawn(level, ModEntities.BANDIT.get(), base.offset(1, 1, 2));
        spawn(level, ModEntities.BANDIT.get(), base.offset(-1, 1, 2));
        if (level.random.nextBoolean()) spawn(level, ModEntities.BANDIT.get(), base.offset(0, 1, -2));
    }

    private static void buildSettlement(ServerLevel level, Player player) {
        BlockPos center = randomSurface(level, player.blockPosition(), 72, 112);
        if (center == null) return;
        buildHouse(level, center.offset(-7, 0, 0));
        buildHouse(level, center.offset(7, 0, 0));
        for (int z = -7; z <= 7; z++) {
            for (int x = -1; x <= 1; x++) {
                level.setBlock(center.offset(x, 0, z), ModBlocks.GRAVEL_ROAD.get().defaultBlockState(), 3);
            }
        }
        level.setBlock(center.offset(0, 1, 0), Blocks.LANTERN.defaultBlockState(), 3);
        spawn(level, ModEntities.VILLAGE_MERCHANT.get(), center.offset(-5, 1, 2));
        spawn(level, ModEntities.WORKER.get(), center.offset(5, 1, 2));
        spawn(level, ModEntities.GUARD.get(), center.offset(0, 1, -5));
    }

    private static void buildHouse(ServerLevel level, BlockPos base) {
        BlockState wall = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            level.setBlock(base.offset(x, 0, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            boolean edge = Math.abs(x) == 3 || Math.abs(z) == 3;
            if (!edge) continue;
            for (int y = 1; y <= 3; y++) {
                boolean corner = Math.abs(x) == 3 && Math.abs(z) == 3;
                BlockState state = corner ? log : wall;
                if (y == 2 && ((x == 0 && z == 3) || (x == -3 && z == 0) || (x == 3 && z == 0))) {
                    if (x == 0 && z == 3) continue;
                    state = Blocks.GLASS_PANE.defaultBlockState();
                }
                level.setBlock(base.offset(x, y, z), state, 3);
            }
        }
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++)
            level.setBlock(base.offset(x, 4, z), Blocks.SPRUCE_SLAB.defaultBlockState(), 3);
        level.setBlock(base.offset(0, 1, 3), Blocks.SPRUCE_DOOR.defaultBlockState(), 3);
        level.setBlock(base.offset(0, 2, 3), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 3);
        level.setBlock(base.offset(-1, 1, -1), Blocks.BARREL.defaultBlockState(), 3);
        level.setBlock(base.offset(1, 1, -1), Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        level.setBlock(base.offset(0, 1, 1), Blocks.RED_BED.defaultBlockState(), 3);
    }

    private static void buildKingdomCastle(ServerLevel level, Player player) {
        BlockPos base = randomSurface(level, player.blockPosition(), 128, 176);
        if (base == null) return;
        for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) {
            boolean wall = Math.abs(x) == 6 || Math.abs(z) == 6;
            if (wall) {
                for (int y = 1; y <= 4; y++) {
                    if (y == 2 && x == 0 && z == 6) continue;
                    level.setBlock(base.offset(x, y, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                }
            }
            if (Math.abs(x) == 6 && Math.abs(z) == 6) {
                for (int y = 5; y <= 8; y++)
                    level.setBlock(base.offset(x, y, z), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 3);
            }
        }
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            for (int y = 1; y <= 5; y++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2)
                    level.setBlock(base.offset(x, y, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            }
        }
        level.setBlock(base.offset(0, 1, 6), Blocks.OAK_DOOR.defaultBlockState(), 3);
        int index = KingdomSavedData.get(level.getServer()).all().size() + 1;
        String kingdomName = "Королевство_" + index;
        KingdomSavedData.get(level.getServer()).createNeutral(kingdomName, base.getX(), base.getZ(), 600);
        spawn(level, ModEntities.KING.get(), base.offset(0, 1, 0));
        spawn(level, ModEntities.LORD.get(), base.offset(2, 1, 0));
        spawn(level, ModEntities.GUARD.get(), base.offset(0, 1, 5));
        spawn(level, ModEntities.GUARD.get(), base.offset(5, 1, 0));
    }

    private static void spawn(ServerLevel level, EntityType<CivilizationNpc> type, BlockPos pos) {
        CivilizationNpc npc = type.create(level);
        if (npc == null) return;
        npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360F, 0F);
        npc.setPersistenceRequired();
        level.addFreshEntity(npc);
    }

    private static BlockPos randomSurface(ServerLevel level, BlockPos origin, int min, int max) {
        int dx = level.random.nextInt(max * 2 + 1) - max;
        int dz = level.random.nextInt(max * 2 + 1) - max;
        while (Math.sqrt((double) dx * dx + (double) dz * dz) < min) {
            dx = level.random.nextInt(max * 2 + 1) - max;
            dz = level.random.nextInt(max * 2 + 1) - max;
        }
        int x = origin.getX() + dx;
        int z = origin.getZ() + dz;
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        if (!level.getBlockState(top).isAir()) return null;
        if (!level.getBlockState(top.below()).isSolidRender(level, top.below())) return null;
        if (level.getFluidState(top).isSource()) return null;
        return top;
    }
}
