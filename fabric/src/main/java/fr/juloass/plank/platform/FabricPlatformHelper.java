package fr.juloass.plank.platform;

import fr.juloass.plank.Constants;
import fr.juloass.plank.ItemDefinition;
import fr.juloass.plank.ItemGroupDefinition;
import fr.juloass.plank.PlankMod;
import fr.juloass.plank.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public File getResourcesFolder() {
        return new File(FabricLoader.getInstance().getGameDir().toFile(), "config/openloader/resources");
    }

    @Override
    public File getLocalVersionFile() {
        return new File(FabricLoader.getInstance().getGameDir().toFile(), "local_version.json");
    }

    @Override
    public File getCacheFolder() {
        return new File(FabricLoader.getInstance().getGameDir().toFile(), "cache");
    }

    @Override
    public void createItem(ItemDefinition itemDefinition) {
        Constants.LOG.info("Creating item " + itemDefinition.getDomain() + ":" + itemDefinition.getName());
        PlankMod.createItem(itemDefinition);
    }

    @Override
    public void createItemGroup(ItemGroupDefinition itemGroupDefinition) {
        Constants.LOG.info("Creating item group " + itemGroupDefinition.getName());
        PlankMod.createItemGroup(itemGroupDefinition);
    }
}
