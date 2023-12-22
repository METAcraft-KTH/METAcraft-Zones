package se.datasektionen.mc.zones.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import se.datasektionen.mc.zones.BetterSpawnEntry;
import se.datasektionen.mc.zones.ZoneManager;
import se.datasektionen.mc.zones.zone.data.ZoneDataRegistry;

import java.util.ArrayList;

@Mixin(SpawnHelper.class)
public class MixinSpawnHelper {

	@ModifyReturnValue(method = "getSpawnEntries", at = @At("RETURN"))
	private static Pool<SpawnSettings.SpawnEntry> getSpawnEntryFromZone(
			Pool<SpawnSettings.SpawnEntry> original, ServerWorld world, StructureAccessor structureAccessor,
			ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, BlockPos pos, @Nullable RegistryEntry<Biome> biomeEntry
	) {
		var zones = ZoneManager.getInstance(world.getServer()).getZonesAt(world.getRegistryKey(), pos, zone -> {
			return zone.get(ZoneDataRegistry.SPAWN).map(data -> {
				return !data.getSpawns().isEmpty();
			}).orElse(false);
		}).toList();
		if (!zones.isEmpty()) {
			var spawns = new ArrayList<>(original.getEntries());
			for (var zone : zones) {
				zone.get(ZoneDataRegistry.SPAWN).ifPresent(spawnData -> {
					spawns.addAll(spawnData.getSpawns().get(spawnGroup));
				});
			}
			return Pool.of(spawns);
 		}
		return original;
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
			}
			var nbt = mob.writeNbt(new NbtCompound());
			nbt.copyFrom(betterSpawnEntry.nbt);
			mob.readNbt(nbt);
			return data;
		}
		return initialise.call(mob, world, difficulty, spawnReason, entityData, entityNbt);
	}

	@ModifyExpressionValue(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/SpawnHelper;isValidSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/MobEntity;D)Z"
		),
		method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V"
	)
	private static boolean canSpawn(
			boolean original, @Local ServerWorld world, @Local MobEntity entity, @Local SpawnSettings.SpawnEntry spawnEntry
	) {
		if (spawnEntry instanceof BetterSpawnEntry) {
			return original;
		}
		var zones = ZoneManager.getInstance(world.getServer()).getZonesAt(world.getRegistryKey(), entity.getBlockPos(), zone -> {
			return zone.get(ZoneDataRegistry.SPAWN).map(data -> {
				return !data.getSpawnBlockers().isEmpty();
			}).orElse(false);
		}).toList();
		for (var zone : zones) {
			if (zone.get(ZoneDataRegistry.SPAWN).isPresent()) {
				var data = zone.get(ZoneDataRegistry.SPAWN).get();
				for (var blocker : data.getSpawnBlockers()) {
					if (blocker.test(world, null, entity)) {
						return false;
					}
				}
			}
		}
		return original;
	}

}
