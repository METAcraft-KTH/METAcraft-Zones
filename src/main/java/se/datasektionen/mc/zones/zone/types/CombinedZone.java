package se.datasektionen.mc.zones.zone.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import se.datasektionen.mc.zones.zone.Zone;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CombinedZone extends ZoneType {

	protected final List<ZoneType> zones;
	protected double size;

	public static <T extends CombinedZone> Codec<T> getCodec(Function<List<ZoneType>, T> creator) {
		return RecordCodecBuilder.create(instance -> instance.group(
				REGISTRY_CODEC.listOf().fieldOf("zones").forGetter(zone -> zone.zones)
		).apply(instance, creator));
	}

	public CombinedZone(List<ZoneType> zones) {
		this.zones = zones;
		if (zones.isEmpty()) {
			throw new IllegalStateException("Don't create an empty combined zone!");
		}
		this.setZoneRef(zones.get(0).getZoneRef());
	}

	protected abstract double getSize(Iterable<ZoneType> zones);
	protected abstract double updateSize(ZoneType newZone, UpdateDirection direction);

	public void addZone(ZoneType zone) {
		zones.add(zone);
		size = updateSize(zone, UpdateDirection.ADD);
	}

	public void removeZone(ZoneType zone) {
		zones.add(zone);
		size = updateSize(zone, UpdateDirection.REMOVE);
	}

	public void forEach(Consumer<ZoneType> consumer) {
		zones.forEach(consumer);
	}

	@Override
	public void setZoneRef(Zone zone) {
		super.setZoneRef(zone);
		if (zone != null) {
			for (ZoneType type : zones) {
				type.setZoneRef(zone);
			}
			size = getSize(zones);
		}
	}

	@Override
	public double getSize() {
		return size;
	}

	@Override
	public ZoneType clone() {
		return clone(zones.stream().map(ZoneType::clone).collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().replace("Zone", "") + "[" + zones.stream().map(ZoneType::toString).collect(Collectors.joining(", ")) + "]";
	}

	protected abstract ZoneType clone(List<ZoneType> newZones);

	public enum UpdateDirection {
		ADD,
		REMOVE
	}
}
