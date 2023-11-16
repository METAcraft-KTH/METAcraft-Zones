package se.datasektionen.mc.zones.zone;

import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.datasektionen.mc.zones.METAcraftZones;
import se.datasektionen.mc.zones.ZoneManager;
import se.datasektionen.mc.zones.zone.data.ZoneData;
import se.datasektionen.mc.zones.zone.data.ZoneDataType;
import se.datasektionen.mc.zones.zone.data.ZoneDataRegistry;
import se.datasektionen.mc.zones.zone.types.ZoneType;

import java.util.*;

public class RealZone implements Zone {
	private static final String NAME = "name";
	private static final String DIM = "dim";
	private static final String REMOTE_DIMS = "remote_dims";
	private static final String ZONE = "zone";
	private static final String DATA = "data";
	private static final String PRIORITY = "priority";

	protected final World world;

	protected String name;
	protected ZoneType zone;
	protected final Map<RegistryKey<World>, Zone> remoteDimensions = new HashMap<>();
	protected int priority;
	protected final Map<ZoneDataType<?>, ZoneData> zoneData;
	protected final Runnable markNeedsSave;

	public RealZone(String name, World world, ZoneType zone, Map<ZoneDataType<?>, ZoneData> zoneData, int priority, Runnable markNeedsSave) {
		this.name = name;
		this.world = world;
		this.zone = zone;
		this.zoneData = zoneData instanceof HashMap<ZoneDataType<?>, ZoneData> ? zoneData : new HashMap<>(zoneData);
		this.priority = priority;
		this.markNeedsSave = markNeedsSave;
	}

	@Override
	public boolean contains(BlockPos pos) {
		return zone.contains(pos);
	}

	public <T extends ZoneData> Optional<T> get(ZoneDataType<T> data) {
		return Optional.ofNullable((T) zoneData.get(data));
	}

	public Collection<ZoneData> getAllData() {
		return zoneData.values();
	}

	public <T extends ZoneData> T getOrCreate(ZoneDataType<T> data) {
		return get(data).orElseGet(() -> {
			if (ZoneDataRegistry.REGISTRY.getKey(data).isEmpty()) {
				throw new IllegalStateException("You need to register your zone data types!");
			}
			zoneData.put(data, data.creator().get());
			markNeedsSave.run();
			return (T) zoneData.get(data);
		});
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public RegistryKey<World> getDim() {
		return world.getRegistryKey();
	}

	public World getWorld() {
		return world;
	}

	@Override
	public ZoneType getZone() {
		return zone;
	}

	public void setZone(ZoneType zone) {
		this.zone = zone;
		markNeedsSave.run();
	}

	public void setPriority(int priority) {
		this.priority = priority;
		markNeedsSave.run();
	}

	@Override
	public int getPriority() {
		return priority;
	}

	public NbtCompound toNBT() {
		NbtCompound nbt = new NbtCompound();
		nbt.putString(NAME, name);
		World.CODEC.encodeStart(NbtOps.INSTANCE, getDim()).resultOrPartial(
				METAcraftZones.LOGGER::error
		).ifPresent(dim -> {
			nbt.put(DIM, dim);
		});
		ZoneType.getRegistryCodec(world).encodeStart(RegistryOps.of(NbtOps.INSTANCE, world.getRegistryManager()), zone).resultOrPartial(
				METAcraftZones.LOGGER::error
		).ifPresent(zone -> {
			nbt.put(ZONE, zone);
		});

		NbtList remoteDims = new NbtList();
		for (var dim : remoteDimensions.keySet()) {
			World.CODEC.encodeStart(NbtOps.INSTANCE, dim).resultOrPartial(
					METAcraftZones.LOGGER::error
			).ifPresent(remoteDims::add);
		}
		nbt.put(REMOTE_DIMS, remoteDims);

		NbtList data = new NbtList();
		zoneData.values().forEach(dataValue -> {
			ZoneData.REGISTRY_CODEC.encodeStart(
					RegistryOps.of(NbtOps.INSTANCE, world.getRegistryManager()), dataValue
			).resultOrPartial(METAcraftZones.LOGGER::error).ifPresent(data::add);
		});
		nbt.put(DATA, data);

		nbt.putInt(PRIORITY, priority);
		return nbt;
	}

	public static Optional<RealZone> fromNBT(MinecraftServer server, NbtCompound nbt, Runnable markNeedsSave) {
		return World.CODEC.parse(NbtOps.INSTANCE, nbt.get(DIM)).resultOrPartial(
				METAcraftZones.LOGGER::error
		).flatMap(dim -> {
			World world = server.getWorld(dim);
			if (world == null) {
				METAcraftZones.LOGGER.error("Dimension invalid");
				return Optional.empty();
			}
			return ZoneType.getRegistryCodec(world).parse(RegistryOps.of(NbtOps.INSTANCE, world.getRegistryManager()), nbt.get(ZONE)).resultOrPartial(
					METAcraftZones.LOGGER::error
			).map(zone -> {
				Map<ZoneDataType<?>, ZoneData> dataTypes = new HashMap<>();
				NbtList data = nbt.getList(DATA, NbtElement.COMPOUND_TYPE);
				for (NbtElement element : data) {
					ZoneData.REGISTRY_CODEC.parse(
							RegistryOps.of(NbtOps.INSTANCE, world.getRegistryManager()), element
					).resultOrPartial(METAcraftZones.LOGGER::error).ifPresent(dataValue -> {
						dataTypes.put(dataValue.getType(), dataValue);
					});
				}

				var container = new RealZone(
						nbt.getString(NAME), world, zone, dataTypes, nbt.getInt(PRIORITY), markNeedsSave
				);

				NbtList remoteDims = nbt.getList(REMOTE_DIMS, NbtElement.STRING_TYPE);
				for (NbtElement element : remoteDims) {
					World.CODEC.parse(NbtOps.INSTANCE, element).resultOrPartial(
							METAcraftZones.LOGGER::error
					).flatMap(
							otherDim -> Optional.ofNullable(server.getWorld(otherDim))
					).ifPresent(otherDim -> {
						container.remoteDimensions.put(otherDim.getRegistryKey(), new RemoteZone(otherDim, container));
					});
				}
				return container;
			});
		});
	}

	@Override
	public boolean isRealZone() {
		return true;
	}

	@Override
	public RealZone getRealZone() {
		return this;
	}

	public boolean hasRemoteZone(RegistryKey<World> dim) {
		return remoteDimensions.containsKey(dim);
	}

	public Collection<Zone> getRemoteZones() {
		return remoteDimensions.values();
	}

	public void addRemoteDimension(World remoteDim) {
		if (remoteDim.getRegistryKey() != getDim()) {
			var newContainer = new RemoteZone(remoteDim, this);
			if (world.getServer() != null) {
				ZoneManager.getInstance(world.getServer()).addZone(newContainer);
			}
			remoteDimensions.put(remoteDim.getRegistryKey(), newContainer);
		}
	}

	public void removeRemoteDimension(World remoteDim) {
		if (world.getServer() != null) {
			ZoneManager.getInstance(world.getServer()).removeZone(remoteDimensions.remove(remoteDim.getRegistryKey()));
		}
	}

}