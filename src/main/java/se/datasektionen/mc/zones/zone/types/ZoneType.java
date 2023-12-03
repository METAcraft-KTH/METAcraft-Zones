package se.datasektionen.mc.zones.zone.types;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import se.datasektionen.mc.zones.zone.Zone;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

public abstract class ZoneType {

	/**
	 * WARNING, this codec will require RegistryOps!
	 * @param zoneRef The zone that this is a type for.
	 * @return The codec for the zone type.
	 */
	public static Codec<ZoneType> REGISTRY_CODEC = ZoneRegistry.REGISTRY.getCodec().dispatch(
			ZoneType::getType, ZoneRegistry.ZoneType::codec
	);

	private Zone zoneRef;

	public void setZoneRef(Zone zone) {
		this.zoneRef = zone;
	}

	public Zone getZoneRef() {
		return zoneRef;
	}

	public abstract boolean contains(BlockPos pos);

	//Estimation of the zone size. Used to make smaller zones have higher priority by default. Does not have to be very accurate.
	public abstract double getSize();

	public abstract ZoneType clone();

	public abstract ZoneRegistry.ZoneType<?> getType();
}
