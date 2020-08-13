package com.github.kyazuki.growthmod.events;

import com.github.kyazuki.growthmod.GrowthMod;
import com.github.kyazuki.growthmod.GrowthModConfig;
import com.github.kyazuki.growthmod.capabilities.IScale;
import com.github.kyazuki.growthmod.capabilities.ScaleProvider;
import com.github.kyazuki.growthmod.network.CapabilityPacket;
import com.github.kyazuki.growthmod.network.PacketHandler;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = GrowthMod.MODID)
public class CommonEventHandler {
  public static final ResourceLocation SCALE_CAP_RESOURCE = new ResourceLocation(GrowthMod.MODID, "capabilities");
  public static final UUID ReachDistance = UUID.fromString("76132528-e511-4bc2-8fd5-8727a5aab314");
  private static final EntitySize DEFAULT_STANDING_SIZE = EntitySize.flexible(0.6F, 1.8F);
  private static final Map<Pose, EntitySize> DEFAULT_SIZE_BY_POSE = ImmutableMap.<Pose, EntitySize>builder().put(Pose.STANDING, PlayerEntity.STANDING_SIZE).put(Pose.SLEEPING, EntitySize.fixed(0.2F, 0.2F)).put(Pose.FALL_FLYING, EntitySize.flexible(0.6F, 0.6F)).put(Pose.SWIMMING, EntitySize.flexible(0.6F, 0.6F)).put(Pose.SPIN_ATTACK, EntitySize.flexible(0.6F, 0.6F)).put(Pose.CROUCHING, EntitySize.flexible(0.6F, 1.5F)).put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F)).build();

  // Utils

  public static IScale getCap(PlayerEntity player) {
    return player.getCapability(ScaleProvider.SCALE_CAP).orElseThrow(IllegalArgumentException::new);
  }

  public static EntitySize getScaledPlayerSize(PlayerEntity player, Pose poseIn) {
    if (GrowthModConfig.change_hitbox) {
      float scale;
      try {
        scale = getCap(player).getScale();
      } catch (IllegalArgumentException e) {
        scale = 1.0f;
      } catch (Exception e) {
        throw e;
      }

      switch (poseIn) {
        case STANDING:
          return EntitySize.flexible(0.6F, 1.8F * scale);
        case SLEEPING:
          return EntitySize.fixed(0.2F, 0.2F);
        case CROUCHING:
          return EntitySize.flexible(0.6F, 1.5F * scale);
        case DYING:
          return EntitySize.flexible(0.2F, 0.2F);
        default:
          return EntitySize.flexible(0.6F, 0.6F);
      }
    }
    return DEFAULT_SIZE_BY_POSE.getOrDefault(poseIn, DEFAULT_STANDING_SIZE);
  }

  public static float getScaledStandingEyeHeight(PlayerEntity player, Pose poseIn) {
    float scale;
    try {
      scale = getCap(player).getScale();
    } catch (IllegalArgumentException e) {
      scale = 1.0f;
    } catch (Exception e) {
      throw e;
    }

    switch (poseIn) {
      case SWIMMING:
      case FALL_FLYING:
      case SPIN_ATTACK:
        return 0.4F;
      case CROUCHING:
        return 1.27F * scale;
      default:
        return 1.62F * scale;
    }
  }

  // Server Only

  @SubscribeEvent
  public static void calc(TickEvent.PlayerTickEvent event) {
    if (!event.player.getEntityWorld().isRemote() && event.player.isAlive()) {
      IScale cap = getCap(event.player);
      float distanceWalked = event.player.distanceWalkedModified / 0.6f;
      float prevWalkDistance = cap.getPrevWalkDistance();
      if (distanceWalked > prevWalkDistance) {
        float distance = distanceWalked - prevWalkDistance;
        float prevScale = cap.getScale();
        float scale = (float) (prevScale + distance * GrowthModConfig.heightByDistance);
        cap.setScale(scale);
        MinecraftForge.EVENT_BUS.post(new GrowthModEvents.ChangedScaleEvent(event.player, scale));
        MinecraftForge.EVENT_BUS.post(new GrowthModEvents.UpdatePlayerSizeEvent(event.player));
        PacketHandler.sendToTrackersAndSelf(new CapabilityPacket(event.player.getEntityId(), scale), event.player);
        cap.setPrevWalkDistance(distanceWalked);
      }
    }
  }

  @SubscribeEvent
  public static void setHealthPlayer(GrowthModEvents.ChangedScaleEvent event) {
    if (GrowthModConfig.change_eyeheight && !event.getPlayer().getEntityWorld().isRemote()) {
      PlayerEntity player = event.getPlayer();
      float scale = event.getScale();
      if (player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() != scale * 5.0d) {
        ModifiableAttributeInstance player_reach_distance = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
        player_reach_distance.removeModifier(ReachDistance);
        player_reach_distance.applyNonPersistentModifier(new AttributeModifier(ReachDistance, "TallReachDistance", scale - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
      }
    }
  }

  @SubscribeEvent
  public static void onEat(LivingEntityUseItemEvent.Finish event) {
    if (GrowthModConfig.count_food && !event.getEntity().getEntityWorld().isRemote()) {
      if (event.getEntity() instanceof PlayerEntity) {
        PlayerEntity player = (PlayerEntity) event.getEntity();
        if (event.getItem().getItem().isFood()) {
          float food_heal = event.getItem().getItem().getFood().getHealing() * (float) GrowthModConfig.food_modifier;
          IScale cap = getCap(player);
          float scale = cap.getScale() + food_heal;
          cap.setScale(scale);
          MinecraftForge.EVENT_BUS.post(new GrowthModEvents.ChangedScaleEvent(player, scale));
          MinecraftForge.EVENT_BUS.post(new GrowthModEvents.UpdatePlayerSizeEvent(player));
          PacketHandler.sendToTrackersAndSelf(new CapabilityPacket(player.getEntityId(), scale), player);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onLoggedInPlayer(PlayerEvent.PlayerLoggedInEvent event) {
    PlayerEntity player = event.getPlayer();
    IScale cap = getCap(player);
    PacketHandler.sendTo(new CapabilityPacket(player.getEntityId(), cap.getScale()), player);
    cap.setPrevWalkDistance(0.0f);
  }

  @SubscribeEvent
  public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    PlayerEntity player = event.getPlayer();
    PacketHandler.sendTo(new CapabilityPacket(player.getEntityId(), getCap(player).getScale()), player);
  }

  @SubscribeEvent
  public static void onRespawnPlayer(PlayerEvent.PlayerRespawnEvent event) {
    PlayerEntity player = event.getPlayer();

    if (!event.isEndConquered()) {
      player.getAttribute(ForgeMod.REACH_DISTANCE.get()).removeModifier(ReachDistance);
    } else {
      PacketHandler.sendTo(new CapabilityPacket(player.getEntityId(), getCap(player).getScale()), player);
    }
  }

  @SubscribeEvent
  public static void onStartTracking(PlayerEvent.StartTracking event) {
    if (!(event.getTarget() instanceof PlayerEntity)) return;

    PlayerEntity trackedPlayer = (PlayerEntity) event.getTarget();
    PacketHandler.sendTo(new CapabilityPacket(trackedPlayer.getEntityId(), getCap(trackedPlayer).getScale()), event.getPlayer());
  }

  @SubscribeEvent
  public static void onClonePlayer(PlayerEvent.Clone event) {
    if (event.isWasDeath()) return;

    PlayerEntity newPlayer = event.getPlayer();
    PlayerEntity oldPlayer = event.getOriginal();
    IScale newCap = getCap(newPlayer);
    IScale oldCap = getCap(oldPlayer);
    newCap.copy(oldCap);
    newCap.setPrevWalkDistance(0.0f);
    newPlayer.setHealth(oldPlayer.getHealth());
  }

  // Server & Client

  @SubscribeEvent
  public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
    if (!(event.getObject() instanceof PlayerEntity)) return;

    event.addCapability(SCALE_CAP_RESOURCE, new ScaleProvider());
  }

  @SubscribeEvent
  public static void setPlayerHitbox(GrowthModEvents.UpdatePlayerSizeEvent event) {
    if (GrowthModConfig.change_hitbox)
      event.getPlayer().recalculateSize();
  }

  @SubscribeEvent
  public static void onJumpPlayer(LivingEvent.LivingJumpEvent event) {
    if (GrowthModConfig.enable_jump_boost && event.getEntity() instanceof PlayerEntity) {
      PlayerEntity player = (PlayerEntity) event.getEntity();
      float scale = getCap(player).getScale();
      int amplifier = MathHelper.clamp((int) (1.8f * (scale - 1.0f)), 0, 5);
      player.setMotion(player.getMotion().x, player.getMotion().y * (1.0f + 0.5f * amplifier), player.getMotion().z);
    }
  }

  @SubscribeEvent
  public static void onFall(LivingFallEvent event) {
    if (GrowthModConfig.enable_jump_boost && event.getEntity() instanceof PlayerEntity) {
      float scale = getCap((PlayerEntity) event.getEntity()).getScale();
      float fallDistance = event.getDistance() - 1.8f * (scale - 1.0f);
      if (fallDistance < 0) fallDistance = 0.0f;
      event.setDistance(fallDistance);
    }
  }
}
