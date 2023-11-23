package se.datasektionen.mc.zones;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.datasektionen.mc.zones.compat.CompatMods;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

public class METAcraftZones implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("METAcraft-Zones");

	public static final String MODID = "metacraft-zones";

	@Override
	public void onInitialize() {
		ZoneRegistry.init();
		Commands.registerCommands();
		CompatMods.init();
		LOGGER.info("Loaded METAcraft zones by Acuadragon100");
	}

	public static Identifier getID(String name) {
		return new Identifier(MODID, name);
	}
}
