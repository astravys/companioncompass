package astravys.companioncompass;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CompanionCompass.MODID)
public class CompanionCompass {
    public static final String MODID = "companioncompass";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompanionCompass(IEventBus modEventBus) {
        modEventBus.addListener(CompanionCompassNetwork::registerPayloads);
    }
}
