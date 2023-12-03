package se.datasektionen.mc.zones.zone.data;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import se.datasektionen.mc.zones.METAcraftZones;

import java.util.Optional;

public class ZoneDataRegistry {

	public static final Registry<ZoneDataType<?>> REGISTRY = FabricRegistryBuilder.<ZoneDataType<?>>createSimple(
			RegistryKey.ofRegistry(METAcraftZones.getID("data_types"))
	).buildAndRegister();


	public static final ZoneDataType<MessageZoneData> MESSAGE = Registry.register(
			REGISTRY, METAcraftZones.getID("message"),
			new ZoneDataType<>(MessageZoneData.CODEC, () -> new MessageZoneData(Optional.empty(), Optional.empty()))
	);

	public static void init() {
		//DO NOT REMOVE!
	}

}
