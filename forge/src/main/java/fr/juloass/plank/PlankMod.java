package fr.juloass.plank;

import com.tterrag.registrate.Registrate;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

@Mod(Constants.MOD_ID)
public class PlankMod {

    private static HashMap<String, Registrate> REGS = new HashMap<>();

    public PlankMod() {
        CommonClass.init();
    }

    public static void createItem(ItemDefinition itemDefinition) {
        Constants.LOG.info("Registering item !");
        String domain = itemDefinition.getDomain();
        Registrate REG = REGS.computeIfAbsent(domain, Registrate::create);
        if(itemDefinition.isAutoGenerate()) {
            switch(itemDefinition.getItemType()) {
                case BASE:
                    REG.item(itemDefinition.getName(), Item::new).defaultLang().defaultModel().register();
                    break;
                case SEQUENCED_ASSEMBLY:
                    REG.item(itemDefinition.getName(), SequencedAssemblyItem::new).defaultLang().defaultModel().register();
                    break;
            }
        }
    }

}