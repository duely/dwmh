package com.noobanidus.dwmh.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.noobanidus.dwmh.ConfigHandler;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class ConditionalRecipeFactory implements IConditionFactory {
  @Override
  public BooleanSupplier parse(JsonContext context, JsonObject json) {
    String key = JsonUtils.getString(json, "recipe");

    if (key.toLowerCase().equals("saddle")) {
      return () -> ConfigHandler.enableSaddle;
    }

    throw new JsonParseException("recipeDisable not found!");
  }
}
