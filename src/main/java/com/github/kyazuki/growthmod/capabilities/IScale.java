package com.github.kyazuki.growthmod.capabilities;

public interface IScale {
  void setScale(float value);

  float getScale();

  void setPrevWalkDistance(float value);

  float getPrevWalkDistance();

  void copy(IScale cap);
}
