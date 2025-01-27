package de.mrjulsen.paw.config;

import de.mrjulsen.paw.PantographsAndWires;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    
    public static final ForgeConfigSpec.ConfigValue<Boolean> ADVANCED_BLOCK_SELECTION;

    public static final double MIN_SCALE = 0.25f;
    public static final double MAX_SCALE = 2.0f;

    static {
        BUILDER.push(PantographsAndWires.MOD_ID + "_common_config");
        
        ADVANCED_BLOCK_SELECTION = BUILDER.comment(new String[] {"Improves targeting of blocks when the hitbox is outside the actual block area (e.g. large or diagonal hitboxes). However, this requires a bit more computing power because 9 blocks have to be checked instead of 1.", "Default: ON"})
            .define("advanced.block_selection", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
