package dev.mangojellypudding.files_njs.nekojs;

import com.tkisor.nekojs.bindings.event.NekoEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.MinecraftServer;

@Getter
@RequiredArgsConstructor
public class FileEventJS implements NekoEvent {
    private final String path;
    private final String content;
    private final String type;
    private final MinecraftServer server;
}
