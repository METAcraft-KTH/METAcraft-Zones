package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

public class NotRule implements SpawnRule {

	public static final Codec<NotRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SpawnRule.REGISTRY_CODEC.fieldOf("rule").forGetter(rule -> rule.rule)
	).apply(instance, NotRule::new));

	public SpawnRule rule;

	public NotRule(SpawnRule rule) {
		this.rule = rule;
	}

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends NotRule> getType() {
		return SpawnRuleRegistry.NOT;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return !rule.canSpawn(entityType, world, reason, pos, random);
	}
}
