package com.starchitect;

import com.starchitect.config.SConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
//#if forge
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//#endif
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starchitect — un critique d'architecture snob vient noter vos constructions.
 *
 * Mod purement client : sur un serveur dédié il ne fait rien (hormis la mise en scène du test
 * automatique en CI). Les gestionnaires d'événements portent {@code @Mod.EventBusSubscriber}.
 */
@Mod(Starchitect.MOD_ID)
public class Starchitect {

    public static final String MOD_ID = "starchitect";
    public static final Logger LOGGER = LoggerFactory.getLogger("Starchitect");

    //#if neoforge
    //$$ public Starchitect(net.neoforged.bus.api.IEventBus modBus, net.neoforged.fml.ModContainer container) {
    //$$     if (FMLEnvironment.dist == Dist.CLIENT) {
    //$$         container.registerConfig(ModConfig.Type.CLIENT, SConfig.SPEC);
    //$$         com.starchitect.client.ClientBoot.init(modBus);
    //$$     }
    //$$ }
    //#else
    public Starchitect() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SConfig.SPEC);
            com.starchitect.client.ClientBoot.init(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }
    //#endif

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
