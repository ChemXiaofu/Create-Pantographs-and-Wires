package de.mrjulsen.paw.forge.client;

import de.mrjulsen.paw.forge.client.model.loaders.MultipartObjLoader;
import de.mrjulsen.paw.forge.compat.EmbeddiumCompat;
import de.mrjulsen.paw.PantographsAndWires;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Bus.MOD, modid = PantographsAndWires.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void registerGeometryLoaders(FMLClientSetupEvent event) {
        if (PantographsAndWires.isEmbeddiumLoaded()) {
            EmbeddiumCompat.register();
        }
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(RegisterGeometryLoaders event) {
        event.register("multipart_obj", MultipartObjLoader.INSTANCE);
    }
}
