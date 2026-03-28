package dev.mangojellypudding.nekojs_addon_example.nekojs;

import com.tkisor.nekojs.api.NekoJSPlugin;
import com.tkisor.nekojs.api.annotation.RegisterNekoJSPlugin;
import com.tkisor.nekojs.api.data.BindingsRegister;
import com.tkisor.nekojs.api.data.JSTypeAdapterRegister;
import com.tkisor.nekojs.api.event.EventGroupRegistry;
import com.tkisor.nekojs.api.recipe.RecipeNamespaceRegister;
import lombok.NoArgsConstructor;

@RegisterNekoJSPlugin
@NoArgsConstructor
public class NekoJSAddonExamplePlugin implements NekoJSPlugin {
    @Override
    public void registerBindings(BindingsRegister registry) {
    }

    @Override
    public void registerAdapters(JSTypeAdapterRegister registry) {
    }

    @Override
    public void registerRecipeNamespaces(RecipeNamespaceRegister registry) {
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
    }
}
