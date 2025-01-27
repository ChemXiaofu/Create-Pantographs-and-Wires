package de.mrjulsen.paw.fabric.client;

import de.mrjulsen.paw.fabric.client.model.loaders.MultipartObjLoader;
import io.github.fabricators_of_create.porting_lib.models.geometry.RegisterGeometryLoadersCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public class ClientSetup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(MultipartObjLoader.INSTANCE);
        RegisterGeometryLoadersCallback.EVENT.register(loaders -> loaders.put(MultipartObjLoader.ID, MultipartObjLoader.INSTANCE));
    }
}
