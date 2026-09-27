package com.mcphoneminesweeper.client;

import com.mcphoneminesweeper.MCphoneMinesweeper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MsSounds {
   private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "mcphone_minesweeper");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLICK = register("click");
   public static final DeferredHolder<SoundEvent, SoundEvent> WIN = register("win");
   public static final DeferredHolder<SoundEvent, SoundEvent> LOSE = register("lose");

   private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
      ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", name);
      return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(loc));
   }

   public static void registerBus(IEventBus modBus) {
      SOUNDS.register(modBus);
   }

   public static void play(SoundEvent event) {
      if (MsConfig.get().sound) {
         Minecraft mc = Minecraft.getInstance();
         ResourceLocation id = event.getLocation();
         mc.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F, 1.0F));
         MCphoneMinesweeper.LOGGER.info("[mcphone_minesweeper] 播放音效 {}", id);
      }
   }
}
