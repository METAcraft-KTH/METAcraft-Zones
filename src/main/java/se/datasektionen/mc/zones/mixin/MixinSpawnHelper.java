package se.datasektionen.mc.zones.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.datasektionen.mc.zones.ZoneManager;
import se.datasektionen.mc.zones.spawns.BetterSpawnEntry;
import se.datasektionen.mc.zones.zone.data.ZoneDataRegistry;

import java.util.ArrayList;

@Mixin(SpawnHelper.class)
public class MixinSpawnHelper {

	@ModifyReturnValue(method = "getSpawnEntries", at = @At("RETURN"))
	private static Pool<SpawnSettings.SpawnEntry> getSpawnEntryFromZone(
			Pool<SpawnSettings.SpawnEntry> original, ServerWorld world, StructureAccessor structureAccessor,
			ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, BlockPos pos, @Nullable RegistryEntry<Biome> biomeEntry
	) {
		MutableBoolean hasBlockers = new MutableBoolean(false);
		var zones = ZoneManager.getInstance(world.getServer()).getZonesAt(world.getRegistryKey(), pos, zone -> {
			return zone.get(ZoneDataRegistry.SPAWN).map(data -> {
				if (!data.getSpawnBlockers().isEmpty()) {
					hasBlockers.setTrue();
				}
				return !data.getSpawns().isEmpty() || !data.getSpawnBlockers().isEmpty();
			}).orElse(false);
		}).toList();
		if (!zones.isEmpty()) {
			var spawns = original.getEntries();
			if (hasBlockers.isTrue()) {
				Multimap<EntityType<?>, SpawnSettings.SpawnEntry> spawnsMap = spawns.stream().collect(
						Multimaps.toMultimap(entry -> entry.type, entry -> entry, HashMultimap::create)
				);
				for (var zone : zones) {
					zone.get(ZoneDataRegistry.SPAWN).ifPresent(spawnData -> {
						for (var blocker : spawnData.getSpawnBlockers()) {
							for (var type : blocker.types()) {
								spawnsMap.removeAll(type.value());
							}
						}
					});
				}
				spawns = new ArrayList<>(spawnsMap.values());
			} else {
				spawns = new ArrayList<>(original.getEntries());
			}
			for (var zone : zones) {
				var spawnData = zone.get(ZoneDataRegistry.SPAWN).orElse(null);
				if (spawnData != null) {
					spawns.addAll(spawnData.getSpawns().get(spawnGroup));
				}
			}
			return Pool.of(spawns);
 		}
		return original;
	}

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/SpawnHelper;isValidSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/MobEntity;D)Z"
			),
			method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V"
	)
	private static void addNBTBeforeSpawnCheck(
			SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, SpawnHelper.Checker checker,
			SpawnHelper.Runner runner, CallbackInfo ci, @Local SpawnSettings.SpawnEntry spawnEntry,
			@Local MobEntity mob
	) {
		//Apply nbt once first in order for spawn check to check it.
		if (spawnEntry instanceof BetterSpawnEntry betterSpawnEntry) {
			var nbt = mob.writeNbt(new NbtCompound());
			nbt.copyFrom(betterSpawnEntry.nbt);
			mob.readNbt(nbt);
		}
	}

	@WrapOperation(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/mob/MobEntity;initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/EntityData;"
		),
		method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V"
	)
	private static EntityData onSpawnEntities(
			MobEntity mob, ServerWorldAccess world, LocalDifficulty difficulty,
			SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt, Operation<EntityData> initialise,
			@Local SpawnSettings.SpawnEntry spawnEntry
	) {
		if (spawnEntry instanceof BetterSpawnEntry betterSpawnEntry) {
			EntityData data = null;
			if (betterSpawnEntry.shouldInitialise) {
				data = initialise.call(mob, world, difficulty, spawnReason, entityData, entityNbt);
				var nbt = mob.writeNbt(new NbtCompound()); //Apply nbt again after initialisation ()
				nbt.copyFrom(betterSpawnEntry.nbt);
				mob.readNbt(nbt);
			}
			return data;
		}
		return initialise.call(mob, world, difficulty, spawnReason, entityData, entityNbt);
	}

	@WrapOperation(
		method = "canSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/biome/SpawnSettings$SpawnEntry;Lnet/minecraft/util/math/BlockPos$Mutable;D)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/SpawnRestriction;canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z"
		)
	)
	private static <T extends Entity> boolean canSpawn(
			EntityType<T> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, Operation<Boolean> original
	) {
		var zones = ZoneManager.getInstance(world.getServer()).getZonesAt(world.toServerWorld().getRegistryKey(), pos, zone -> {
			return zone.get(ZoneDataRegistry.SPAWN).map(data -> {
				return !data.getSpawnRules().isEmpty();
			}).orElse(false);
		}).toList();
		for (var zone : zones) {
			if (zone.get(ZoneDataRegistry.SPAWN).isPresent()) {
				var data = zone.get(ZoneDataRegistry.SPAWN).get();
				for (var rule : data.getSpawnRules()) {
					if (rule.type().matches(type)) {
						return rule.rule().canSpawn(type, world, spawnReason, pos, random);
					}
				}
			}
		}
		return original.call(type, world, spawnReason, pos, random);
	}

}
