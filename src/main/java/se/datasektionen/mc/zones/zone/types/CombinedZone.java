package se.datasektionen.mc.zones.zone.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class CombinedZone extends ZoneType {

	protected final List<ZoneType> zones;
	protected double size;

	public static <T extends CombinedZone> Codec<T> getCodec(World world, BiFunction<World, List<ZoneType>, T> creator) {
		return RecordCodecBuilder.create(instance -> instance.group(
				getRegistryCodec(world).listOf().fieldOf("zones").forGetter(zone -> zone.zones)
		).apply(instance, zones -> creator.apply(world, zones)));
	}

	public CombinedZone(World world, List<ZoneType> zones) {
		super(world);
		this.zones = zones;
		size = getSize(zones);
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
	public double getSize() {
		return size;
	}

	@Override
	public ZoneType clone(World otherWorld) {
		return clone(otherWorld, zones.stream().map(zone -> zone.clone(otherWorld)).collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().replace("Zone", "") + "[" + zones.stream().map(ZoneType::toString).collect(Collectors.joining(", ")) + "]";
	}

	protected abstract ZoneType clone(World otherWorld, List<ZoneType> newZones);

	public enum UpdateDirection {
		ADD,
		REMOVE
	}
}
