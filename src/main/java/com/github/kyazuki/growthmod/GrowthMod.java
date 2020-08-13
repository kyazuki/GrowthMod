package com.github.kyazuki.growthmod;

import com.github.kyazuki.growthmod.capabilities.IScale;
import com.github.kyazuki.growthmod.capabilities.Scale;
import com.github.kyazuki.growthmod.capabilities.ScaleStorage;
import com.github.kyazuki.growthmod.network.PacketHandler;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GrowthMod.MODID)
public class GrowthMod {
  public static final String MODID = "growthmod";
  public static final Logger LOGGER = LogManager.getLogger(MODID);

  public GrowthMod() {
    LOGGER.debug("GrowthMod loaded!");
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GrowthModConfig.COMMON_SPEC);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(GrowthMod::onFMLCommonSetup);
    PacketHandler.register();
  }

  public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
    CapabilityManager.INSTANCE.register(IScale.class, new ScaleStorage(), () -> new Scale((float) GrowthModConfig.defaultHeight));
  }
}
