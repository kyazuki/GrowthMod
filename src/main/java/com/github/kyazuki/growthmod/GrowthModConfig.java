package com.github.kyazuki.growthmod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = GrowthMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GrowthModConfig {
  public static final CommonConfig COMMON;
  public static final ForgeConfigSpec COMMON_SPEC;

  static {
    final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
    COMMON_SPEC = specPair.getRight();
    COMMON = specPair.getLeft();
  }

  public static double defaultHeight;
  public static double heightByDistance;
  public static double food_modifier;
  public static boolean count_food;
  public static boolean change_eyeheight;
  public static boolean change_hitbox;
  public static boolean enable_jump_boost;

  @SubscribeEvent
  public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
    if (configEvent.getConfig().getSpec() == GrowthModConfig.COMMON_SPEC) {
      bakeConfig();
    }
  }

  public static void bakeConfig() {
    defaultHeight = COMMON.defaultHeight.get();
    heightByDistance = COMMON.heightByDistance.get();
    food_modifier = COMMON.food_modifier.get();
    count_food = COMMON.count_food.get();
    change_eyeheight = COMMON.change_eyeheight.get();
    change_hitbox = COMMON.change_hitbox.get();
    enable_jump_boost = COMMON.enable_jump_boost.get();
  }

  public static class CommonConfig {
    public final ForgeConfigSpec.DoubleValue defaultHeight;
    public final ForgeConfigSpec.DoubleValue heightByDistance;
    public final ForgeConfigSpec.DoubleValue food_modifier;
    public final ForgeConfigSpec.BooleanValue count_food;
    public final ForgeConfigSpec.BooleanValue change_eyeheight;
    public final ForgeConfigSpec.BooleanValue change_hitbox;
    public final ForgeConfigSpec.BooleanValue enable_jump_boost;

    public CommonConfig(ForgeConfigSpec.Builder builder) {
      builder.push("GrowthMod Config");
      defaultHeight = builder
              .comment("Player's default height.")
              .translation(GrowthMod.MODID + ".config." + "defaultHeight")
              .defineInRange("defaultHeight", 0.6d, 0.0d, 1000.0d);
      heightByDistance = builder
              .comment("Player's height increment by block.")
              .translation(GrowthMod.MODID + ".config." + "heightByDistance")
              .defineInRange("heightByDistance", 0.001d, 0.0d, 1000.0d);
      food_modifier = builder
              .comment("Player's height increment by hunger.")
              .translation(GrowthMod.MODID + ".config." + "food_modifier")
              .defineInRange("food_modifier", 0.025d, 0.0d, 10.0d);
      count_food = builder
              .comment("Players grow when they eat foods.")
              .translation(GrowthMod.MODID + ".config" + "count_food")
              .define("count_food", true);
      change_eyeheight = builder
              .comment("Whether Player's eyeheight is changed.")
              .translation(GrowthMod.MODID + ".config" + "change_eyeheight")
              .define("change_eyeheight", true);
      change_hitbox = builder
              .comment("Whether Player's hitbox is changed.")
              .translation(GrowthMod.MODID + ".config" + "change_hitbox")
              .define("change_hitbox", true);
      enable_jump_boost = builder
              .comment("Whether player has jump boost effect.")
              .translation(GrowthMod.MODID + ".config" + "enable_jump_boost")
              .define("enable_jump_boost", true);
      builder.pop();
    }
  }
}
