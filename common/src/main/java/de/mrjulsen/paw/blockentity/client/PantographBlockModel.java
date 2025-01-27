package de.mrjulsen.paw.blockentity.client;

import de.mrjulsen.paw.PantographsAndWires;
import de.mrjulsen.paw.blockentity.PantographBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.model.GeoModel;

public class PantographBlockModel extends GeoModel<PantographBlockEntity> {

    public PantographBlockModel() {        
        MolangParser.INSTANCE.setMemoizedValue("query.func_start", () -> {
            return PantographBlockEntity.START_ANGLE;
        });
        MolangParser.INSTANCE.setMemoizedValue("query.func2", () -> {
            return PantographBlockEntity.BASE_ANGLE;
        });
        MolangParser.INSTANCE.setMemoizedValue("query.max_height", () -> {
            return PantographBlockEntity.DELTA_HEIGHT_PIXELS;
        });
    }

    @Override
    public ResourceLocation getModelResource(PantographBlockEntity animatable) {
        return new ResourceLocation(PantographsAndWires.MOD_ID, "geo/block/pantograph.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PantographBlockEntity animatable) {
        return new ResourceLocation(PantographsAndWires.MOD_ID, "textures/block/pantograph.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PantographBlockEntity animatable) {
        return new ResourceLocation(PantographsAndWires.MOD_ID, "animations/block/pantograph.animation.json");
    }    

    @Override
    public void applyMolangQueries(PantographBlockEntity animatable, double animTime) {        
        super.applyMolangQueries(animatable, animTime);
        animatable.applyMolangVariables();
    }
}
