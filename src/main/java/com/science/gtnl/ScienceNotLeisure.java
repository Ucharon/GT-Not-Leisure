package com.science.gtnl;

import static com.science.gtnl.ScienceNotLeisure.MODID;
import static com.science.gtnl.ScienceNotLeisure.MODNAME;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.science.gtnl.Utils.enums.Mods;
import com.science.gtnl.Utils.item.MissingMappingsHandler;
import com.science.gtnl.Utils.text.LanguageManager;
import com.science.gtnl.api.TickrateAPI;
import com.science.gtnl.common.command.CommandEnergyNetwork;
import com.science.gtnl.common.command.CommandPlaySound;
import com.science.gtnl.common.command.CommandReloadConfig;
import com.science.gtnl.common.command.CommandSteamNetwork;
import com.science.gtnl.common.command.CommandTickrate;
import com.science.gtnl.common.command.CommandTitle;
import com.science.gtnl.common.machine.multiMachineClasses.EdenGardenManager.EIGBucketLoader;
import com.science.gtnl.config.MainConfig;
import com.science.gtnl.loader.MachineLoader;
import com.science.gtnl.loader.MaterialLoader;
import com.science.gtnl.loader.RecipeLoader;
import com.science.gtnl.loader.RecipeLoaderRunnable;
import com.science.gtnl.loader.ScriptLoader;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(
    modid = MODID,
    version = Tags.VERSION,
    name = MODNAME,
    dependencies = "required-after:IC2;" + "required-after:structurelib;"
        + "required-after:Avaritia;"
        + "after:eternalsingularity;"
        + "required-after:AWWayofTime;"
        + "required-after:BloodArsenal;"
        + "required-after:modularui;"
        + "required-after:Botania;"
        + "after:GalacticraftCore;"
        + "required-after:bartworks;"
        + "after:miscutils;"
        + "required-after:dreamcraft;"
        + "after:GalacticraftMars;"
        + "required-after:gregtech;"
        + "before:TwistSpaceTechnology;"
        + "after:GalacticraftPlanets",
    guiFactory = "com.science.gtnl.config.ConfigGuiFactory",
    acceptedMinecraftVersions = "1.7.10")
public class ScienceNotLeisure {

    @Mod.Instance(Mods.ModIds.SCIENCENOTLEISURE)
    public static ScienceNotLeisure instance;
    public static final String MODID = Mods.ModIds.SCIENCENOTLEISURE;
    public static final String MODNAME = "GTNotLeisure";
    public static final String VERSION = Tags.VERSION;
    public static final String Arthor = "HFstudio";
    public static final String RESOURCE_ROOT_ID = Mods.ModIds.SCIENCENOTLEISURE;
    public static final Logger LOG = LogManager.getLogger(MODID);

    public static SimpleNetworkWrapper network;

    @SidedProxy(clientSide = "com.science.gtnl.ClientProxy", serverSide = "com.science.gtnl.CommonProxy")
    public static CommonProxy proxy;

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        proxy.preInit(event);
        MaterialLoader.loadPreInit();
        LanguageManager.init();

        new RecipeLoaderRunnable().run();
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        MachineLoader.registerGlasses();
        TickrateAPI.changeTickrate(MainConfig.defaultTickrate);
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        EIGBucketLoader.LoadEIGBuckets();
        MaterialLoader.loadPostInit();
        MachineLoader.run();
    }

    @Mod.EventHandler
    public void completeInit(FMLLoadCompleteEvent event) {
        // ScriptLoader.run(); // Moved to BartworkLoad_Head_Mixin
        // RecipeLoader.loadRecipesCompleteInit(); // Moved to BartworkLoad_Head_Mixin
    }

    // register server commands in this event handler (Remove if not needed)
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandReloadConfig());
        event.registerServerCommand(new CommandTitle());
        event.registerServerCommand(new CommandTickrate());
        event.registerServerCommand(new CommandSteamNetwork());
        event.registerServerCommand(new CommandEnergyNetwork());
        event.registerServerCommand(new CommandPlaySound());
    }

    @Mod.EventHandler
    public void onMissingMappings(FMLMissingMappingsEvent event) {
        MissingMappingsHandler.handleMappings(event.getAll());
    }
}
