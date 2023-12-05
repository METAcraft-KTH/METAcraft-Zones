package se.datasektionen.mc.zones.zone.types;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import se.datasektionen.mc.zones.ZoneCommandUtils;
import se.datasektionen.mc.zones.ZoneManagementCommand;
import se.datasektionen.mc.zones.ZoneManager;
import se.datasektionen.mc.zones.zone.Zone;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

import java.util.Optional;

public class ZoneZone extends ZoneType {

	private final String zone;
	private double size;
	private Zone zoneCache = null;

	public static final Codec<ZoneZone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("zone").forGetter(zone -> zone.zone),
			Codec.DOUBLE.fieldOf("cachedSize").orElse(0.0).forGetter(zone -> zone.size)
	).apply(instance, ZoneZone::new));

	public static ArgumentBuilder<ServerCommandSource, ?> createCommand(
			ArgumentBuilder<ServerCommandSource, ?> builder, ZoneManagementCommand.ZoneAdder addZone
	) {
		return builder.then(
			ZoneCommandUtils.zone("zone").executes(ctx -> {
				String zone = ZoneCommandUtils.getZone(ctx, "zone").getName();
				return addZone.add(() -> new ZoneZone(zone), ctx);
			})
		);
	}

	public ZoneZone(String zone, double size) {
		this.zone = zone;
		this.size = size;
	}

	public ZoneZone(String zone) {
		this(zone, 0);
	}

	protected Optional<Zone> getZone() {
		if (zoneCache == null && getZoneRef() != null) {
			ZoneManager.getInstanceNoStackOverflow(getZoneRef().getWorld().getServer()).ifPresent(zoneManager -> {
				zoneCache = zoneManager.getZone(zone);
				if (zoneCache != null) {
					var correctSize = zoneCache.getZone().getSize();
					if (size != correctSize) {
						getZoneRef().markDirty();
					}
					size = correctSize;
				}
			});
		}
		return Optional.ofNullable(zoneCache);
	}

	@Override
	public boolean contains(BlockPos pos) {
		return getZone().map(zone -> zone.contains(pos)).orElse(false);
	}

	@Override
	public double getSize() {
		return getZone().map(zone -> zone.getZone().getSize()).orElse(size);
	}

	@Override
	public ZoneType copy() {
		return new ZoneZone(zone);
	}

	@Override
	public String toString() {
		return "Zone[zone=" + zone + "]";
	}

	@Override
	public ZoneRegistry.ZoneTypeType<?> getType() {
		return ZoneRegistry.zone;
	}
}
