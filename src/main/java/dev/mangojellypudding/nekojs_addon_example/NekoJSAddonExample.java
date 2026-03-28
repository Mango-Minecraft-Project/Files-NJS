package dev.mangojellypudding.nekojs_addon_example;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(NekoJSAddonExample.MODID)
public class NekoJSAddonExample {
    public static final String MODID = "nekojs_addon_example";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NekoJSAddonExample(IEventBus modEventBus, ModContainer modContainer) {}
}
