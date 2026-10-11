package xyz.immortius.museumcurator.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import xyz.immortius.museumcurator.client.screens.MuseumCuratorConfigScreen;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MuseumCuratorConfigScreen::new;
    }
}
