package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.predicate.NumberRange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public class LightLevelSpawnRule implements SpawnRule {

	public Map<LightCheckMode, NumberRange.IntRange> lightLevel;
	public boolean requireAll;
	private static final Codec<LightCheckMode> LIGHT_TYPE_CODEC = Codec.STRING.flatXmap(name -> {
		try {
			return DataResult.success(LightCheckMode.valueOf(name.toUpperCase(Locale.ROOT)));
		} catch (IllegalArgumentException err) {
			return DataResult.error(err::getLocalizedMessage);
		}
	}, type -> DataResult.success(type.name().toLowerCase(Locale.ROOT)));

	public static final MapCodec<LightLevelSpawnRule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.simpleMap(
					LIGHT_TYPE_CODEC, NumberRange.IntRange.CODEC,
					Keyable.forStrings(() -> Arrays.stream(LightCheckMode.values()).map(type -> type.name().toLowerCase(Locale.ROOT)))
			).fieldOf("lightLevel").forGetter(rule -> rule.lightLevel),
			Codec.BOOL.fieldOf("andMode").orElse(false).forGetter(rule -> rule.requireAll)
	).apply(instance, LightLevelSpawnRule::new));

	public LightLevelSpawnRule(Map<LightCheckMode, NumberRange.IntRange> lightLevel, boolean requireAll) {
		this.lightLevel = lightLevel;
		this.requireAll = requireAll;
	}

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends LightLevelSpawnRule> getType() {
		return SpawnRuleRegistry.LIGHT_LEVEL;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		if (requireAll) {
			for (var level : lightLevel.entrySet()) {
				if (!level.getValue().test(level.getKey().getLight(world, pos))) {
					return false;
				}
			}
			return true;
		} else {
			for (var level : lightLevel.entrySet()) {
				if (level.getValue().test(level.getKey().getLight(world, pos))) {
					return true;
				}
			}
			return false;
		}
	}

	public enum LightCheckMode {
		SKY {
			@Override
			int getLight(WorldAccess world, BlockPos pos) {
				return world.getLightLevel(LightType.SKY, pos);
			}
		},
		BLOCK {
			@Override
			int getLight(WorldAccess world, BlockPos pos) {
				return world.getLightLevel(LightType.BLOCK, pos);
			}
		},
		LIGHT {
			@Override
			int getLight(WorldAccess world, BlockPos pos) {
				return world.getLightLevel(pos, world.getAmbientDarkness());
			}
		};

		abstract int getLight(WorldAccess world, BlockPos pos);
	}
}
