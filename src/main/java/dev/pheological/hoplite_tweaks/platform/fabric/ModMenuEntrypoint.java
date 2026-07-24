package dev.pheological.hoplite_tweaks.platform.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfigScreen;

public final class ModMenuEntrypoint implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return HopliteTweaksConfigScreen::create;
    }
}
