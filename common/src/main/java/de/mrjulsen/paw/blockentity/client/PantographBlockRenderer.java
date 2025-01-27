package de.mrjulsen.paw.blockentity.client;

import de.mrjulsen.paw.blockentity.PantographBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PantographBlockRenderer extends GeoBlockRenderer<PantographBlockEntity> {
    
    public PantographBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new PantographBlockModel());
    }

    
}
