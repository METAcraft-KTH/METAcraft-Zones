package se.datasektionen.mc.zones.zone.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.util.StringIdentifiable;
import se.datasektionen.mc.zones.METAcraftZones;
import se.datasektionen.mc.zones.spawns.BetterSpawnEntry;
import se.datasektionen.mc.zones.spawns.rules.SpawnRule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdditionalSpawnsZoneData extends ZoneDataEntityTracking {

	private static final MapCodec<Multimap<SpawnGroup, BetterSpawnEntry>> SPAWN_ENTRY_MAP_CODEC = Codec.simpleMap(
			SpawnGroup.CODEC,
			BetterSpawnEntry.CODEC.listOf(),
			StringIdentifiable.toKeyable(SpawnGroup.values())
	).xmap(map -> {
		var multimap = HashMultimap.<SpawnGroup, BetterSpawnEntry>create();
		for (var entry : map.entrySet()) {
			multimap.putAll(entry.getKey(), entry.getValue());
		}
		return multimap;
	}, multimap -> {
		return multimap.asMap().entrySet().stream().map(
				entry -> Pair.of(
						entry.getKey(),
						new ArrayList<>(entry.getValue())
				)
		).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	});

	public static final Codec<AdditionalSpawnsZoneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SPAWN_ENTRY_MAP_CODEC.fieldOf("spawns").forGetter(data -> data.spawns),
			EntityTypePredicate.CODEC.listOf().fieldOf("defaultSpawnBlockers").forGetter(data -> data.defaultSpawnBlockers),
			SpawnRuleEntry.CODEC.listOf().fieldOf("spawnRules").forGetter(data -> data.rules)
	).apply(instance, AdditionalSpawnsZoneData::new));


	private final ListMultimap<SpawnGroup, BetterSpawnEntry> spawns;
	private final List<EntityTypePredicate> defaultSpawnBlockers;

	private final List<SpawnRuleEntry> rules;

	public AdditionalSpawnsZoneData(
			Multimap<SpawnGroup, BetterSpawnEntry> spawns, List<EntityTypePredicate> defaultSpawnBlockers,
			List<SpawnRuleEntry> rules
	)  {
		this.spawns = MultimapBuilder.hashKeys().arrayListValues().build(spawns);
		this.defaultSpawnBlockers = new ArrayList<>(defaultSpawnBlockers);
		this.rules = new ArrayList<>(rules);
	}

	public ListMultimap<SpawnGroup, BetterSpawnEntry> getSpawns() {
		return spawns;
	}

	public List<EntityTypePredicate> getSpawnBlockers() {
		return defaultSpawnBlockers;
	}

	public List<SpawnRuleEntry> getSpawnRules() {
		return rules;
	}

	@Override
	public ZoneDataType<? extends ZoneData> getType() {
		return ZoneDataRegistry.SPAWN;
	}

	@Override
	public String toString() {
		return CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial(METAcraftZones.LOGGER::error).map(NbtElement::asString).orElse("Error");
	}

	public record SpawnRuleEntry(EntityTypePredicate type, SpawnRule rule) {
		public static final Codec<SpawnRuleEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityTypePredicate.CODEC.fieldOf("type").forGetter(SpawnRuleEntry::type),
				SpawnRule.REGISTRY_CODEC.fieldOf("rule").forGetter(SpawnRuleEntry::rule)
		).apply(instance, SpawnRuleEntry::new));
	}

}
