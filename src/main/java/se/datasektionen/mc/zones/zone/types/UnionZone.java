package se.datasektionen.mc.zones.zone.types;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.stream.Streams;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnionZone extends CombinedZone {

	public UnionZone(World world, ZoneType... zones) {
		this(world, new ArrayList<>(Arrays.asList(zones)));
	}

	public UnionZone(World world, List<ZoneType> zones) {
		super(world, zones);
	}

	@Override
	protected double getSize(Iterable<ZoneType> zones) {
		return Streams.of(zones).mapToDouble(ZoneType::getSize).reduce(0.0, Double::sum);
	}

	@Override
	protected double updateSize(ZoneType newZone, UpdateDirection direction) {
		return switch (direction) {
			case ADD -> size + newZone.getSize();
			case REMOVE -> size - newZone.getSize();
		};
	}

	@Override
	public boolean contains(BlockPos pos) {
		for (var zone : zones) {
			if (zone.contains(pos)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ZoneType clone(World otherWorld, List<ZoneType> zones) {
		return new UnionZone(otherWorld, zones);
	}

	@Override
	public ZoneRegistry.ZoneType<?> getType() {
		return ZoneRegistry.union;
	}
}
