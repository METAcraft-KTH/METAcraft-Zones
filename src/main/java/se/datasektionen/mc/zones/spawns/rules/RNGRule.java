package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

public class RNGRule implements SpawnRule {

	public double probability;

	public static final MapCodec<RNGRule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.DOUBLE.fieldOf("probability").forGetter(rule -> rule.probability)
	).apply(instance, RNGRule::new));

	public RNGRule(double probability) {
		this.probability = probability;
	}

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends RNGRule> getType() {
		return SpawnRuleRegistry.RANDOM;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return random.nextDouble() <= probability;
	}
}
