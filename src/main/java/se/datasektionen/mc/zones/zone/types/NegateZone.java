package se.datasektionen.mc.zones.zone.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import se.datasektionen.mc.zones.zone.Zone;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

public class NegateZone extends ZoneType {

	protected final ZoneType zone;

	public static final Codec<NegateZone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			REGISTRY_CODEC.fieldOf("zone").forGetter(zone -> zone.zone)
	).apply(instance, NegateZone::new));

	public NegateZone(ZoneType zone) {
		this.zone = zone;
		this.setZoneRef(zone.getZoneRef());
	}

	public ZoneType getZone() {
		return zone;
	}

	@Override
	public boolean contains(BlockPos pos) {
		return !zone.contains(pos);
	}

	@Override
	public void setZoneRef(Zone zone) {
		this.zone.setZoneRef(zone);
		super.setZoneRef(zone);
	}

	@Override
	public double getSize() {
		return zone.getSize();
	}

	@Override
	public ZoneType clone() {
		return new NegateZone(zone.clone());
	}

	@Override
	public String toString() {
		return "Negate:[" + zone + "]";
	}

	@Override
	public ZoneRegistry.ZoneType<?> getType() {
		return ZoneRegistry.negate;
	}
}
