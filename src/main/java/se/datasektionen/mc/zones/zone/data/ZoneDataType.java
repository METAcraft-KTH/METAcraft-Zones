package se.datasektionen.mc.zones.zone.data;

import com.mojang.serialization.Codec;

import java.util.function.Supplier;

public record ZoneDataType<T extends ZoneData>(Codec<T> codec, Supplier<T> creator) {}
