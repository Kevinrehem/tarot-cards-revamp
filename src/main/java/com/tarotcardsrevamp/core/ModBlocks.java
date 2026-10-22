package com.tarotcardsrevamp.core;

import com.tarotcardsrevamp.TarotCardsRevampMod;
import com.tarotcardsrevamp.block.DivinationTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TarotCardsRevampMod.MOD_ID);

    public static final DeferredBlock<DivinationTableBlock> DIVINATION_TABLE = BLOCKS.registerBlock(
            "divination_table",
            DivinationTableBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.5F, 6.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );
}
