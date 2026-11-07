package ru.livingcivilizations.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A low, walkable road tile instead of a full-height cube. */
public class RoadBlock extends Block {
    private static final VoxelShape ROAD_SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);

    public RoadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ROAD_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ROAD_SHAPE;
    }
}
