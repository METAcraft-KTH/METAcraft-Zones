package se.datasektionen.mc.zones.zone.data;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import se.datasektionen.mc.zones.METAcraftZones;

public class ZoneDataRegistry {

	public static final Registry<ZoneDataType<?>> REGISTRY = FabricRegistryBuilder.<ZoneDataType<?>>createSimple(
			RegistryKey.ofRegistry(METAcraftZones.getID("data_types"))
	).buildAndRegister();

}
