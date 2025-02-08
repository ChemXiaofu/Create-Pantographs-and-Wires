package de.mrjulsen.paw.event;

import de.mrjulsen.wires.WireNetwork;
import dev.architectury.event.events.common.LifecycleEvent;

public final class ModCommonEvents {

    private ModCommonEvents() {}
    
    public static void init() {
        LifecycleEvent.SERVER_STARTED.register((server) -> {
            WireNetwork.load(server);
        });

        LifecycleEvent.SERVER_LEVEL_SAVE.register((server) -> {
            if (!server.isClientSide) {
                WireNetwork.save(server.getServer());
            }
        });

        LifecycleEvent.SERVER_STOPPED.register((server) -> {
            WireNetwork.clear();
        });
    }
}
