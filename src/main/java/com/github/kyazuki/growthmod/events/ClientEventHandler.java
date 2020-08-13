package com.github.kyazuki.growthmod.events;

import com.github.kyazuki.growthmod.GrowthMod;
import com.github.kyazuki.growthmod.capabilities.ScaleProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GrowthMod.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
  @SubscribeEvent
  public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
    float scale = event.getPlayer().getCapability(ScaleProvider.SCALE_CAP).orElseThrow(IllegalArgumentException::new).getScale();
    event.getMatrixStack().push();
    event.getMatrixStack().scale(1.0f, scale, 1.0f);
  }

  @SubscribeEvent
  public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
    event.getMatrixStack().pop();
  }
}
