package dev.mangojellypudding.files_njs;

import com.tkisor.nekojs.api.NekoJSPlugin;
import com.tkisor.nekojs.api.annotation.RegisterNekoJSPlugin;
import com.tkisor.nekojs.api.data.Binding;
import com.tkisor.nekojs.api.data.BindingsRegister;
import com.tkisor.nekojs.api.data.JSTypeAdapterRegister;
import com.tkisor.nekojs.api.event.EventGroupRegistry;
import com.tkisor.nekojs.api.recipe.RecipeNamespaceRegister;
import dev.mangojellypudding.files_njs.nekojs.FileEvents;
import dev.mangojellypudding.files_njs.nekojs.FilesWrapper;
import lombok.NoArgsConstructor;

@RegisterNekoJSPlugin
@NoArgsConstructor
public class FilesNJSPlugin implements NekoJSPlugin {
    @Override
    public void registerBindings(BindingsRegister registry) {
        registry.register(Binding.of("FilesNJS", new FilesWrapper()));
    }

    @Override
    public void registerAdapters(JSTypeAdapterRegister registry) {
    }

    @Override
    public void registerRecipeNamespaces(RecipeNamespaceRegister registry) {
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(FileEvents.GROUP);
    }
}
