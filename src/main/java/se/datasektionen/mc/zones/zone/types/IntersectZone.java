package se.datasektionen.mc.zones.zone.types;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.stream.Streams;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntersectZone extends CombinedZone {

	public IntersectZone(World world, ZoneType... zones) {
		this(world, new ArrayList<>(Arrays.asList(zones)));
	}

	public IntersectZone(World world, List<ZoneType> zones) {
		super(world, zones);
	}

	@Override
	protected double getSize(Iterable<ZoneType> zones) {
		return Streams.of(zones).mapToDouble(ZoneType::getSize).min().orElse(0);
	}

	@Override
	protected double updateSize(ZoneType newZone, UpdateDirection direction) {
		return switch (direction) {
			case ADD -> Math.min(size, newZone.getSize());
			case REMOVE -> getSize(zones);
		};
	}

	@Override
	public boolean contains(BlockPos pos) {
		for (var zone : zones) {
			if (!zone.contains(pos)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ZoneType clone(World otherWorld, List<ZoneType> zones) {
		return new IntersectZone(otherWorld, zones);
	}

	@Override
	public ZoneRegistry.ZoneType<?> getType() {
		return ZoneRegistry.intersect;
	}
}
