package se.datasektionen.mc.zones.spawns.rules;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

import java.util.List;

public class OrRule extends CombineRule {

	public OrRule(List<SpawnRule> rules) {
		super(rules);
	}

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends OrRule> getType() {
		return SpawnRuleRegistry.OR;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		for (var rule : rules) {
			if (rule.canSpawn(entityType, world, reason, pos, random)) {
				return true;
			}
		}
		return false;
	}
}
