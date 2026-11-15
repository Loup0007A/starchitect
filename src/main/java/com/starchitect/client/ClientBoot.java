package com.starchitect.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Enregistrements côté client sur le bus du mod. */
public final class ClientBoot {

    private ClientBoot() {}

    public static final KeyMapping KEY_INVITE = new KeyMapping("key.starchitect.invite",
        InputConstants.Type.KEYSYM, InputConstants.KEY_J, "key.categories.starchitect");
    public static final KeyMapping KEY_GUIDE = new KeyMapping("key.starchitect.guide",
        InputConstants.Type.KEYSYM, InputConstants.KEY_H, "key.categories.starchitect");

    public static final KeyMapping KEY_GAUGE = new KeyMapping("key.starchitect.gauge",
        InputConstants.Type.KEYSYM, InputConstants.KEY_G, "key.categories.starchitect");

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientBoot::onKeys);
    }

    private static void onKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_INVITE);
        event.register(KEY_GUIDE);
        event.register(KEY_GAUGE);
    }
}
