package de.mrjulsen.paw;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;

public final class CrossPlatform {
        
    @ExpectPlatform
    public static void registerConfig() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static double interactionRange(Player player) {
        throw new AssertionError();
    }
}
