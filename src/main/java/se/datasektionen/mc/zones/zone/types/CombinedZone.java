package se.datasektionen.mc.zones.zone.types;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.command.ServerCommandSource;
import se.datasektionen.mc.zones.util.ZoneCommandUtils;
import se.datasektionen.mc.zones.ZoneManagementCommand;
import se.datasektionen.mc.zones.zone.Zone;

import java.util.ArrayList;
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

	public static ArgumentBuilder<ServerCommandSource, ?> createCommand(
			ArgumentBuilder<ServerCommandSource, ?> builder, ZoneManagementCommand.ZoneAdder addZone,
			Function<List<ZoneType>, CombinedZone> creator
	) {
		Function<Integer, Command<ServerCommandSource>> commandExecution = zoneCount -> {
			return ctx -> {
				List<ZoneType> zones = new ArrayList<>(zoneCount);
				for (int i = 0; i < zoneCount; i++) {
					zones.add(ZoneCommandUtils.getZoneType(ctx, "zone" + i));
				}
				addZone.add(() -> creator.apply(zones), ctx);
				return 1;
			};
		};
		final int maxLength = 10;
		var currentPoint = ZoneCommandUtils.zoneType("zone" + maxLength).executes(commandExecution.apply(maxLength));
		for (int i = maxLength-1; i > 0; i--) {
			var next = ZoneCommandUtils.zoneType("zone" + i).executes(commandExecution.apply(i));
			next.then(currentPoint);
			currentPoint = next;
		}
		return builder.then(currentPoint);
	}

	public CombinedZone(List<ZoneType> zones) {
		this.zones = new ArrayList<>(zones);
		if (zones.isEmpty()) {
			throw new IllegalArgumentException("Don't create an empty combined zone!");
		}
		this.setZoneRef(zones.get(0).getZoneRef());
	}

	protected abstract double getSize(Iterable<ZoneType> zones);
	protected abstract double updateSize(ZoneType newZone, UpdateDirection direction);

	public void addZone(ZoneType zone) {
		zones.add(zone);
		size = updateSize(zone, UpdateDirection.ADD);
	}

	public ZoneType removeZone(int index) {
		var removed = zones.remove(index);
		size = updateSize(removed, UpdateDirection.REMOVE);
		return removed;
	}

	public void forEach(Consumer<ZoneType> consumer) {
		zones.forEach(consumer);
	}

	public int zoneCount() {
		return zones.size();
	}

	public ZoneType getZone(int index) {
		return zones.get(index);
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
	public ZoneType copy() {
		return copy(zones.stream().map(ZoneType::copy).collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().replace("Zone", "") + "[" + zones.stream().map(ZoneType::toString).collect(Collectors.joining(", ")) + "]";
	}

	protected abstract ZoneType copy(List<ZoneType> newZones);

	public enum UpdateDirection {
		ADD,
		REMOVE
	}
}
