package com.github.kyazuki.growthmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

@Mod(GrowthMod.MODID)
@Mod.EventBusSubscriber
public class GrowthMod {
  public static final String MODID = "growthmod";
  public static final Logger LOGGER = LogManager.getLogger(MODID);
  public static float height = 0.0f;
  public static float prevWalkDistance = 0.0f;
  public static float walkDistance = 0.0f;
  public static float food_heal = 0.0f;

  public GrowthMod() {
    LOGGER.debug("GrowthMod loaded!");
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, GrowthModConfig.CLIENT_SPEC);
  }

  public static void resetPlayer(PlayerEntity player) {
    prevWalkDistance = -player.distanceWalkedModified;
    walkDistance = 0.0f;
    food_heal = 0.0f;
  }

  @SubscribeEvent
  public static void calc(TickEvent.PlayerTickEvent event) {
    if (!event.player.world.isRemote()) {
      walkDistance = prevWalkDistance + event.player.distanceWalkedModified / 0.6f;
      if (GrowthModConfig.count_food)
        height = (float) ((walkDistance * GrowthModConfig.heightByDistance) + food_heal + GrowthModConfig.defaultHeight);
      else
        height = (float) ((walkDistance * GrowthModConfig.heightByDistance) + GrowthModConfig.defaultHeight);
    }
  }

  @SubscribeEvent
  public static void setEyeHeightPlayer(TickEvent.PlayerTickEvent event) {
    Class EntityClass = event.player.getClass();
    Field field = null;
    while (EntityClass != null) {
      try {
        field = EntityClass.getDeclaredField("eyeHeight");
        field.setAccessible(true);
        field.set(event.player, height * 0.85f);
        break;
      } catch (NoSuchFieldException | IllegalAccessException e) {
        EntityClass = EntityClass.getSuperclass();
      }
    }
  }

  @SubscribeEvent
  public static void sethitboxPlayer(TickEvent.PlayerTickEvent event) {
    if (GrowthModConfig.change_hitbox && event.player.isAlive()) {
      AxisAlignedBB playerBoundingBox = event.player.getBoundingBox();
      event.player.setBoundingBox(new AxisAlignedBB(playerBoundingBox.minX, playerBoundingBox.minY, playerBoundingBox.minZ, playerBoundingBox.maxX, playerBoundingBox.minY + height, playerBoundingBox.maxZ));
    }
  }

  @SubscribeEvent
  public static void onEat(LivingEntityUseItemEvent.Finish event) {
    if (GrowthModConfig.count_food && event.getEntity().world.isRemote()) {
      if (event.getEntity() instanceof PlayerEntity) {
        if (event.getItem().getItem().isFood()) {
          food_heal += event.getItem().getItem().getFood().getHealing() * GrowthModConfig.food_modifier;
        }
      }
    }
  }

  @SubscribeEvent
  public static void JumpBoost(TickEvent.PlayerTickEvent event) {
    if (GrowthModConfig.enable_jump_boost && event.player.world.isRemote()) {
      int amplifer = MathHelper.clamp((int) height, 1, 11);
      event.player.addPotionEffect(new EffectInstance(Effects.JUMP_BOOST, 200, amplifer - 1, false, false, true));
    }
  }

  @SubscribeEvent
  public static void onFall(LivingFallEvent event) {
    if (GrowthModConfig.enable_jump_boost && event.getEntity() instanceof PlayerEntity) {
      float fallDistance = event.getDistance() - (height - 1.8f);
      if (fallDistance < 0) fallDistance = 0.0f;
      event.setDistance(fallDistance);
    }
  }

  @SubscribeEvent
  public static void onloginPlayer(PlayerEvent.PlayerLoggedInEvent event) {
    resetPlayer(event.getPlayer());
  }

  @SubscribeEvent
  public static void onrespawnPlayer(PlayerEvent.PlayerRespawnEvent event) {
    resetPlayer(event.getPlayer());
  }

  @SubscribeEvent
  public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
    event.getMatrixStack().push();
    event.getMatrixStack().scale(1.0f, height / 1.8f, 1.0f);
  }

  @SubscribeEvent
  public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
    event.getMatrixStack().pop();
  }
}
