package mcjty.deepresonance.proxy;

import mcjty.deepresonance.DeepResonance;
import mcjty.deepresonance.ForgeEventHandlers;
import mcjty.deepresonance.blocks.ModBlocks;
import mcjty.deepresonance.blocks.generator.GeneratorConfiguration;
import mcjty.deepresonance.blocks.laser.LaserBonusConfiguration;
import mcjty.deepresonance.fluid.DRFluidRegistry;
import mcjty.deepresonance.gui.GuiProxy;
import mcjty.deepresonance.items.ModItems;
import mcjty.deepresonance.network.DRMessages;
import mcjty.deepresonance.radiation.RadiationConfiguration;
import mcjty.deepresonance.radiation.RadiationTickEvent;
import mcjty.deepresonance.radiation.SuperGenerationConfiguration;
import mcjty.deepresonance.worldgen.WorldGen;
import mcjty.deepresonance.worldgen.WorldGenConfiguration;
import mcjty.deepresonance.worldgen.WorldTickHandler;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.proxy.AbstractCommonProxy;
import mcjty.lib.varia.WrenchChecker;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Level;

public abstract class CommonProxy extends AbstractCommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());

        mainConfig = DeepResonance.config;
        readMainConfig();

        SimpleNetworkWrapper network = PacketHandler.registerMessages(DeepResonance.MODID, "deepresonance");
        DRMessages.registerNetworkMessages(network);

        ModItems.init();
        ModBlocks.init();
        WorldGen.init();
        DRFluidRegistry.initFluids();
    }

    private void readMainConfig() {
        Configuration cfg = mainConfig;
        try {
            cfg.load();

            cfg.addCustomCategoryComment(WorldGenConfiguration.CATEGORY_WORLDGEN, "Configuration for worldgen");
            cfg.addCustomCategoryComment(GeneratorConfiguration.CATEGORY_GENERATOR, "Configuration for the generator multiblock");
            cfg.addCustomCategoryComment(RadiationConfiguration.CATEGORY_RADIATION, "Configuration for the radiation");
            cfg.addCustomCategoryComment(LaserBonusConfiguration.CATEGORY_LASERBONUS, "Configuration for the laser bonuses");
            cfg.addCustomCategoryComment(SuperGenerationConfiguration.CATEGORY_SUPERGEN, "Configuration for super power generation (using pulser)");
            WorldGenConfiguration.init(cfg);
            GeneratorConfiguration.init(cfg);
            RadiationConfiguration.init(cfg);
            LaserBonusConfiguration.init(cfg);
            SuperGenerationConfiguration.init(cfg);
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        } finally {
            if (mainConfig.hasChanged()) {
                mainConfig.save();
            }
        }
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        NetworkRegistry.INSTANCE.registerGuiHandler(DeepResonance.instance, new GuiProxy());
        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance);
        MinecraftForge.EVENT_BUS.register(new RadiationTickEvent());
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
        mainConfig = null;
        WrenchChecker.init();
    }

    public abstract void throwException(Exception e, int i);

}
