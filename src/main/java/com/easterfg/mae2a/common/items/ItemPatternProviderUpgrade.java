package com.easterfg.mae2a.common.items;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.items.parts.PartItem;
import appeng.parts.AEBasePart;

import com.easterfg.mae2a.common.block.PatternProviderPlusBlockEntity;
import com.easterfg.mae2a.common.definition.MAE2ABlockEntities;
import com.easterfg.mae2a.common.definition.MAE2ABlocks;
import com.easterfg.mae2a.common.definition.MAE2AParts;
import com.easterfg.mae2a.common.menu.host.PatternProviderPlusLogicHost;
import com.easterfg.mae2a.config.MAE2AConfig;

public class ItemPatternProviderUpgrade extends Item {
    public ItemPatternProviderUpgrade(Properties pProperties) {
        super(pProperties);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        var level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        var te = level.getBlockEntity(clickedPos);
        if (te == null) {
            return InteractionResult.PASS;
        }
        var ctx = new BlockPlaceContext(context);
        if (te instanceof PatternProviderBlockEntity tile && !(tile instanceof PatternProviderPlusBlockEntity)) {
            var origin = level.getBlockState(clickedPos);
            var location = ForgeRegistries.BLOCKS.getKey(origin.getBlock());
            if (notWhiteList(location)) {
                return InteractionResult.PASS;
            }

            var state = MAE2ABlocks.PATTERN_PROVIDER_PLUS.asBlock().getStateForPlacement(ctx);
            if (state == null) {
                return InteractionResult.PASS;
            }
            for (var entry : origin.getValues().entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                try {
                    if (state.hasProperty(key)) {
                        state = state.<Comparable, Comparable>setValue((Property) key, value);
                    }
                } catch (Exception ignore) {
                }
            }
            BlockEntity be = MAE2ABlockEntities.PATTERN_PROVIDER_PLUS_BLOCK_ENTITY.create(clickedPos, state);
            replace(level, clickedPos, te, be, state);
            context.getItemInHand().shrink(1);
            return InteractionResult.CONSUME;
        } else if (te instanceof CableBusBlockEntity cable) {
            var hit = context.getClickLocation();
            Vec3 target = new Vec3(hit.x - clickedPos.getX(), hit.y - clickedPos.getY(), hit.z - clickedPos.getZ());
            var part = cable.getCableBus().selectPartLocal(target).part;
            if (part instanceof AEBasePart pp) {
                var local = ForgeRegistries.ITEMS.getKey(pp.getPartItem().asItem());
                if (notWhiteList(local)) {
                    return InteractionResult.PASS;
                }

                if (!(pp instanceof PatternProviderLogicHost) || pp instanceof PatternProviderPlusLogicHost) {
                    return InteractionResult.PASS;
                }
                var side = pp.getSide();
                var content = new CompoundTag();

                PartItem<?> item = (PartItem<?>) MAE2AParts.PATTERN_PROVIDER_PLUS.asItem();
                pp.writeToNBT(content);
                var place = cable.replacePart(item, side, context.getPlayer(), null);
                if (place != null) {
                    place.readFromNBT(content);
                    place.addToWorld();
                }
                context.getItemInHand().shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean notWhiteList(ResourceLocation location) {
        if (location != null) {
            return !MAE2AConfig.upgradeWhiteList.contains(location.toString());
        }
        return true;
    }

    private void replace(Level level, BlockPos pos, BlockEntity oldTile, BlockEntity newTile, BlockState newBlock) {
        CompoundTag content = oldTile.saveWithFullMetadata();
        level.removeBlockEntity(pos);
        level.removeBlock(pos, false);
        level.setBlockAndUpdate(pos, newBlock);
        level.setBlockEntity(newTile);
        newTile.load(content);
        if (newTile instanceof AEBaseBlockEntity aeTile) {
            aeTile.markForUpdate();
        } else {
            newTile.setChanged();
        }
    }
}
