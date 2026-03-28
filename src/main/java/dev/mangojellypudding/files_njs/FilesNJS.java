package dev.mangojellypudding.files_njs;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(FilesNJS.MODID)
public class FilesNJS {
    public static final String MODID = "files_njs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FilesNJS(IEventBus modEventBus, ModContainer modContainer) {}
}
