package se.datasektionen.mc.zones.zone;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ZoneMap {

	protected Multimap<RegistryKey<World>, Zone> worldZones = MultimapBuilder.hashKeys().treeSetValues().build();
	protected Map<String, RealZone> zones = new HashMap<>();

	protected final Runnable markNeedsSave;
	protected final Consumer<Zone> onAdd;
	protected final Consumer<Zone> onRemove;

	public ZoneMap(Runnable markNeedsSave, Consumer<Zone> onAdd, Consumer<Zone> onRemove) {
		this.markNeedsSave = markNeedsSave;
		this.onAdd = onAdd;
		this.onRemove = onRemove;
	}

	public void addZone(Zone zone) {
		if (zone.isRealZone()) {
			markNeedsSave.run();
			onAdd.accept(zone);
		}
		addZoneInternal(zone);
	}

	private void addZoneInternal(Zone zone) {
		if (zone.isRealZone()) {
			zones.put(zone.getName(), zone.getRealZone());
		}
		worldZones.put(zone.getDim(), zone);
		if (zone.isRealZone()) {
			for (Zone remoteZone : zone.getRealZone().getRemoteZones()) {
				worldZones.put(remoteZone.getDim(), remoteZone);
			}
		}
	}

	public boolean removeZone(String name) {
		if (!zones.containsKey(name)) return false;
		removeZone(zones.get(name));
		return true;
	}

	public void removeZone(Zone zone) {
		if (zone.isRealZone()) {
			zones.remove(zone.getName());
			onRemove.accept(zone);
			markNeedsSave.run();
		}
		worldZones.remove(zone.getDim(), zone);
		if (zone.isRealZone()) {
			for (Zone remoteZone : zone.getRealZone().getRemoteZones()) {
				worldZones.remove(remoteZone.getDim(), remoteZone);
			}
		}
	}

	public RealZone getZone(String name) {
		return zones.get(name);
	}

	public Collection<Zone> getZones(RegistryKey<World> dim) {
		return worldZones.get(dim);
	}

	public Collection<String> getZoneNames() {
		return zones.keySet();
	}

	public Collection<RealZone> getZones() {
		return zones.values();
	}

	public void updatePriority(RealZone zone) {
		worldZones.get(zone.getDim()).remove(zone);
		worldZones.get(zone.getDim()).add(zone);
	}

	public boolean containsZone(String name) {
		return zones.containsKey(name);
	}

	public NbtList writeNBT() {
		NbtList list = new NbtList();
		for (RealZone container : zones.values()) {
			list.add(container.toNBT());
		}
		return list;
	}

	public void readNBT(MinecraftServer server, NbtList nbt) {
		if (!nbt.isEmpty() && nbt.getHeldType() != NbtElement.COMPOUND_TYPE) {
			throw new IllegalStateException("NBT type of list must be Compound!");
		}
		for (NbtElement element : nbt) {
			RealZone.fromNBT(server, ((NbtCompound) element), markNeedsSave).ifPresent(this::addZoneInternal);
		}
	}

}
