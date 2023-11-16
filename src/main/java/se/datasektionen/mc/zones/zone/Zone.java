package se.datasektionen.mc.zones.zone;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import se.datasektionen.mc.zones.zone.data.ZoneData;
import se.datasektionen.mc.zones.zone.data.ZoneDataType;
import se.datasektionen.mc.zones.zone.types.ZoneType;

import java.util.Optional;

public interface Zone extends Comparable<Zone> {

	boolean contains(BlockPos pos);

	<T extends ZoneData> Optional<T> get(ZoneDataType<T> data);

	<T extends ZoneData> T getOrCreate(ZoneDataType<T> data);

	String getName();

	RegistryKey<World> getDim();

	ZoneType getZone();

	int getPriority();

	boolean isRealZone();

	RealZone getRealZone();


	@Override
	default int compareTo(@NotNull Zone zone) {
		int state = Integer.compare(zone.getPriority(), getPriority());
		if (state == 0) {
			state = Double.compare(this.getZone().getSize(), zone.getZone().getSize());
			if (state == 0) {
				return getName().compareTo(zone.getName());
			}
		}
		return state;
	}

}
