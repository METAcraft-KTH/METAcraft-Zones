package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.function.Function;

public abstract class CombineRule implements SpawnRule {

	public static <T extends CombineRule> MapCodec<T> createCodec(Function<List<SpawnRule>, T> creator) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				SpawnRule.REGISTRY_CODEC.listOf().fieldOf("rules").forGetter(rule -> rule.rules)
		).apply(instance, creator));
	}

	public List<SpawnRule> rules;

	public CombineRule(List<SpawnRule> rules) {
		this.rules = rules;
	}

}
