package se.datasektionen.mc.zones;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import se.datasektionen.mc.zones.zone.RealZone;
import se.datasektionen.mc.zones.zone.Zone;
import se.datasektionen.mc.zones.zone.ZoneMap;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ZoneManager extends PersistentState {

	private static final String stateKey = "metacraft-zones";
	private static final String zonesKey = "zones";

	public static ZoneManager getInstance(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(getType(server), stateKey);
	}

	private static PersistentState.Type<ZoneManager> getType(MinecraftServer server) {
		return new Type<>(
				() -> createNew(server), nbt -> fromNbt(server, nbt), null
		);
	}

	private static ZoneManager createNew(MinecraftServer server) {
		METAcraftZones.LOGGER.info("No previous state found, setting default values");
		return new ZoneManager(server);
	}

	private static ZoneManager fromNbt(MinecraftServer server, NbtCompound tag) {
		METAcraftZones.LOGGER.info("Previous state found, loading values");
		ZoneManager settings = new ZoneManager(server);
		settings.readNbt(tag);
		return settings;
	}

	protected final MinecraftServer server;

	protected ZoneManager(MinecraftServer server) {
		this.server = server;
	}
	private final ZoneMap zones = new ZoneMap(this::markDirty);

	public Optional<Zone> getFirstZoneMatching(RegistryKey<World> dim, Predicate<Zone> zonePredicate) {
		return zones.getZones(dim).stream().filter(zonePredicate).findFirst();
	}

	public Optional<Zone> getZoneAt(RegistryKey<World> dim, BlockPos pos, Predicate<Zone> zonePredicate) {
		return getFirstZoneMatching(dim, zone -> zone.contains(pos) && zonePredicate.test(zone));
	}

	public Optional<Zone> getZoneAt(RegistryKey<World> dim, BlockPos pos) {
		return getFirstZoneMatching(dim, zone -> zone.contains(pos));
	}

	public <T> Optional<T> getValueForPrimaryZone(RegistryKey<World> dim, BlockPos pos, Function<Zone, Optional<T>> valueGetter) {
		return getValueForPrimaryZone(dim, zone -> {
			if (!zone.contains(pos)) {
				return Optional.empty();
			}
			return valueGetter.apply(zone);
		});
	}

	public <T> Optional<T> getValueForPrimaryZone(RegistryKey<World> dim, Function<Zone, Optional<T>> valueGetter) {
		return zones.getZones(dim).stream().map(valueGetter).filter(Optional::isPresent).map(Optional::get).findFirst();
	}

	public ZoneMap getZones() {
		return zones;
	}

	public void addZone(Zone zone) {
		zones.addZone(zone);
	}

	public void removeZone(Zone zone) {
		zones.removeZone(zone);
	}

	public boolean removeZone(String zone) {
		return zones.removeZone(zone);
	}
	
	public RealZone getZone(String name) {
		return zones.getZone(name);
	}

	public void updatePriority(RealZone zone) {
		zones.updatePriority(zone);
	}

	public Collection<Zone> getZones(RegistryKey<World> dim) {
		return zones.getZones(dim);
	}

	public Collection<String> getZoneNames() {
		return zones.getZoneNames();
	}

	public boolean containsZone(String name) {
		return zones.containsZone(name);
	}

	public void readNbt(NbtCompound tag) {
		zones.readNBT(server, tag.getList(zonesKey, NbtElement.COMPOUND_TYPE));
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		tag.put(zonesKey, zones.writeNBT());
		return tag;
	}

}
