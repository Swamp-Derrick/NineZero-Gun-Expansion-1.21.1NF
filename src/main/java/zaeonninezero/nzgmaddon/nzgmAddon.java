package zaeonninezero.nzgmaddon;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import zaeonninezero.nzgmaddon.init.initItems;
import zaeonninezero.nzgmaddon.init.initSounds;
import zaeonninezero.nzgmaddon.init.ModCreativeTabs;
import zaeonninezero.nzgmaddon.crafting.VariantWorkbenchRecipe;

@Mod(nzgmAddon.MOD_ID)
public class nzgmAddon {
    public static final String MOD_ID = "nzgmaddon";

    public nzgmAddon(IEventBus bus) {
        initItems.ITEMS.register(bus);
        initSounds.SOUNDS.register(bus);
        ModCreativeTabs.TABS.register(bus);
        VariantWorkbenchRecipe.SERIALIZERS.register(bus);
    }
}
