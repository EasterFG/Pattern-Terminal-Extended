package com.easterfg.mae2a.integration.appflux;

import com.glodblock.github.appflux.common.AFItemAndBlock;
import com.glodblock.github.appflux.util.AFUtil;
import com.glodblock.github.appflux.util.helpers.INeighborListener;

import net.minecraft.core.BlockPos;

import appeng.api.upgrades.Upgrades;
import appeng.helpers.patternprovider.PatternProviderLogic;

import com.easterfg.mae2a.common.definition.MAE2ABlocks;
import com.easterfg.mae2a.common.definition.MAE2AParts;

import lombok.Getter;

public class AppFluxCommonLoad {

    @Getter
    private static boolean load = false;

    public static void init() {
        load = true;
        Upgrades.add(AFItemAndBlock.INDUCTION_CARD, MAE2AParts.PATTERN_PROVIDER_PLUS, 1,
                "gui.mae2a.pattern_provider_plus");
        Upgrades.add(AFItemAndBlock.INDUCTION_CARD, MAE2ABlocks.PATTERN_PROVIDER_PLUS, 1,
                "gui.mae2a.pattern_provider_plus");
    }

    public static void notifyNeighbor(PatternProviderLogic logic, BlockPos pos, BlockPos fromPos) {
        AFUtil.notifyNeighbor((INeighborListener) logic, pos, fromPos);
    }

}
