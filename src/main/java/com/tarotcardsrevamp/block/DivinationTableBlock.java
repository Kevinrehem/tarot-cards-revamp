package com.tarotcardsrevamp.block;

import com.tarotcardsrevamp.block.menu.DivinationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DivinationTableBlock extends Block {

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    private static final Component CONTAINER_TITLE = Component.translatable("container.tarotcardsrevamp.divination_table");

    public DivinationTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInv, p) -> new DivinationMenu(containerId, playerInv, ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE
        ));
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInv, p) -> new DivinationMenu(containerId, playerInv, ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE
        ));
        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        for (int i = 0; i < 2; i++) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 1.2D;
            double y = pos.getY() + 0.75D + random.nextDouble() * 0.5D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 1.2D;
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.05D, 0.0D);
        }
    }
}
