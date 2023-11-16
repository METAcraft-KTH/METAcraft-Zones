package se.datasektionen.mc.zones.zone.types;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

public abstract class ZoneType {

	/**
	 * WARNING, this codec will require RegistryOps!
	 * @param world The world to use when creating the zone.
	 * @return The codec for the zone type.
	 */
	public static Codec<ZoneType> getRegistryCodec(World world) {
		return ZoneRegistry.REGISTRY.getCodec().dispatch(
				ZoneType::getType, zoneType -> zoneType.codec().apply(world)
		);
	}

	protected final World world;

	public ZoneType(World world) {
		this.world = world;
	}

	public abstract boolean contains(BlockPos pos);

	//Estimation of the zone size. Used to make smaller zones have higher priority by default. Does not have to be very accurate.
	public abstract double getSize();

	public abstract ZoneType clone(World otherWorld);

	public abstract ZoneRegistry.ZoneType<?> getType();
}
