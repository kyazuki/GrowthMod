package com.github.kyazuki.growthmod.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class GrowthModEvents {
  public static class ChangedScaleEvent extends PlayerEvent {
    private final float scale;

    public ChangedScaleEvent(PlayerEntity player, float scale) {
      super(player);
      this.scale = scale;
    }

    public float getScale() {
      return scale;
    }
  }

  public static class UpdatePlayerSizeEvent extends PlayerEvent {
    public UpdatePlayerSizeEvent(PlayerEntity player) {
      super(player);
    }
  }
}
