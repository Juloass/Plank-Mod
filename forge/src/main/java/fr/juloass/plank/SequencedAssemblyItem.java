/**
 * SequencedAssemblyItem class for the Plank Mod.
 *
 * This class implements a sequenced assembly item, allowing integration with the Create Mod's
 * crafting system. The structure and functionality are from the Create Mod.
 *
 * Credits:
 * - Create Mod: A mod for Minecraft that introduces new and innovative production mechanics.
 * - GitHub Repository: https://github.com/Creators-of-Create/Create
 *
 * License: This code is inspired by and leverages concepts from the Create Mod.
 * Please ensure to respect their licensing agreements when using this code.
 */

package fr.juloass.plank;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SequencedAssemblyItem extends Item {

    public SequencedAssemblyItem(Properties p_41383_) {
        super(p_41383_);
    }
    public float getProgress(ItemStack stack) {
        if (!stack.hasTag())
            return 0;
        CompoundTag tag = stack.getTag();
        if (!tag.contains("SequencedAssembly"))
            return 0;
        return tag.getCompound("SequencedAssembly")
                .getFloat("Progress");
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(getProgress(stack) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return mixColors(0xFF_FFC074, 0xFF_46FFE0, getProgress(stack));
    }

    public static int mixColors(int color1, int color2, float w) {
        int a1 = (color1 >> 24);
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a2 = (color2 >> 24);
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        return
                ((int) (a1 + (a2 - a1) * w) << 24) +
                ((int) (r1 + (r2 - r1) * w) << 16) +
                ((int) (g1 + (g2 - g1) * w) << 8) +
                ((int) (b1 + (b2 - b1) * w) << 0);
    }

}
