package com.easterfg.mae2a.common.block;

import static appeng.block.crafting.PatternProviderBlock.PUSH_DIRECTION;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.PushDirection;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

import com.easterfg.mae2a.integration.appflux.AppFluxCommonLoad;

/**
 * @author EasterFG on 2025/4/3
 */
@SuppressWarnings("deprecation")
public class PatternProviderPlusBlock extends AEBaseEntityBlock<PatternProviderPlusBlockEntity> {
    public PatternProviderPlusBlock() {
        super(metalProps());
        registerDefaultState(defaultBlockState().setValue(PUSH_DIRECTION, PushDirection.ALL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PUSH_DIRECTION);
    }

    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Block block, @NotNull BlockPos fromPos,
            boolean isMoving) {
        var be = this.getBlockEntity(level, pos);
        if (be != null) {
            be.getLogic().updateRedstoneState();
            if (AppFluxCommonLoad.isLoad()) {
                AppFluxCommonLoad.notifyNeighbor(be.getLogic(), pos, fromPos);
            }
        }
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        if (!level.isClientSide()) {
            if (AppFluxCommonLoad.isLoad()) {
                var be = this.getBlockEntity(level, pos);
                if (be != null) {
                    AppFluxCommonLoad.notifyNeighbor(be.getLogic(), pos, neighbor);
                }
            }
        }
    }

    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player p,
            InteractionHand hand,
            @Nullable ItemStack heldItem, BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            return InteractionResult.PASS;
        }

        if (heldItem != null && InteractionUtil.canWrenchRotate(heldItem)) {
            setSide(level, pos, hit.getDirection());
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        var be = this.getBlockEntity(level, pos);
        if (be != null) {
            if (!level.isClientSide()) {
                be.openMenu(p, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    public void setSide(Level level, BlockPos pos, Direction facing) {
        var currentState = level.getBlockState(pos);
        var pushSide = currentState.getValue(PUSH_DIRECTION).getDirection();

        PushDirection newPushDirection;
        if (pushSide == facing.getOpposite()) {
            newPushDirection = PushDirection.fromDirection(facing);
        } else if (pushSide == facing) {
            newPushDirection = PushDirection.ALL;
        } else if (pushSide == null) {
            newPushDirection = PushDirection.fromDirection(facing.getOpposite());
        } else {
            newPushDirection = PushDirection.fromDirection(Platform.rotateAround(pushSide, facing));
        }

        level.setBlockAndUpdate(pos, currentState.setValue(PUSH_DIRECTION, newPushDirection));
    }
}
