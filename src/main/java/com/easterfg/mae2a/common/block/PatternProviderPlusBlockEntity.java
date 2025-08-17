package com.easterfg.mae2a.common.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.stacks.AEItemKey;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.crafting.PatternProviderBlockEntity;

import com.easterfg.mae2a.common.definition.MAE2ABlocks;
import com.easterfg.mae2a.common.menu.host.PatternProviderPlusLogicHost;
import com.easterfg.mae2a.common.menu.logic.PatternProviderPlusLogic;
import com.easterfg.mae2a.config.MAE2AConfig;

/**
 * @author EasterFG on 2025/4/3
 */
public class PatternProviderPlusBlockEntity extends PatternProviderBlockEntity
        implements IUpgradeableObject, PatternProviderPlusLogicHost {

    private final IUpgradeInventory upgrades;

    public PatternProviderPlusBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.upgrades = UpgradeInventories.forMachine(MAE2ABlocks.PATTERN_PROVIDER_PLUS, 2, this::saveChanges);
    }

    protected PatternProviderPlusLogic createLogic() {
        return new PatternProviderPlusLogic(this.getMainNode(), this, MAE2AConfig.plusMaxSlot);
    }

    @Override
    public PatternProviderPlusLogic getLogic() {
        return (PatternProviderPlusLogic) this.logic;
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.upgrades.writeToNBT(data, "upgrades");
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.upgrades.readFromNBT(data, "upgrades");
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return MAE2ABlocks.PATTERN_PROVIDER_PLUS.stack(1);
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(MAE2ABlocks.PATTERN_PROVIDER_PLUS.stack(1));
    }

    @Override
    public void clearContent() {
        super.clearContent();
        upgrades.clear();
    }
}
