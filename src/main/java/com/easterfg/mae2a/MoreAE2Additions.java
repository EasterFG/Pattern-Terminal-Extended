package com.easterfg.mae2a;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.easterfg.mae2a.api.MainCreativeMod;
import com.easterfg.mae2a.api.register.*;
import com.easterfg.mae2a.client.MoreAE2AdditionsClient;
import com.easterfg.mae2a.config.MAE2AConfig;
import com.easterfg.mae2a.integration.appflux.AppFluxCommonLoad;
import com.easterfg.mae2a.integration.eae.EAECommonLoad;
import com.easterfg.mae2a.integration.wt.WTCommonLoad;
import com.easterfg.mae2a.util.Platform;

/**
 * @author EasterFG on 2024/9/17
 */
@Mod(MoreAE2Additions.MOD_ID)
public class MoreAE2Additions {
    public static final String MOD_ID = "mae2a";
    public static final Logger LOGGER = LogManager.getLogger();

    public static MoreAE2Additions INSTANCE;

    public MoreAE2Additions() {
        INSTANCE = this;
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MAE2AConfig.build());
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modEventBus.register(MoreAE2AdditionsClient.INSTANCE));
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
                MainCreativeMod.init(event.getVanillaRegistry());
            }

            if (!event.getRegistryKey().equals(Registries.BLOCK)) {
                return;
            }

            ModRegisterHandler.initBlock(ForgeRegistries.BLOCKS);
            ModRegisterHandler.initItem(ForgeRegistries.ITEMS);
            ModRegisterHandler.initBlockEntity(ForgeRegistries.BLOCK_ENTITY_TYPES);
            MenuTypeRegister.init(ForgeRegistries.MENU_TYPES);
        });
        ModRegisterHandler.init();
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        NetworkRegister.register();
        CellRegister.register();
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public void clientSetup(FMLClientSetupEvent event) {
        MoreAE2AdditionsClient.INSTANCE.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::postRegistrationInitialization).whenComplete((res, err) -> {
            if (err != null) {
                LOGGER.warn(err);
            }
        });
    }

    private void postRegistrationInitialization() {
        UpgradesInit.init();
        if (Platform.isModLoaded("ae2wtlib")) {
            WTCommonLoad.init();
        }

        if (Platform.isModLoaded("expatternprovider")) {
            EAECommonLoad.init();
        }

        if (Platform.isModLoaded("appflux")) {
            AppFluxCommonLoad.init();
        }
    }
}
