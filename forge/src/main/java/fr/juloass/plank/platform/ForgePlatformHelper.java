package fr.juloass.plank.platform;

import fr.juloass.plank.Constants;
import fr.juloass.plank.ItemDefinition;
import fr.juloass.plank.ItemGroupDefinition;
import fr.juloass.plank.PlankMod;
import fr.juloass.plank.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.File;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public File getResourcesFolder() {
        return new File(FMLLoader.getGamePath().toFile(), "config/openloader/resources");
    }

    @Override
    public File getLocalVersionFile() {
        return new File(FMLLoader.getGamePath().toFile(), "local_version.json");
    }

    @Override
    public File getCacheFolder() {
        return new File(FMLLoader.getGamePath().toFile(), "cache");
    }

    @Override
    public void createItem(ItemDefinition itemDefinition) {
        Constants.LOG.info(getPlatformName() + "Creating item " + itemDefinition.getDomain() + ":" + itemDefinition.getName());
        PlankMod.createItem(itemDefinition);
    }

    @Override
    public void createItemGroup(ItemGroupDefinition itemGroupDefinition) {
        // TODO: Implement this
    }
}